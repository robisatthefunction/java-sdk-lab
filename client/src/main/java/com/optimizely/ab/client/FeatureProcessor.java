package com.optimizely.ab.client;

import com.optimizely.ab.annotations.OptimizelyFeature;
import com.optimizely.ab.annotations.OptimizelyVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static com.optimizely.ab.annotations.OptimizelyVariable.UNASSIGNED;

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
        if (!clazz.isAnnotationPresent(OptimizelyFeature.class)) {
            // eh
            throw new RuntimeException();
        }

        this.optimizelyFeature = clazz.getAnnotation(OptimizelyFeature.class);
        this.clazz = clazz;
        this.fields = processFields(clazz);
    }

    public Map<Field, OptimizelyVariable> getFieldVariableMap() {
        return fields;
    }

    public OptimizelyFeature getOptimizelyFeature() {
        return optimizelyFeature;
    }

    @SuppressWarnings("unchecked")
    public T newInstance(OptimizelyClient optimizelyClient) {
        try {
            T instance = clazz.newInstance();
            String featureName = optimizelyFeature.name();
            String userIdKey = optimizelyFeature.userIdKey();
            boolean isEnabled = optimizelyClient != null && optimizelyClient.isFeatureEnabled(featureName, userIdKey);

            for (Map.Entry<Field, OptimizelyVariable> entry: fields.entrySet()) {
                Field field = entry.getKey();
                OptimizelyVariable optimizelyVariable = entry.getValue();

                String value = optimizelyVariable.defaultValue();
                Class<?> type = field.getType();

                Object defaultValue = null;

                // Yes this is a hack. There has to be a better way. Probably where the Provider comes in :)
                if (type.isEnum()) {
                    try {
                        defaultValue = Enum.valueOf((Class<Enum>)type, value);
                    } catch (IllegalArgumentException e) {
                        LOG.warn("{} is not a value enum value for {}", value, type);
                    }
                }

                if (type == String.class && !value.equals(UNASSIGNED)) {
                    defaultValue = value;
                }

                if (isEnabled) {
                    Object object = optimizelyClient.getFeatureVariable(featureName, optimizelyVariable.name(), userIdKey, type);
                    if (object != null) {
                        field.set(instance, object);
                        continue;
                    }
                }

                if (defaultValue != null) {
                    field.set(instance, defaultValue);
                }
            }

            return instance;

        } catch (InstantiationException|IllegalAccessException e) {
            LOG.error("Error instantiating new object for {}", clazz, e);
        }

        return null;
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
