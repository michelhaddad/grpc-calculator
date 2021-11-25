package io.grpc.examples.calculator;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CalculatorServer {
    private static final Logger logger = Logger.getLogger(CalculatorServer.class.getName());

    private Server server;

    private void start() throws IOException {
        /* The port on which the server should run */
        int port = 50051;
        server = ServerBuilder.forPort(port)
                .addService(new CalculatorServer.CalculatorImpl())
                .build()
                .start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    CalculatorServer.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        final CalculatorServer server = new CalculatorServer();
        server.start();
        server.blockUntilShutdown();
    }

    static class CalculatorImpl extends CalculatorServiceGrpc.CalculatorServiceImplBase {

        @Override
        public void add(NumberCouple request, StreamObserver<Number> responseObserver) {
            Number reply = Number.newBuilder().setValue(request.getNum1().getValue() + request.getNum2().getValue()).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void sub(NumberCouple request, StreamObserver<Number> responseObserver) {
            Number reply = Number.newBuilder().setValue(request.getNum1().getValue() - request.getNum2().getValue()).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void mul(NumberCouple request, StreamObserver<Number> responseObserver) {
            Number reply = Number.newBuilder().setValue(request.getNum1().getValue() * request.getNum2().getValue()).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void div(NumberCouple request, StreamObserver<Number> responseObserver) {
            Number reply = Number.newBuilder().setValue(request.getNum1().getValue() / request.getNum2().getValue()).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public StreamObserver<Number> addAsync(final StreamObserver<Number> responseObserver) {
            return new StreamObserver<Number>() {
                double sum;
                @Override
                public void onNext(Number number) {
                    sum += number.getValue();
                    responseObserver.onNext(newNumber(sum));
                }

                @Override
                public void onError(Throwable t) {
                    logger.log(Level.WARNING, "addAsync cancelled");
                }

                @Override
                public void onCompleted() {
                    responseObserver.onCompleted();
                }
            };
        }

        private Number newNumber(double a) {
            return Number.newBuilder().setValue(a).build();
        }
    }
}
