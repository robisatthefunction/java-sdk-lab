# Client

The `client` package provides a wrapper around the [base Java SDK](github.com/optimizely/java-sdk) to make
it easier, faster, and safer to develop Optimizely experiments.

Benefits:

*   Scoped `activate` and `track` methods that resolve the User IDs behind-the-scenes.
*   Generic types for Feature Variables, including support for Enums.
*   Annotations to model Feature as plain Java class and the ability to get
    instances with values for Feature Variables automatically injected for the current user scope.
*   Datafile validation to ensure it is compatible with the code that consumes it.

## Example

Let's first define a class for a Feature that will have its Feature Variables injected into it:

```java
@OptimizelyFeature(name = "example_feature", userIdKey = "user_key")
public class ExampleFeature {

    @OptimizelyVariable(name = "example_variable")
    public String exampleString;

    @Override
    public String toString() {
        return exampleString;
    }
}

```

Next, we need to provide the client with `FeatureProcessor` for our class via an `OptimizelyRegistry`.
Classes annotated with `@OptimizelyFeature` on the classpath are automatically discovered by the registry
using reflection.

```java
OptimizelyRegistry optimizelyRegistry = OptimizelyRegistry.get();

```

The last step to obtaining instances of `ExampleFeature` is creating an `OptimizelyClient` object.

We'll use the `OptimizelyMDCClient` to leverage the logging MDC (Mapped Dynamic Context) to manage state of
user ID(s) scoped to the current thread. The `OptimizelyClient` interface could be implemented with a different
storage mechanism.

```java
OptimizelyClient optimizelyClient = new OptimizelyMDCClient(optimizely, optimizelyRegistry);

```

Since we want to promote "Experimentation Everywhere", we want to make Optimizely available in every part
of our codebase and with as little friction as possible. To achieve this we've implemented a static
factory class `OptimizelyClientFactory`. By default, the factory will return a non-op `OptimizelyClient` that
returns default implementation of OptimizelyFeatures.

At runtime, however, an implementor can utilize the static setProvider
method to have the factory provide the required `OptimizelyClient` implementation for their use case.
For our production applications we set the `OptimizelyMDCProvider` class, but for non-production or test environments
a different implementation may be more appropriate. Recognizing this need we have the `TestableOptimizelyClient`
that we use in the majority of our unit tests as alternative to mocked dependencies.

Putting this altogether we can create a simple program.


```java
public class SampleApplication {

    private final ExampleFeature feature;

    public SampleApplication() {
        OptimizelyClient optimizelyClient = OptimizelyClientFactory.getClient();
        feature = optimizelyClient.getFeature(ExampleFeature.class);
    }

    public String whichFeature() {
        return feature.toString();
    }

    public static void main(String[] args) throws Exception {
        String datafile = args[0];

        EventHandler eventHandler = System.out::println;
        Optimizely optimizely = Optimizely.builder(datafile, eventHandler).build();

        OptimizelyRegistry registry = OptimizelyRegistry.get();
        OptimizelyValidator validator = new OptimizelyValidator(registry);

        if (!validator.validate(optimizely)) {
            System.out.println("Datafile failed validation!");
            System.exit(1);
        }

        OptimizelyClient client = new OptimizelyMDCClient(optimizely, registry);
        OptimizelyClientFactory.setProvider(() -> client);

        SampleApplication sampleApplication = new SampleApplication();
        System.out.println(sampleApplication.whichFeature());
    }
}
```

## Context

One of the difficulties in using the core SDK is that it's up to the implementer to provide context
to the SDK with each and every api call. This means explicitly passing the userId and attributes. This
puts a lot of burden on the implementer to pass this state between class invocations or method calls
in order to properly track and bucket the user. After adopting the SDK into our backend stack we realised
that the state management we wanted closely mirrored the state, or context that we wanted in our logging
framework. With that in mind we looked to the MDC (Mapped Dynamic Context) we were already leveraging
for verbose logging and tied into that for our OptimizelyClient implementation.

With the OptimizelyMDCClient implementation all MDC key values are passed with each and every call to the
SDK as attributes. This makes those values available as targeting conditions for bucketing users into
experiemnts as well as segmentation qualifiers for results.

## Features

Features are a core building block of the Optimizely fullstack product and provide the basis
for rollouts and experimentation. A feature can be as simple as boolean value, or as complex as a fork in business logic.

This package provides three annotations for managing features and feature variables within your application.
The purpose of these annotations is to provide an explicit, declarative linking between the entities
setup within Optimizely Platform and their POJO counterparts. 

## Registry

Reflection is powerful and expensive operation. As such, we want to use it as infrequently as possible within
normal code execution. The `OptimizelyRegistry` class provides a static factory method which builds the registry once
via reflection, scanning for all `@OptimizelyFeature` classes and `@OptimizelyVariable` attributes to build the set
of api keys needed to access the core SDK. It's recommended that this registry be built once and shared throughout
the call stack.

Advanced users can choose to explicitly register classes. This is most useful in unit tests where more control over the
registry is preferable.

## Validation

"With great power, comes great responsibility." -- wise person

Optimizely allows users to quickly and easily define new features and variables to be used within
their application code. The downside to this flexibility, is that any code dependant on those api keys and definitions
are vulnerable to human error and misconfiguration.

Since we've been bitten by this ourselves, the first line of defence that we've implemented is a
schema validator. Leveraging the `OptimizelyRegistry`, which scans the classpath for all classes annotated as 
`@OptimizelyFeatures` and parses any associated attributes annotated as an `@OptimizelyVariable`, we assert that those 
definitions exist as part of the provided `Optimizely` instance.

## Future Enhancements

Hopefully this project will continue to be in active development. Here are just a few enhancements
we'd like to implement in the near future.
 
* Generate Optimizely entities from annotated classes.
* Pluggable validators.
