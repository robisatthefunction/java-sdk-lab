package com.optimizely.ab.annotations;

import java.lang.annotation.*;

/**
 * TODO Add link to product documentation. Doesn't appear to exist ATM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface OptimizelyVariable {
    /**
     * name used for the variable as part of the optimizely data file
     * @return String
     */
    String name();
}
