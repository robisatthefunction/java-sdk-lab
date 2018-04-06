package com.optimizely.ab;

import com.optimizely.ab.config.Variation;

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
    public <T> T getFeature(Class<T> clazz) {
        FeatureProcessor<T> processor = new FeatureProcessor<>(clazz);
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
}
