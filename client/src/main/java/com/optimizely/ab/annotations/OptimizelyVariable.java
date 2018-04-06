package com.optimizely.ab.annotations;

import java.lang.annotation.*;

/**
 * TODO Add link to product documentation. Doesn't appear to exist ATM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface OptimizelyVariable {
    String UNASSIGNED = "[unassigned]";

    /**
     * name used for the variable as part of the optimizely data file
     */
    String name();

    /**
     * Default value
     */
    String defaultValue() default UNASSIGNED;

    /**
     * Provider used to convert the value into the corresponding data type
     * TODO - IMPLEMENT ME
     */
    Class provider() default Void.class;
}
