package com.optimizely.ab.client;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.optimizely.ab.Optimizely;
import com.optimizely.ab.annotations.OptimizelyFeature;
import com.optimizely.ab.annotations.OptimizelyVariable;
import com.optimizely.ab.config.parser.ConfigParseException;
import com.optimizely.ab.event.EventHandler;
import com.optimizely.ab.event.LogEvent;

import java.io.IOException;
import java.net.URL;

/**
 * Created by mdavis on 4/2/18.
 */
@OptimizelyFeature(name = "test_feature", userIdKey = "test_key")
public class TestFeature {

    /**
     * Utility for building a test instance of Optimizely
     */
    public static Optimizely getTestOptimizely() throws ConfigParseException, IOException {
        URL url = Resources.getResource("optimizely_datafile.json");
        String json = Resources.toString(url, Charsets.UTF_8);
        return Optimizely.builder(json, new EventHandler() {
            @Override
            public void dispatchEvent(LogEvent logEvent) throws Exception {
                // non-op
            }
        }).build();
    }

    @OptimizelyVariable(name = "test_string_no_default")
    public String testStringWithoutDefault;

    @OptimizelyVariable(name = "test_string", defaultValue = "default")
    public String testString;

    @OptimizelyVariable(name = "test_double", defaultValue = "0.01")
    public Double testDouble;

    @OptimizelyVariable(name = "test_double_no_default")
    public Double testDoubleWithoutDefault;

    @OptimizelyVariable(name = "test_boolean", defaultValue = "true")
    public Boolean testBoolean;

    @OptimizelyVariable(name = "test_boolean_no_default")
    public Boolean testBooleanWithoutDefault;

    @OptimizelyVariable(name = "test_integer", defaultValue = "10")
    public Integer testInteger;

    @OptimizelyVariable(name = "test_integer_no_default")
    public Integer testIntegerWithoutDefault;

    @OptimizelyVariable(name = "test_enum", defaultValue = "VALUE_1")
    public FeatureProcessorTest.TestEnum testEnum;

    @OptimizelyVariable(name = "test_enum_no_default")
    public FeatureProcessorTest.TestEnum testEnumWithoutDefault;
}
