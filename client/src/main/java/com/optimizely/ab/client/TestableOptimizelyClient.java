package com.optimizely.ab.client;

import com.optimizely.ab.Optimizely;
import com.optimizely.ab.config.Variation;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Helper class to verify OptimizelyClient implementation. This gives us a bit more control
 * then trying to get Mockito to provide all the resources
 */
public class TestableOptimizelyClient implements OptimizelyClient {

    private List<String> activations = new ArrayList<>();
    private List<String> events = new ArrayList<>();
    private List<Long> revenues = new ArrayList<>();
    private List<Double> values = new ArrayList<>();
    private Map<String, Variation> variationMap = new HashMap<>();
    private Map<Class<?>, Object> providedFeatures = new HashMap<>();
    private Set<String> enabledFeatures = new HashSet<>();

    @Override
    public Variation activate(String experimentKey, String userIdKey) {
        activations.add(experimentKey);
        return variationMap.get(experimentKey);
    }

    @Override
    public void track(String eventName) {
        track(eventName, Collections.<String, Object>emptyMap());
    }

    @Override
    public void track(String eventName, Map<String, ?> eventTags) {
        events.add(eventName);
    }

    @Override
    public void track(String eventName, Double value) {
        values.add(value);
        Map<String, Double> eventTags = new HashMap<>();
        eventTags.put("value", value);

        track(eventName, eventTags);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getFeature(Class<T> clazz) {
        FeatureProcessor<T> processor = new FeatureProcessor<>(clazz);

        if (providedFeatures.containsKey(clazz)) {
            activations.add(processor.getOptimizelyFeature().name());
            return (T) providedFeatures.get(clazz);
        }

        return processor.newInstance(this);
    }

    @Override
    public void track(String eventName, Long revenue) {
        revenues.add(revenue);
        Map<String, Long> eventTags = new HashMap<>();
        eventTags.put("revenue", revenue);

        track(eventName, eventTags);
    }

    @Override
    public boolean isFeatureEnabled(String featureFlagKey, String userIdKey) {
        activations.add(featureFlagKey);
        return enabledFeatures.contains(featureFlagKey);
    }

    @Override
    public <T> T getFeatureVariable(String featureFlagKey, String variableKey, String userIdKey, Class<T> clazz) {
        return null;
    }

    public void setVariation(String experimentKey, Variation variation) {
        this.variationMap.put(experimentKey, variation);
    }

    public void enableFeature(String feature) {
        enabledFeatures.add(feature);
    }

    public void provideFeature(Object instance) {
        providedFeatures.put(instance.getClass(), instance);
    }

    public List<String> getActivations() {
        return activations;
    }

    public List<String> getEvents() {
        return events;
    }

    public List<Long> getRevenues() {
        return revenues;
    }

    public List<Double> getValues() {
        return values;
    }

    public void reset() {
        events.clear();
        values.clear();
        revenues.clear();
        activations.clear();
        variationMap.clear();
    }

    @Nullable
    @Override
    public Optimizely getOptimizely() {
        return null;
    }
}
