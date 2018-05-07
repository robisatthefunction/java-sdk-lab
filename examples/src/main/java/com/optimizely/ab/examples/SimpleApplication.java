package com.optimizely.ab.examples;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.optimizely.ab.Optimizely;
import com.optimizely.ab.client.*;
import com.optimizely.ab.event.EventHandler;
import org.apache.commons.lang3.mutable.MutableLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Simple application that takes a datafile name as input, then prints the contents of
 * the {@link SimpleFeature}.
 */
public class SimpleApplication {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleApplication.class);
    private final Map<SimpleFeature, MutableLong> distribution = new HashMap<>();

    private void run(int numIterations) {

        LOG.info("Fetching Optimizely Client...");
        OptimizelyClient optimizelyClient = OptimizelyClientFactory.getClient();

        LOG.info("Starting simulation...");

        for (int i = 0; i < numIterations; i++) {
            MDC.clear();
            MDC.put("user_key", UUID.randomUUID().toString());
            SimpleFeature feature = optimizelyClient.getFeature(SimpleFeature.class);

            MutableLong mutableLong = distribution.computeIfAbsent(feature, k -> new MutableLong());
            mutableLong.increment();
        }

        LOG.info("Done...");

        for (Map.Entry<SimpleFeature, MutableLong> entry: distribution.entrySet()) {
            LOG.info("Feature: " + entry.getKey() + ", Count: " + entry.getValue());
        }
    }

    public static void main(String[] args) throws Exception {
        URL url = Resources.getResource("experiment_datafile.json");
        String datafile = Resources.toString(url, Charsets.UTF_8);

        EventHandler eventHandler = logEvent -> LOG.debug(logEvent.toString());
        Optimizely optimizely = Optimizely.builder(datafile, eventHandler).build();

        OptimizelyRegistry registry = new OptimizelyRegistry();
        registry.register(SimpleFeature.class);

        OptimizelyValidator validator = new OptimizelyValidator(registry);

        if (!validator.validate(optimizely)) {
            throw new RuntimeException("Datafile failed validation!");
        }

        OptimizelyClient client = new OptimizelyMDCClient(optimizely, registry);
        OptimizelyClientFactory.setProvider(() -> client);

        SimpleApplication sampleApplication = new SimpleApplication();
        sampleApplication.run(10000);
    }
}
