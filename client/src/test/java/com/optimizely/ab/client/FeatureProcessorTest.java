package com.optimizely.ab.client;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

/**
 * Created by mdavis on 3/12/18.
 */
@RunWith(MockitoJUnitRunner.class)
public class FeatureProcessorTest {

    @Mock
    private OptimizelyClient optimizelyClient;

    private FeatureProcessor<TestFeature> featureProcessor;

    @Before
    public void setUp() throws Exception {
        Mockito.when(optimizelyClient.isFeatureEnabled("test_feature", "test_key")).thenReturn(true);
        featureProcessor = new FeatureProcessor<>(TestFeature.class);
    }

    @Test
    public void testGetInstanceSimple() {
        TestFeature actual = featureProcessor.newInstance(optimizelyClient);
        assertNotNull(actual);
    }

    @Test
    public void testGetInstanceWithVariableDefaults() {
        TestFeature actual = featureProcessor.newInstance(optimizelyClient);
        assertNotNull(actual);

        assertNull(actual.testDoubleWithoutDefault);
        assertEquals(0.01, actual.testDouble, 0.0001);

        assertNull(actual.testBooleanWithoutDefault);
        assertEquals(Boolean.TRUE, actual.testBoolean);

        assertNull(actual.testIntegerWithoutDefault);
        assertEquals(10, actual.testInteger.intValue());

        assertNull(actual.testStringWithoutDefault);
        assertEquals("default", actual.testString);

        assertNull(actual.testEnumWithoutDefault);
        assertEquals(TestEnum.VALUE_1, actual.testEnum);
    }

    @Test
    public void testGetInstanceWithVariableNonDefaults() {
        Mockito.when(optimizelyClient.getFeatureVariable("test_feature", "test_string", "test_key", String.class)).thenReturn("non-default");
        Mockito.when(optimizelyClient.getFeatureVariable("test_feature", "test_double", "test_key", Double.class)).thenReturn(0.02);
        Mockito.when(optimizelyClient.getFeatureVariable("test_feature", "test_boolean", "test_key", Boolean.class)).thenReturn(false);
        Mockito.when(optimizelyClient.getFeatureVariable("test_feature", "test_integer", "test_key", Integer.class)).thenReturn(20);
        Mockito.when(optimizelyClient.getFeatureVariable("test_feature", "test_enum", "test_key", TestEnum.class)).thenReturn(TestEnum.VALUE_2);

        TestFeature actual = featureProcessor.newInstance(optimizelyClient);
        assertNotNull(actual);

        assertEquals("non-default", actual.testString);
        assertEquals(0.02, actual.testDouble, 0.0001);
        assertEquals(Boolean.FALSE, actual.testBoolean);
        assertEquals(20, actual.testInteger.intValue());
        assertEquals(TestEnum.VALUE_2, actual.testEnum);
    }

    public enum TestEnum {
        VALUE_1, VALUE_2
    }
}
