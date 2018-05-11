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

import static com.optimizely.ab.client.FeatureProcessorTest.TestEnum.VALUE_1;

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

    @OptimizelyVariable(name = "test_string")
    public String testString = "default";

    @OptimizelyVariable(name = "test_double")
    public Double testDouble = 0.01;

    @OptimizelyVariable(name = "test_boolean")
    public Boolean testBoolean = true;

    @OptimizelyVariable(name = "test_integer")
    public Integer testInteger = 10;

    @OptimizelyVariable(name = "test_enum")
    public FeatureProcessorTest.TestEnum testEnum = VALUE_1;
}
