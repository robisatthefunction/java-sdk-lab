package com.optimizely.ab.client;

import com.optimizely.ab.Optimizely;
import com.optimizely.ab.annotations.OptimizelyFeature;
import com.optimizely.ab.annotations.OptimizelyVariable;
import com.optimizely.ab.config.FeatureFlag;
import com.optimizely.ab.config.LiveVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates the instance Optimizely against the registered features.
 * Currently supports validating features and their associated variables.
 *
 * @since 4/2/18.
 */
public class OptimizelyValidator {
    private static final Logger LOG = LoggerFactory.getLogger(OptimizelyValidator.class);

    private OptimizelyRegistry registry;

    public OptimizelyValidator(OptimizelyRegistry registry) {
        this.registry = registry;
    }

    public boolean validate(Optimizely optimizely) {
        for (Class<?> clazz: registry.getItems()) {
            FeatureProcessor<?> featureProcessor = registry.get(clazz);
            OptimizelyFeature optimizelyFeature = featureProcessor.getOptimizelyFeature();

            if (!optimizelyFeature.validate()) {
                continue;
            }

            String featureName = optimizelyFeature.name();

            FeatureFlag featureFlag = optimizely.getProjectConfig().getFeatureKeyMapping().get(featureName);
            if (featureFlag == null) {
                LOG.error(featureName + "{} not a valid feature name.", featureName);
                return false;
            }

            for (OptimizelyVariable variable: featureProcessor.getFieldVariableMap().values()) {
                String variableName = variable.name();
                LiveVariable liveVariable = featureFlag.getVariableKeyToLiveVariableMap().get(variableName);

                if (liveVariable == null) {
                    LOG.error("{} not a valid variable name.", variableName);
                    return false;
                }
            }
        }

        return true;
    }
}
