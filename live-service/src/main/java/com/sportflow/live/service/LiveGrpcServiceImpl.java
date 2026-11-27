package com.sportflow.live.service;

import com.sportflow.live.entity.LiveEvent;
import com.sportflow.live.grpc.EventType;
import com.sportflow.live.grpc.LiveCompetitionServiceGrpc;
import com.sportflow.live.grpc.SubscribeRequest;
import com.sportflow.live.registry.LiveStreamRegistry;
import com.sportflow.live.repository.LiveEventRepository;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@GrpcService
public class LiveGrpcServiceImpl extends LiveCompetitionServiceGrpc.LiveCompetitionServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(LiveGrpcServiceImpl.class);

    private final LiveStreamRegistry streamRegistry;
    private final LiveEventRepository eventRepository;

    public LiveGrpcServiceImpl(LiveStreamRegistry streamRegistry, LiveEventRepository eventRepository) {
        this.streamRegistry = streamRegistry;
        this.eventRepository = eventRepository;
    }

    @Override
    public void subscribe(SubscribeRequest request, StreamObserver<com.sportflow.live.grpc.LiveEvent> responseObserver) {
        String competitionId = request.getCompetitionId();
        log.info("Client subscribed to live events for competitionId: {}", competitionId);

        // Register cancellation hook if possible
        if (responseObserver instanceof ServerCallStreamObserver<com.sportflow.live.grpc.LiveEvent> serverObserver) {
            serverObserver.setOnCancelHandler(() -> {
                log.info("Client cancelled subscription for competitionId: {}", competitionId);
                streamRegistry.removeSubscriber(competitionId, responseObserver);
            });
        }

        // Replay historical events
        List<LiveEvent> history = eventRepository.findByCompetitionIdOrderByEventTimestampAsc(competitionId);
        boolean alreadyEnded = false;
        for (LiveEvent pastEvent : history) {
            com.sportflow.live.grpc.LiveEvent protoEvent = toProto(pastEvent);
            responseObserver.onNext(protoEvent);
            if (protoEvent.getType() == EventType.END) {
                alreadyEnded = true;
            }
        }

        if (alreadyEnded) {
            log.info("Match for competition {} has already completed. Closing subscription stream.", competitionId);
            responseObserver.onCompleted();
            return;
        }

        // Register for ongoing live events
        streamRegistry.addSubscriber(competitionId, responseObserver);
    }

    private com.sportflow.live.grpc.LiveEvent toProto(LiveEvent entity) {
        return com.sportflow.live.grpc.LiveEvent.newBuilder()
                .setCompetitionId(entity.getCompetitionId())
                .setType(EventType.valueOf(entity.getEventType()))
                .setAthleteA(entity.getAthleteA())
                .setAthleteB(entity.getAthleteB())
                .setScoreA(entity.getScoreA())
                .setScoreB(entity.getScoreB())
                .setTimestamp(entity.getEventTimestamp())
                .build();
    }
}
