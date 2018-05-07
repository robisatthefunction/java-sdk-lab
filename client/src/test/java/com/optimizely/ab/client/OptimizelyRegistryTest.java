package com.optimizely.ab.client;

import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by mdavis on 4/2/18.
 */
public class OptimizelyRegistryTest {

    private OptimizelyRegistry optimizelyRegistry;

    @Before
    public void setUp() throws Exception {
        optimizelyRegistry = new OptimizelyRegistry();
        optimizelyRegistry.register(TestFeature.class);
        optimizelyRegistry.register(TestInterface.class);
        optimizelyRegistry.register(TestInterface.TestVariation1.class);
    }

    @Test
    public void register() throws Exception {
        // Registering again is ok.
        optimizelyRegistry.register(TestFeature.class);
    }

    @Test
    public void getItems() throws Exception {
        Set<Class> actual = optimizelyRegistry.getItems();
        assertEquals(3, actual.size());
        assertTrue(actual.contains(TestFeature.class));
        assertTrue(actual.contains(TestInterface.class));
        assertTrue(actual.contains(TestInterface.TestVariation1.class));
    }

    @Test
    public void get() throws Exception {
        FeatureProcessor<TestFeature> featureProcessor = optimizelyRegistry.get(TestFeature.class);
        assertNotNull(featureProcessor);
    }

    @Test
    public void getVariation() throws Exception {
        FeatureProcessor<TestInterface> featureProcessor = optimizelyRegistry.get(TestInterface.class, "variation_1");
        assertNotNull(featureProcessor);
    }

    @Test
    public void testAutoRegistration() throws Exception {
        OptimizelyRegistry optimizelyRegistry = OptimizelyRegistry.get();

        FeatureProcessor<TestFeature> featureProcessor = optimizelyRegistry.get(TestFeature.class);
        assertNotNull(featureProcessor);
    }
}
