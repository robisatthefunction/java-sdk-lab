package com.optimizely.ab.client;

import com.optimizely.ab.annotations.OptimizelyFeature;
import com.optimizely.ab.annotations.OptimizelyVariation;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOG = LoggerFactory.getLogger(OptimizelyRegistry.class);

    private Map<Class, FeatureProcessor> featureMap = new HashMap<>();
    private Map<Class, Map<String, Class>> interfaceMap = new HashMap<>();

    /**
     * Instantiates an instance of the OptimizelyRegistry pre-registered
     * with all classes annotated with {@link OptimizelyFeature}. Since this
     * scans the entire class path its important that this method be called
     * conservatively.
     * @return OptimizelyRegistry
     */
    public static OptimizelyRegistry get() {
        OptimizelyRegistry registry = new OptimizelyRegistry();

        // Suppresses unwanted log messages.
        Reflections.log = null;

        Reflections reflections = new Reflections();
        registry.registerAll(reflections.getTypesAnnotatedWith(OptimizelyFeature.class));
        registry.registerAll(reflections.getTypesAnnotatedWith(OptimizelyVariation.class));

        return registry;
    }

    public void register(Class<?> clazz) {
        if (clazz.isInterface()) {
            interfaceMap.put(clazz, new HashMap<>());
        }

        FeatureProcessor<?> featureProcessor = new FeatureProcessor<>(clazz);
        featureMap.put(clazz, featureProcessor);

        if (clazz.isAnnotationPresent(OptimizelyVariation.class)) {
            OptimizelyVariation optimizelyVariation = clazz.getAnnotation(OptimizelyVariation.class);
            for (Class<?> interphase : clazz.getInterfaces()) {
                Map<String, Class> variationNameMap = interfaceMap.get(interphase);

                // Interfaces must be registered prior to their implementations
                if (variationNameMap == null) {
                    continue;
                }

                variationNameMap.put(optimizelyVariation.name(), clazz);
            }
        }
    }

    public void registerAll(Set<Class<?>> classes) {
        for (Class<?> clazz: classes) {
            register(clazz);
        }
    }

    public Set<Class> getItems() {
        return this.featureMap.keySet();
    }

    @SuppressWarnings("unchecked")
    public <T> FeatureProcessor<T> get(Class<T> clazz) {
        LOG.debug("Fetching processor for class {}", clazz);
        return featureMap.get(clazz);
    }

    @SuppressWarnings("unchecked")
    public <T> FeatureProcessor<T> get(Class<T> clazz, String variationName) {
        Map<String, Class> variationMap = interfaceMap.get(clazz);

        if (variationMap == null) {
            return null;
        }

        LOG.debug("Fetching variation for variation: {}", variationName);
        return get(variationMap.get(variationName));
    }
}
