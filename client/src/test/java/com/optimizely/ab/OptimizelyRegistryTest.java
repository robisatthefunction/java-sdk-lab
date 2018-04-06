package com.optimizely.ab;

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
    }

    @Test
    public void register() throws Exception {
        // Registering again is ok.
        optimizelyRegistry.register(TestFeature.class);
    }

    @Test
    public void getItems() throws Exception {
        Set<Class> actual = optimizelyRegistry.getItems();
        assertEquals(1, actual.size());
        assertTrue(actual.contains(TestFeature.class));
    }

    @Test
    public void get() throws Exception {
        FeatureProcessor<TestFeature> featureProcessor = optimizelyRegistry.get(TestFeature.class);
        assertNotNull(featureProcessor);
    }

}
