package com.optimizely.ab.client;

import com.optimizely.ab.Optimizely;
import com.optimizely.ab.config.Variation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;


/**
 * Wrapper around the native Optimizely client to leverage
 * the logging MDC framework to persist attributes throughout
 * the request lifecycle.
 *
 * @since 7/27/17.
 */
public class OptimizelyMDCClient implements OptimizelyClient {
    private static final Logger LOG = LoggerFactory.getLogger(OptimizelyMDCClient.class);

    private static final String REVENUE_KEY = "revenue";
    private static final String VALUE_KEY = "value";

    static final String USER_IDS_KEY = "userIds";

    private static final String DELIMITER_PATTERN = "\\|";
    private static final String DELIMITER = "|";

    private final Optimizely optimizely;
    private final OptimizelyRegistry registry;

    public OptimizelyMDCClient(@Nullable Optimizely optimizely, @Nonnull OptimizelyRegistry registry) {
        this.optimizely = optimizely;
        this.registry = registry;
    }

    @Override
    @Nullable
    public Variation activate(String experimentKey, String userIdKey) {
        if (optimizely == null) {
            LOG.error("Skipping activation for {} experiment since Optimizely is null.", experimentKey);
            return null;
        }

        return optimizely.activate(experimentKey, getUserId(userIdKey), getAttributes());
    }

    @Override
    public void track(String eventName) {
        track(eventName, Collections.<String, Object>emptyMap());
    }

    @Override
    public void track(String eventName, Map<String, ?> eventTags) {
        for (String userId: getUserIds()) {
            track(eventName, eventTags, userId);
        }
    }

    private void track(String eventName, Map<String, ?> eventTags, String userId) {
        if (optimizely == null) {
            LOG.error("Not tracking {}, since optimizely has not been initialized.", eventName);
            return;
        }

        if (userId == null || userId.isEmpty()) {
            LOG.info("Not tracking {}, since userId has not been initialized.", eventName);
            return;
        }

        optimizely.track(eventName, userId, getAttributes(), eventTags);
    }

    @Override
    public void track(String eventName, Double value) {
        Map<String, Double> eventTags = new HashMap<>();
        eventTags.put(VALUE_KEY, value);

        track(eventName, eventTags);
    }

    @Override
    public void track(String eventName, Long revenue) {
        Map<String, Long> eventTags = new HashMap<>();
        eventTags.put(REVENUE_KEY, revenue);

        track(eventName, eventTags);
    }

    /**
     * Method is responsible for extracting the user id from the MDC
     * and maintaining the state of USER_IDS_KEY in MDC.
     */
    private String getUserId(String userIdKey) {
        String userId = MDC.get(userIdKey);

        if (userId != null) {
            Set<String> userIds = getUserIds();
            if (!userIds.contains(userId)) {
                userIds.add(userId);
                setUserIds(userIds);
            }
        }

        return userId;
    }

    /**
     * Since different features and experiments can use different userId keys from the
     * MDC we want to collection all used keys and send them all for each call to track.
     */
    private Set<String> getUserIds() {
        String userIds = MDC.get(USER_IDS_KEY);

        Set<String> userIdSet = new HashSet<>();

        if (userIds == null || userIds.isEmpty()) {
            return userIdSet;
        }

        for (String userId: userIds.split(DELIMITER_PATTERN)) {
            userIdSet.add(userId);
        }

        return userIdSet;
    }

    private void setUserIds(Set<String> userIds) {
        StringBuffer userIdStringBuffer = null;
        for (String userId: userIds) {
            if (userIdStringBuffer == null) {
                userIdStringBuffer = new StringBuffer(userId);
            } else {
                userIdStringBuffer.append(DELIMITER);
                userIdStringBuffer.append(userId);
            }
        }

        if (userIdStringBuffer != null) {
            MDC.put(USER_IDS_KEY, userIdStringBuffer.toString());
        }
    }

    private Map<String, String> getAttributes() {
        return MDC.getCopyOfContextMap();
    }

    @Override
    @Nullable
    public <T> T getFeature(Class<T> clazz) {
        FeatureProcessor<T> processor = registry.get(clazz);

        if (processor == null) {
            // We could simply return null, but making one last attempt to return an instance of this class.
            try {
                LOG.info("{} is NOT registered. Attempting to return default instance.", clazz);
                return clazz.newInstance();
            } catch (InstantiationException|IllegalAccessException e) {
                LOG.warn("{} could not be instantiated, returning null.", clazz, e);
                return null;
            }
        }

        String variationName = processor.getVariationName(this);
        if (variationName == null) {
            return processor.newInstance(this);
        }

        FeatureProcessor<T> variationProcessor = registry.get(clazz, variationName);

        if (variationProcessor == null) {
            LOG.info("{} is NOT registered for Variation {}. Returning null.", clazz, variationName);
            return null;
        }

        return variationProcessor.newInstance(this, processor.getOptimizelyFeature());
    }

    @Override
    public boolean isFeatureEnabled(String featureFlagKey, String userIdKey) {
        if (optimizely == null) {
            LOG.error("returning false for feature flag {} enable since Optimizely is null.", featureFlagKey);
            return false;
        }

        String userId = getUserId(userIdKey);
        if (userId == null) {
            LOG.warn("returning false for feature flag {} enable since userId is null for key {}.",
                featureFlagKey, userIdKey);
            return false;
        }

        return optimizely.isFeatureEnabled(featureFlagKey, getUserId(userIdKey), getAttributes());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getFeatureVariable(String featureFlagKey, String variableKey, String userIdKey, Class<T> clazz) {
        if (optimizely == null) {
            LOG.error("Unable to retrieve variable key: {} for feature flag: {} since Optimizely is null. Returning null", variableKey, featureFlagKey);
            return null;
        }

        if (variableKey == null || variableKey.isEmpty()) {
            throw new RuntimeException("variableKey cannot be empty or null");
        }

        String userId = getUserId(userIdKey);
        if (userId == null || userId.isEmpty()) {
            LOG.warn("returning false for feature flag variable {}.{} enable since userId is null for key {}.",
                featureFlagKey, variableKey, userIdKey);
            return null;
        }

        if (clazz == String.class) {
            return (T) optimizely.getFeatureVariableString(featureFlagKey, variableKey, userId, getAttributes());
        } else if (clazz == Double.class) {
            return (T) optimizely.getFeatureVariableDouble(featureFlagKey, variableKey, userId, getAttributes());
        } else if (clazz == Boolean.class) {
            return (T) optimizely.getFeatureVariableBoolean(featureFlagKey, variableKey, userId, getAttributes());
        } else if (clazz == Integer.class) {
            return (T) optimizely.getFeatureVariableInteger(featureFlagKey, variableKey, userId, getAttributes());
        } else if (clazz.isEnum()) {
            String enumName = optimizely.getFeatureVariableString(featureFlagKey, variableKey, userId, getAttributes());
            try {
                return (T)Enum.valueOf((Class<Enum>)clazz, enumName);
            } catch(IllegalArgumentException | NullPointerException e) {
                LOG.error("Unable to return enum of {}. Returning null", enumName, e);
                return null;
            }
        } else {
            LOG.warn("Unsupported feature variable class type: {} requested. Returning null", clazz.getSimpleName());
            return null;
        }
    }
}
