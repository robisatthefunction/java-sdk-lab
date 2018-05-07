package com.optimizely.ab.examples;

import com.google.common.base.Objects;
import com.optimizely.ab.annotations.OptimizelyFeature;
import com.optimizely.ab.annotations.OptimizelyVariable;

/**
 * Simple feature used to demonstrate the annotation framework.
 */
@OptimizelyFeature(name = "simple_feature", userIdKey = "user_key")
public class SimpleFeature  {

    @OptimizelyVariable(name = "simple_string", defaultValue = "default")
    public String simpleString;

    @Override
    public String toString() {
        return simpleString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleFeature that = (SimpleFeature) o;
        return Objects.equal(simpleString, that.simpleString);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(simpleString);
    }
}
