package io.grpc.examples.calculator;

import io.grpc.*;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CalculatorClient {
    private static final Logger logger = Logger.getLogger(CalculatorClient.class.getName());

    private final CalculatorServiceGrpc.CalculatorServiceBlockingStub blockingStub;
    private final CalculatorServiceGrpc.CalculatorServiceStub asyncStub;

    /** Construct client for accessing HelloWorld server using the existing channel. */
    public CalculatorClient(Channel channel) {
        // 'channel' here is a Channel, not a ManagedChannel, so it is not this code's responsibility to
        // shut it down.

        // Passing Channels to code makes code easier to test and makes it easier to reuse Channels.
        blockingStub = CalculatorServiceGrpc.newBlockingStub(channel);
        asyncStub = CalculatorServiceGrpc.newStub(channel);
    }

    public void add(double a, double b) {
        System.out.println("Adding " + a + " and " + b);
        Number num1 = newNumber(a);
        Number num2 = newNumber(b);
        NumberCouple nc = NumberCouple.newBuilder().setNum1(num1).setNum2(num2).build();
        Number response;
        try {
            response = blockingStub.add(nc);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        System.out.println("Result = " + response.getValue() + "\n");
    }

    public void sub(double a, double b) {
        System.out.println("Substracting " + a + " and " + b);
        Number num1 = newNumber(a);
        Number num2 = newNumber(b);
        NumberCouple nc = NumberCouple.newBuilder().setNum1(num1).setNum2(num2).build();
        Number response;
        try {
            response = blockingStub.sub(nc);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        System.out.println("Result = " + response.getValue() + "\n");
    }

    public void mul(double a, double b) {
        System.out.println("Multiplying " + a + " and " + b);
        Number num1 = newNumber(a);
        Number num2 = newNumber(b);
        NumberCouple nc = NumberCouple.newBuilder().setNum1(num1).setNum2(num2).build();
        Number response;
        try {
            response = blockingStub.mul(nc);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        System.out.println("Result = " + response.getValue() + "\n");
    }

    public void div(double a, double b) {
        System.out.println("Dividing " + a + " and " + b);
        Number num1 = newNumber(a);
        Number num2 = newNumber(b);
        NumberCouple nc = NumberCouple.newBuilder().setNum1(num1).setNum2(num2).build();
        Number response;
        try {
            response = blockingStub.div(nc);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        System.out.println("Result = " + response.getValue() + "\n");
    }

    public CountDownLatch addAsync() {
        System.out.println("*** Add Async");
        final CountDownLatch finishLatch = new CountDownLatch(1);
        StreamObserver<Number> requestObserver =
                asyncStub.addAsync(new StreamObserver<Number>() {
                    @Override
                    public void onNext(Number number) {
                        System.out.println("Server sent: Sum until now = " + number.getValue());
                    }

                    @Override
                    public void onError(Throwable t) {
                        warning("AddAsync Failed: {0}", Status.fromThrowable(t));
                        finishLatch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        System.out.println("Finished AddAsync");
                        finishLatch.countDown();
                    }
                });

        try {
            List<Number> numbers = new ArrayList<>();
            for (int i = 5; i <= 50; i += 5 ) {
                numbers.add(newNumber(i));
            }

            for (Number number : numbers) {
                System.out.println("Client: Sending " + number.getValue() + " to Server");
                requestObserver.onNext(number);
            }
        } catch (RuntimeException e) {
            // Cancel RPC
            requestObserver.onError(e);
            throw e;
        }
        // Mark the end of requests
        requestObserver.onCompleted();

        // return the latch while receiving happens asynchronously
        return finishLatch;
    }

    private Number newNumber(double a) {
        return Number.newBuilder().setValue(a).build();
    }

    private void warning(String msg, Object... params) {
        logger.log(Level.WARNING, msg, params);
    }

    /**
     * Greet server. If provided, the first element of {@code args} is the name to use in the
     * greeting. The second argument is the target server.
     */
    public static void main(String[] args) throws Exception {
        // Access a service running on the local machine on port 50051
        String target = "localhost:50051";
        // Allow passing in the user and target strings as command line arguments
        if (args.length > 0) {
            if ("--help".equals(args[0])) {
                System.err.println("Usage: No arguments passed");
                System.exit(1);
            }
        }

        // Create a communication channel to the server, known as a Channel. Channels are thread-safe
        // and reusable. It is common to create channels at the beginning of your application and reuse
        // them until the application shuts down.
        ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext()
                .build();
        try {
            CalculatorClient client = new CalculatorClient(channel);
            client.sub(4, 3);
            client.add(4, 5);
            client.mul(3, 6);
            client.div(9, 3);

            // Send and receive some notes.
            CountDownLatch finishLatch = client.addAsync();

            if (!finishLatch.await(1, TimeUnit.MINUTES)) {
                client.warning("addAsync can not finish within 1 minutes");
            }
        } finally {
            // ManagedChannels use resources like threads and TCP connections. To prevent leaking these
            // resources the channel should be shut down when it will no longer be used. If it may be used
            // again leave it running.
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
