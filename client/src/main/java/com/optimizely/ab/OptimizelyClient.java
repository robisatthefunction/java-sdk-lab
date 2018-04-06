package com.optimizely.ab;

import com.optimizely.ab.config.Variation;

import java.util.Map;

/**
 * Created by mdavis on 7/27/17.
 * TODO rename package to com.optimizely.client
 */
public interface OptimizelyClient {
    // TODO move me somewhere less general
    String OPTIMIZELY_END_USER_ID_KEY = "optimizelyEndUserId";
    String LAYER_ID_KEY = "layerId";

    Variation activate(String experimentKey, String userIdKey);

    void track(String eventName);
    void track(String eventName, Map<String, ?> eventTags);

    // Convenience APIs
    void track(String eventName, Long revenue);
    void track(String eventName, Double value);

    // For feature flags
    boolean isFeatureEnabled(String featureFlagKey, String userIdKey);

    <T> T getFeature(Class<T> clazz);
    <T> T getFeatureVariable(String featureFlagKey, String variableKey, String userIdKey, Class<T> clazz);
}
