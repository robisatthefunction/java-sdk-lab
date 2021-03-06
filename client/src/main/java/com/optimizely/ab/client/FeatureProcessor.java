package com.optimizely.ab.client;

import com.optimizely.ab.annotations.OptimizelyFeature;
import com.optimizely.ab.annotations.OptimizelyVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;


/**
 * Currently you have to setup the classes to mirror the Optimizely data structures.
 *
 * There are two competing philosophies for improvement:
 * 1) PUSH these entities into Optimizely so the code is the source of truth
 * 2) PULL these entities from Optimizely so the UI is the source of truth
 *
 * TODO rename to AnnotationProcessor??
 */
public class FeatureProcessor<T> {
    private static final Logger LOG = LoggerFactory.getLogger(FeatureProcessor.class);

    private final Class<T> clazz;
    private final OptimizelyFeature optimizelyFeature;

    private final Map<Field, OptimizelyVariable> fields;

    public FeatureProcessor(Class<T> clazz) {
        this.optimizelyFeature = clazz.getAnnotation(OptimizelyFeature.class);
        this.clazz = clazz;
        this.fields = clazz.isInterface() ? new HashMap<>() : processFields(clazz);
    }

    @Nonnull
    public Map<Field, OptimizelyVariable> getFieldVariableMap() {
        return fields;
    }

    public OptimizelyFeature getOptimizelyFeature() {
        return optimizelyFeature;
    }

    @SuppressWarnings("unchecked")
    public T newInstance(OptimizelyClient optimizelyClient) {
        return newInstance(optimizelyClient, optimizelyFeature);
    }

    @SuppressWarnings("unchecked")
    public T newInstance(OptimizelyClient optimizelyClient, OptimizelyFeature optimizelyFeature) {
        try {
            T instance = clazz.newInstance();
            String featureName = optimizelyFeature.name();
            String userIdKey = optimizelyFeature.userIdKey();
            boolean isEnabled = optimizelyClient != null && optimizelyClient.isFeatureEnabled(featureName, userIdKey);

            if (!isEnabled) {
                return instance;
            }

            for (Map.Entry<Field, OptimizelyVariable> entry: fields.entrySet()) {
                Field field = entry.getKey();
                OptimizelyVariable optimizelyVariable = entry.getValue();
                Class<?> type = field.getType();

                Object object = optimizelyClient.getFeatureVariable(featureName, optimizelyVariable.name(), userIdKey, type);
                if (object != null) {
                    field.set(instance, object);
                }
            }

            return instance;

        } catch (InstantiationException|IllegalAccessException e) {
            LOG.error("Error instantiating new object for {}", clazz, e);
        }

        return null;
    }

    // Returns the name of the variation if it exists.
    public String getVariationName(OptimizelyClient optimizelyClient) {
        // I want get variation for feature as a thing.
        String featureName = optimizelyFeature.name();
        String userIdKey = optimizelyFeature.userIdKey();
        String variationKey = optimizelyFeature.variationKey();
        return optimizelyClient.getFeatureVariable(featureName, variationKey, userIdKey, String.class);
    }

    private Map<Field, OptimizelyVariable> processFields(Class<T> clazz) {
        Map<Field, OptimizelyVariable> fields = new HashMap<>();

        for (Field field: clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(OptimizelyVariable.class)) {
                field.setAccessible(true);
                fields.put(field, field.getAnnotation(OptimizelyVariable.class));
            }
        }

        return fields;
    }
}
