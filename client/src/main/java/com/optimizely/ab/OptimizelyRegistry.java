package com.optimizely.ab;

import com.optimizely.ab.annotations.OptimizelyFeature;
import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Registry for storing OptimizelyFeatures and their definitions.
 * This can be used to avoid expensive reflection when constructing the FeatureProcessor.
 *
 * @since 4/2/18.
 */
public class OptimizelyRegistry {

    private Map<Class, FeatureProcessor> registryMap = new HashMap<>();

    /**
     * Instantiates an instance of the OptimizelyRegistry pre-registered
     * with all classes annotated with {@link OptimizelyFeature}. Since this
     * scans the entire class path its important that this method be called
     * conservatively.
     */
    public static OptimizelyRegistry get() {
        OptimizelyRegistry registry = new OptimizelyRegistry();

        // Suppresses unwanted log messages.
        Reflections.log = null;

        Reflections reflections = new Reflections();
        Set<Class<?>> features = reflections.getTypesAnnotatedWith(OptimizelyFeature.class);

        for (Class<?> feature: features) {
            registry.register(feature);
        }

        return registry;
    }

    public void register(Class<?> clazz) {
        FeatureProcessor<?> featureProcessor = new FeatureProcessor<>(clazz);
        registryMap.put(clazz, featureProcessor);
    }

    public Set<Class> getItems() {
        return this.registryMap.keySet();
    }

    @SuppressWarnings("unchecked")
    public <T> FeatureProcessor<T> get(Class<T> clazz) {
        return registryMap.get(clazz);
    }
}
