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
public @interface OptimizelyVariation {
    /**
     * Name of the feature as implemented within the Optimizely data file.
     * @return String
     */
    String name();

    /**
     * Is this variation the baseline variation?
     * @return boolean
     * TODO - IMPLEMENT ME
     */
    boolean baseline() default false;
}
