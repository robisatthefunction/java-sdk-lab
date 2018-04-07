package com.optimizely.ab.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * TODO Add link to product documentation. Doesn't appear to exist ATM
 */
@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface OptimizelyFeature {
    /**
     * Name of the feature as implemented within the Optimizely data file.
     * @return String
     */
    String name();

    /**
     * Store key to use for the bucketing id.
     * @return String
     */
    String userIdKey();

    /**
     * Indicate whether or not to validate the instance of Optimizely against this feature.
     * @return boolean
     */
    boolean validate() default true;
}
