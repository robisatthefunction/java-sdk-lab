package com.optimizely.ab;

import com.optimizely.ab.annotations.OptimizelyFeature;
import com.optimizely.ab.annotations.OptimizelyVariable;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.*;

/**
 * Created by mdavis on 4/2/18.
 */
public class OptimizelyValidatorTest {

    private Optimizely optimizely;
    private OptimizelyRegistry registry;
    private OptimizelyValidator validator;

    @Before
    public void setUp() throws Exception {
        optimizely = TestFeature.getTestOptimizely();
        registry = new OptimizelyRegistry();
        validator = new OptimizelyValidator(registry);
    }

    @Test
    public void validateEmptyRegistry() throws Exception {
        validator.validate(optimizely);
    }

    @Test
    public void validateTestFeature() throws Exception {
        registry.register(com.optimizely.ab.TestFeature.class);
        assertTrue(validator.validate(optimizely));
    }

    @Test
    public void validateMissingFeature() throws Exception {
        registry.register(TestMissingFeature.class);
        assertFalse(validator.validate(optimizely));
    }

    @Test
    public void validateMissingVariable() throws Exception {
        registry.register(TestFeatureMissingVariable.class);
        assertFalse(validator.validate(optimizely));
    }

    @Test
    public void validateSkipped() throws Exception {
        registry.register(TestFeatureMissingDoNotValidate.class);
        assertTrue(validator.validate(optimizely));
    }

    @OptimizelyFeature(name = "test_missing_feature", userIdKey = "test_key")
    public static class TestMissingFeature {
    }

    @OptimizelyFeature(name = "test_feature", userIdKey = "test_key")
    public static class TestFeatureMissingVariable {
        @OptimizelyVariable(name = "test_missing_variable")
        public String testMissingVariable;
    }

    @OptimizelyFeature(name = "test_missing_feature", userIdKey = "test_key", validate = false)
    public static class TestFeatureMissingDoNotValidate {
    }

}
