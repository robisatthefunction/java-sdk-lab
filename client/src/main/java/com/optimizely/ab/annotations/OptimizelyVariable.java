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

    /**
     * Default value
     * @return String
     */
    String defaultValue() default "";

    /**
     * Provider used to convert the value into the corresponding data type
     * @return Class
     * TODO - IMPLEMENT ME
     */
    Class provider() default Void.class;
}
