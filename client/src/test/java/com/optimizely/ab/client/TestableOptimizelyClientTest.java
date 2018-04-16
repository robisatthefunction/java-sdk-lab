package com.optimizely.ab.client;

import com.optimizely.ab.config.Variation;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by mdavis on 4/6/18.
 */
public class TestableOptimizelyClientTest {

    private TestableOptimizelyClient optimizelyClient;

    @Before
    public void setUp() throws Exception {
        optimizelyClient = new TestableOptimizelyClient();
    }

    @Test
    public void testActivate() throws Exception {
        optimizelyClient.activate("name1", "key1");
        optimizelyClient.activate("name2", "key2");

        List<String> actual = optimizelyClient.getActivations();
        assertEquals(2, actual.size());
        assertEquals("name1", actual.get(0));
        assertEquals("name2", actual.get(1));
    }

    @Test
    public void testTrack() throws Exception {
        optimizelyClient.track("event1");
        optimizelyClient.track("event2");

        List<String> actual = optimizelyClient.getEvents();
        assertEquals(2, actual.size());
        assertEquals("event1", actual.get(0));
        assertEquals("event2", actual.get(1));
    }

    @Test
    public void trackRevenue() throws Exception {
        optimizelyClient.track("event1", 10L);
        optimizelyClient.track("event2", 20L);

        List<Long> actual = optimizelyClient.getRevenues();
        assertEquals(2, actual.size());
        assertEquals(10L, actual.get(0).longValue());
        assertEquals(20L, actual.get(1).longValue());
    }

    @Test
    public void trackValue() throws Exception {
        optimizelyClient.track("event1", 10.0);
        optimizelyClient.track("event2", 20.0);

        List<Double> actual = optimizelyClient.getValues();
        assertEquals(2, actual.size());
        assertEquals(10.0, actual.get(0).longValue(), 0.001);
        assertEquals(20.0, actual.get(1).longValue(), 0.001);
    }

    @Test
    public void getFeature() throws Exception {
        TestFeature actual = optimizelyClient.getFeature(TestFeature.class);
        assertNotNull(actual);
    }

    @Test
    public void isFeatureEnabled() throws Exception {
        assertFalse(optimizelyClient.isFeatureEnabled("feature", "key"));
        optimizelyClient.enableFeature("feature");
        assertTrue(optimizelyClient.isFeatureEnabled("feature", "key"));

        List<String> actual = optimizelyClient.getActivations();
        assertEquals(2, actual.size());
        assertEquals("feature", actual.get(0));
        assertEquals("feature", actual.get(1));
    }

    @Test
    public void getFeatureVariable() throws Exception {
        // TODO make this work
        assertNull(optimizelyClient.getFeatureVariable("feature", "variable", "user", Long.class));
    }

    @Test
    public void setVariation() throws Exception {
        Variation expected = new Variation("1", "var");
        optimizelyClient.setVariation("experiment1", expected);
        Variation actual = optimizelyClient.activate("experiment1", "key1");
        assertEquals(expected, actual);
    }

    @Test
    public void testReset() throws Exception {
        optimizelyClient.setVariation("test", new Variation("test","test"));
        assertNotNull(optimizelyClient.activate("test", "test"));

        optimizelyClient.track("test");
        optimizelyClient.track("test", 1L);
        optimizelyClient.track("test", 1.0);

        assertFalse(optimizelyClient.getActivations().isEmpty());
        assertFalse(optimizelyClient.getEvents().isEmpty());
        assertFalse(optimizelyClient.getRevenues().isEmpty());
        assertFalse(optimizelyClient.getValues().isEmpty());

        optimizelyClient.reset();

        assertTrue(optimizelyClient.getActivations().isEmpty());
        assertTrue(optimizelyClient.getEvents().isEmpty());
        assertTrue(optimizelyClient.getRevenues().isEmpty());
        assertTrue(optimizelyClient.getValues().isEmpty());

        assertNull(optimizelyClient.activate("test", "test"));
    }
}
