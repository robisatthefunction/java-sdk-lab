package com.optimizely.ab;

/**
 * Simple provider interface for the OptimizelyClient.
 *
 * This allows the implementer to control how much or how little state to carry between invocations.
 *
 * @since 3/12/18.
 */
public interface OptimizelyClientProvider {
    OptimizelyClient get();
}
