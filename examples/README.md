# Examples

The `examples` package provides a few select examples showcasing the use of annotations
and the OptimizelyClient extension.

## Simple Application

This first example is meant to illustrate how to setup a basic experiment using
`@OptimizelyFeature` and `@OptimizelyVariable` annotations in conjunction with the
OptimizelyMDCClient. This application relies on `experiment_datafile.json` found in the
package resources and can be run via `./gradlew example:runSimpleApplication`.

The application iterates over 10000 random UUIDs to determine the proper associated feature
implementation and outputs the number of impressions for each variation demonstrating the
distribution configuration found in the datafile.

## Email Application

This example is used to show how you can annotate an `Interface` and register specific
implementations of that `Interface` with the `@OptimizelyVariation` annotation. The
`@OptimizelyVariation` returned by the `OptimizelyClient` is dependent on the `variationKey`
of the associated `@OptimizelyFeature` which defaults to "variation_key". This application
relies on the email_datafile.json found in the resources package and can be run via
`./gradlew example:runEmailApplication`.