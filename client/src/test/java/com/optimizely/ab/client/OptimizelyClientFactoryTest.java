package com.optimizely.ab.client;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by mdavis on 4/6/18.
 */
public class OptimizelyClientFactoryTest {

    @After
    public void tearDown() {
        OptimizelyClientFactory.setProvider(null);
    }

    @Test
    public void getClient() throws Exception {
        OptimizelyClient optimizelyClient = OptimizelyClientFactory.getClient();
        assertNotNull(optimizelyClient);
    }

    @Test
    public void setProvider() throws Exception {
        OptimizelyClientFactory.setProvider(new OptimizelyClientProvider() {
            @Override
            public OptimizelyClient get() {
                OptimizelyClient optimizelyClient = new TestableOptimizelyClient();
                return optimizelyClient;
            }
        });

        OptimizelyClient optimizelyClient = OptimizelyClientFactory.getClient();

        assertNotNull(optimizelyClient);
        assertEquals(TestableOptimizelyClient.class, optimizelyClient.getClass());
    }
}
