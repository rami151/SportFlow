package com.sportflow.live.service;

import com.sportflow.live.grpc.EventType;
import com.sportflow.live.grpc.LiveCompetitionServiceGrpc;
import com.sportflow.live.grpc.LiveEvent;
import com.sportflow.live.grpc.SubscribeRequest;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

@GrpcService
public class LiveGrpcServiceImpl extends LiveCompetitionServiceGrpc.LiveCompetitionServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(LiveGrpcServiceImpl.class);

    @Override
    public void subscribe(SubscribeRequest request, StreamObserver<LiveEvent> responseObserver) {
        log.info("Client subscribed to competition stream: {}", request.getCompetitionId());

        // Baseline skeleton event stream; streaming loop will be implemented in task B6
        LiveEvent initialEvent = LiveEvent.newBuilder()
                .setCompetitionId(request.getCompetitionId())
                .setType(EventType.START)
                .setAthleteA("Athlete A")
                .setAthleteB("Athlete B")
                .setScoreA(0)
                .setScoreB(0)
                .setTimestamp(Instant.now().getEpochSecond())
                .build();

        responseObserver.onNext(initialEvent);
        responseObserver.onCompleted();
    }
}
