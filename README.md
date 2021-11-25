gRPC Calculator example
==============================================

The examples require `grpc-java` to already be built. You are strongly encouraged
to check out a git release tag, since there will already be a build of gRPC
available. Otherwise you must follow [COMPILING](../COMPILING.md).

You may want to read through the
[Quick Start](https://grpc.io/docs/languages/java/quickstart)
before trying out the examples.

### <a name="to-build-the-examples"></a> To build the calculator example

1. **[Install gRPC Java library SNAPSHOT locally, including code generation plugin](../COMPILING.md) (Only need this step for non-released versions, e.g. master HEAD).**

2. From grpc-java/examples directory:
```
$ ./gradlew installDist
```

This creates the scripts `calculator-server`, `calculator-client`,
`route-guide-server`, `route-guide-client`, etc. in the
`build/install/examples/bin/` directory that run the examples. Each
example requires the server to be running before starting the client.

For example, to try the calculator example first run:

```
$ ./build/install/examples/bin/calculator-server
```

And in a different terminal window run:

```
$ ./build/install/examples/bin/calculator-client
```

That's it!

For more information, refer to gRPC Java's [README](../README.md) and
[tutorial](https://grpc.io/docs/languages/java/basics).

### Maven

If you prefer to use Maven:
1. **[Install gRPC Java library SNAPSHOT locally, including code generation plugin](../COMPILING.md) (Only need this step for non-released versions, e.g. master HEAD).**

2. Run in this directory:
```
$ mvn verify
$ # Run the server
$ mvn exec:java -Dexec.mainClass=io.grpc.examples.calculator.CalculatorServer
$ # In another terminal run the client
$ mvn exec:java -Dexec.mainClass=io.grpc.examples.calculator.CalculatorClient
```

### Bazel

If you prefer to use Bazel:
```
$ bazel build :calculator-server :calculator-client
$ # Run the server
$ bazel-bin/calculator-server
$ # In another terminal run the client
$ bazel-bin/calculator-client
```
