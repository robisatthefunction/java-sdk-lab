package com.optimizely.ab.client;

import com.optimizely.ab.annotations.OptimizelyFeature;
import com.optimizely.ab.annotations.OptimizelyVariation;

/**
 * Created by mdavis on 5/2/18.
 */
@OptimizelyFeature(name = "test_interface")
public interface TestInterface {

    @OptimizelyVariation(name = "variation_1")
    class TestVariation1 implements TestInterface {

    }

    @OptimizelyVariation(name = "variation_1")
    class TestVariation2 implements TestInterface {

    }
}
