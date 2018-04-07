package com.optimizely.ab.client;

import com.optimizely.ab.config.Variation;

import java.util.Map;

/**
 * Factory method designed to wrap an OptimizelyProvider.
 *
 * Only a single OptimizelyProvider should be active for a given application.
 *
 * By default we provide a pass-thru interface that is basically non-op. But the provider
 * can be overwritten at run time.
 *
 * If test specific behavior is required consider using the {@link TestableOptimizelyClient}
 *
 */
public class OptimizelyClientFactory {

    private static final OptimizelyClientProvider defaultProvider = new DefaultProvider();

    private static OptimizelyClientProvider overrideProvider;


    public static OptimizelyClient getClient() {
        return overrideProvider == null ? defaultProvider.get() : overrideProvider.get();
    }

    public static void setProvider(OptimizelyClientProvider optimizelyClientProvider) {
        overrideProvider = optimizelyClientProvider;
    }

    private static class DefaultProvider implements OptimizelyClientProvider {

        @Override
        public OptimizelyClient get() {
            return new EmptyClient();
        }
    }

    private static class EmptyClient implements OptimizelyClient {

        @Override
        public Variation activate(String experimentKey, String userIdKey) {
            return null;
        }

        @Override
        public void track(String eventName) {

        }

        @Override
        public void track(String eventName, Map<String, ?> eventTags) {

        }

        @Override
        public void track(String eventName, Long revenue) {

        }

        @Override
        public void track(String eventName, Double value) {

        }

        @Override
        public <T> T getFeature(Class<T> clazz) {
            FeatureProcessor<T> processor = new FeatureProcessor<>(clazz);
            return processor.newInstance(this);
        }

        @Override
        public boolean isFeatureEnabled(String featureFlagKey, String userIdKey) {
            return false;
        }

        @Override
        public <T> T getFeatureVariable(String featureFlagKey, String variableKey, String userIdKey, Class<T> clazz) {
            return null;
        }
    }
}
