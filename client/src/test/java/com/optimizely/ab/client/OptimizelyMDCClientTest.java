package com.optimizely.ab.client;

import com.optimizely.ab.Optimizely;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.MDC;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.optimizely.ab.client.OptimizelyClient.OPTIMIZELY_END_USER_ID_KEY;
import static com.optimizely.ab.client.OptimizelyMDCClient.USER_IDS_KEY;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * @since 7/28/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class OptimizelyMDCClientTest {

    private static final String END_USER_ID = "endUserId";
    private static final String EXPERIMENT_KEY = "TEST_EXPERIMENT";
    private static final String FEATURE_FLAG_KEY = "TEST_FEATURE_FLAG";
    private static final String FEATURE_VARIABLE_KEY = "TEST_FEATURE_VARIABLE";

    @Mock
    private Optimizely optimizely;

    private OptimizelyClient optimizelyClient;
    private Map<String, String> expectedAttributes;

    @Before
    public void setUp() throws Exception {
        MDC.clear();
        MDC.put(OPTIMIZELY_END_USER_ID_KEY, END_USER_ID);
        MDC.put("RANDOM_ATTRIBUTE", "RANDOM_VALUE");

        expectedAttributes = new HashMap<>();
        expectedAttributes.put(OPTIMIZELY_END_USER_ID_KEY, END_USER_ID);
        expectedAttributes.put(USER_IDS_KEY, END_USER_ID);
        expectedAttributes.put("RANDOM_ATTRIBUTE", "RANDOM_VALUE");

        OptimizelyRegistry registry = new OptimizelyRegistry();
        optimizelyClient = new OptimizelyMDCClient(optimizely, registry);
    }

    @After
    public void tearDown() {
        MDC.clear();
    }

    @Test
    public void testActivate() {
        optimizelyClient.activate(EXPERIMENT_KEY, OPTIMIZELY_END_USER_ID_KEY);
        Mockito.verify(optimizely, Mockito.only()).activate(EXPERIMENT_KEY, END_USER_ID, expectedAttributes);
    }

    @Test
    public void testDoesNotTrackTrack() {
        optimizelyClient.track("MY_REVENUE_EVENT");
        Mockito.verify(optimizely, Mockito.never()).track(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testTrack() {
        optimizelyClient.activate(EXPERIMENT_KEY, OPTIMIZELY_END_USER_ID_KEY);
        optimizelyClient.track("MY_REVENUE_EVENT");

        Map<String, Long> tags = Collections.emptyMap();
        Mockito.verify(optimizely, Mockito.times(1)).track("MY_REVENUE_EVENT", END_USER_ID, expectedAttributes, tags);
    }

    @Test
    public void testTrackRevenue() {
        optimizelyClient.activate(EXPERIMENT_KEY, OPTIMIZELY_END_USER_ID_KEY);
        optimizelyClient.track("MY_REVENUE_EVENT", 100L);

        HashMap<String, Long> tags = new HashMap<>();
        tags.put("revenue", 100L);
        Mockito.verify(optimizely, Mockito.times(1)).track("MY_REVENUE_EVENT", END_USER_ID, expectedAttributes, tags);
    }

    @Test
    public void testTrackValue() {
        optimizelyClient.activate(EXPERIMENT_KEY, OPTIMIZELY_END_USER_ID_KEY);
        optimizelyClient.track("MY_VALUE_EVENT", 100.0);

        HashMap<String, Double> tags = new HashMap<>();
        tags.put("value", 100.0);
        Mockito.verify(optimizely, Mockito.times(1)).track("MY_VALUE_EVENT", END_USER_ID, expectedAttributes, tags);
    }

    @Test
    public void testIsFeatureEnabled() {
        optimizelyClient.isFeatureEnabled(FEATURE_FLAG_KEY, OPTIMIZELY_END_USER_ID_KEY);
        Mockito.verify(optimizely, Mockito.only()).isFeatureEnabled(FEATURE_FLAG_KEY, END_USER_ID, expectedAttributes);
    }

    @Test
    public void testGetFeatureVariableString() {
        optimizelyClient.getFeatureVariable(FEATURE_FLAG_KEY, FEATURE_VARIABLE_KEY, OPTIMIZELY_END_USER_ID_KEY, String.class);
        Mockito.verify(optimizely, Mockito.only()).getFeatureVariableString(FEATURE_FLAG_KEY, FEATURE_VARIABLE_KEY, END_USER_ID, expectedAttributes);
    }

    @Test
    public void testGetFeatureVariableDouble() {
        optimizelyClient.getFeatureVariable(FEATURE_FLAG_KEY, FEATURE_VARIABLE_KEY, OPTIMIZELY_END_USER_ID_KEY, Double.class);
        Mockito.verify(optimizely, Mockito.only()).getFeatureVariableDouble(FEATURE_FLAG_KEY, FEATURE_VARIABLE_KEY, END_USER_ID, expectedAttributes);
    }

    @Test
    public void testGetFeatureVariableInteger() {
        optimizelyClient.getFeatureVariable(FEATURE_FLAG_KEY, FEATURE_VARIABLE_KEY, OPTIMIZELY_END_USER_ID_KEY, Integer.class);
        Mockito.verify(optimizely, Mockito.only()).getFeatureVariableInteger(FEATURE_FLAG_KEY, FEATURE_VARIABLE_KEY, END_USER_ID, expectedAttributes);
    }

    @Test
    public void testGetFeatureVariableBoolean() {
        optimizelyClient.getFeatureVariable(FEATURE_FLAG_KEY, FEATURE_VARIABLE_KEY, OPTIMIZELY_END_USER_ID_KEY, Boolean.class);
        Mockito.verify(optimizely, Mockito.only()).getFeatureVariableBoolean(FEATURE_FLAG_KEY, FEATURE_VARIABLE_KEY, END_USER_ID, expectedAttributes);
    }

    @Test
    public void testGetFeatureVariableEnumPositive() {
        Mockito.when(optimizely.getFeatureVariableString(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Matchers.any())).thenReturn("VALID");
        TestEnum actual = optimizelyClient.getFeatureVariable(FEATURE_FLAG_KEY, FEATURE_VARIABLE_KEY, OPTIMIZELY_END_USER_ID_KEY, TestEnum.class);
        Assert.assertEquals("cache mode returned does not match", TestEnum.VALID, actual);
    }

    @Test
    public void testGetFeatureVariableEnumNegative() {
        Mockito.when(optimizely.getFeatureVariableString(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Matchers.any())).thenReturn("NOT_VALID");
        TestEnum actual = optimizelyClient.getFeatureVariable(FEATURE_FLAG_KEY, FEATURE_VARIABLE_KEY, OPTIMIZELY_END_USER_ID_KEY, TestEnum.class);
        Assert.assertNull(actual);
    }

    @Test
    public void testGetFeatureVariableEnumNullReturnedFromSDK() {
        Mockito.when(optimizely.getFeatureVariableString(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Matchers.any())).thenReturn(null);
        TestEnum actual = optimizelyClient.getFeatureVariable(FEATURE_FLAG_KEY, FEATURE_VARIABLE_KEY, OPTIMIZELY_END_USER_ID_KEY, TestEnum.class);
        assertNull(actual);
    }

    @Test
    public void testMultipleBucketingIds() {
        // TODO - Dont forget me
    }

    @Test
    public void testGetFeature() throws Exception {
        Optimizely optimizely = TestFeature.getTestOptimizely();
        OptimizelyMDCClient optimizelyMDCClient = new OptimizelyMDCClient(optimizely, new OptimizelyRegistry());

        // Test unregistered feature.
        assertNotNull(optimizelyMDCClient.getFeature(TestFeature.class));

        // Test unregistered non-feature.
        assertNotNull(optimizelyMDCClient.getFeature(NonFeatureDefaultConstructor.class));

        // Test unregistered non-feature with InstantiationException.
        assertNull(optimizelyMDCClient.getFeature(Long.class));

        // Test unregistered non-feature with no IllegalAccessException.
        assertNull(optimizelyMDCClient.getFeature(PrivateNonFeature.class));
    }

    public static class NonFeatureDefaultConstructor {
        // Public will succeed in instantiating from OptimizelyClient.
    }

    private static class PrivateNonFeature {
        // Private. will throw IllegalAccessException when attempting to instantiate from OptimizelyClient.
    }

    public enum TestEnum {
        VALID
    }

    @Test
    public void testNullOptimizely() {
        OptimizelyRegistry registry = new OptimizelyRegistry();
        optimizelyClient = new OptimizelyMDCClient(null, registry);

        MDC.put(USER_IDS_KEY, END_USER_ID);

        assertNull(optimizelyClient.activate("test", OPTIMIZELY_END_USER_ID_KEY));
        optimizelyClient.track("test", new HashMap<>());
        assertFalse(optimizelyClient.isFeatureEnabled("test", OPTIMIZELY_END_USER_ID_KEY));

        assertNull(optimizelyClient.getFeatureVariable(null, null, null, null));
    }
}
