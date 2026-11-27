package com.sportflow.live.client;

import com.sportflow.live.grpc.LiveCompetitionServiceGrpc;
import com.sportflow.live.grpc.LiveEvent;
import com.sportflow.live.grpc.SubscribeRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Standalone Java test client that subscribes to the live stream and prints events.
 */
public class LiveStreamClient {

    public static void main(String[] args) throws InterruptedException {
        String host = args.length > 0 ? args[0] : "localhost";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 9097;
        String competitionId = args.length > 2 ? args[2] : "comp-sim-100";

        System.out.printf("Connecting to Live Service at %s:%d for competition '%s'...%n", host, port, competitionId);

        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        LiveCompetitionServiceGrpc.LiveCompetitionServiceStub stub =
                LiveCompetitionServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        SubscribeRequest request = SubscribeRequest.newBuilder()
                .setCompetitionId(competitionId)
                .build();

        stub.subscribe(request, new StreamObserver<LiveEvent>() {
            @Override
            public void onNext(LiveEvent event) {
                System.out.printf("[EVENT STREAM] Comp: %s | Type: %-12s | %s (%d) vs %s (%d) | ts: %d%n",
                        event.getCompetitionId(),
                        event.getType(),
                        event.getAthleteA(),
                        event.getScoreA(),
                        event.getAthleteB(),
                        event.getScoreB(),
                        event.getTimestamp());
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("[STREAM ERROR] " + t.getMessage());
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("[STREAM COMPLETED] Match END received. Stream closed by server.");
                latch.countDown();
            }
        });

        // Wait up to 5 minutes or until match ends
        latch.await(5, TimeUnit.MINUTES);
        channel.shutdownNow();
    }
}
