package com.sportflow.live.service;

import com.sportflow.live.grpc.EventType;
import com.sportflow.live.grpc.LiveCompetitionServiceGrpc;
import com.sportflow.live.grpc.LiveEvent;
import com.sportflow.live.grpc.SubscribeRequest;
import com.sportflow.live.registry.LiveStreamRegistry;
import com.sportflow.live.repository.LiveEventRepository;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LiveGrpcServiceInProcessTest {

    private final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();
    private ManagedChannel channel;
    private LiveCompetitionServiceGrpc.LiveCompetitionServiceStub asyncStub;

    private LiveStreamRegistry registry;

    @Mock
    private LiveEventRepository eventRepository;

    private LiveGrpcServiceImpl grpcService;

    @BeforeEach
    void setUp() throws IOException {
        String serverName = InProcessServerBuilder.generateName();
        registry = new LiveStreamRegistry();
        grpcService = new LiveGrpcServiceImpl(registry, eventRepository);

        grpcCleanup.register(
                InProcessServerBuilder.forName(serverName)
                        .directExecutor()
                        .addService(grpcService)
                        .build()
                        .start()
        );

        channel = grpcCleanup.register(
                InProcessChannelBuilder.forName(serverName)
                        .directExecutor()
                        .build()
        );

        asyncStub = LiveCompetitionServiceGrpc.newStub(channel);
    }

    @AfterEach
    void tearDown() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdownNow();
        }
    }

    @Test
    @DisplayName("Two concurrent subscribers receive all events from the live stream until END")
    void testTwoSubscribersReceiveAllEvents() throws InterruptedException {
        String competitionId = "comp-100";
        when(eventRepository.findByCompetitionIdOrderByEventTimestampAsc(competitionId))
                .thenReturn(Collections.emptyList());

        List<LiveEvent> client1Events = new ArrayList<>();
        List<LiveEvent> client2Events = new ArrayList<>();
        CountDownLatch client1Completed = new CountDownLatch(1);
        CountDownLatch client2Completed = new CountDownLatch(1);

        SubscribeRequest request = SubscribeRequest.newBuilder()
                .setCompetitionId(competitionId)
                .build();

        // Subscribe Client 1
        asyncStub.subscribe(request, new StreamObserver<LiveEvent>() {
            @Override
            public void onNext(LiveEvent value) {
                client1Events.add(value);
            }

            @Override
            public void onError(Throwable t) {}

            @Override
            public void onCompleted() {
                client1Completed.countDown();
            }
        });

        // Subscribe Client 2
        asyncStub.subscribe(request, new StreamObserver<LiveEvent>() {
            @Override
            public void onNext(LiveEvent value) {
                client2Events.add(value);
            }

            @Override
            public void onError(Throwable t) {}

            @Override
            public void onCompleted() {
                client2Completed.countDown();
            }
        });

        assertThat(registry.getSubscriberCount(competitionId)).isEqualTo(2);

        // Emit START event
        LiveEvent startEvent = LiveEvent.newBuilder()
                .setCompetitionId(competitionId)
                .setType(EventType.START)
                .setAthleteA("Athlete A")
                .setAthleteB("Athlete B")
                .setScoreA(0)
                .setScoreB(0)
                .setTimestamp(100L)
                .build();
        registry.broadcast(competitionId, startEvent);

        // Emit SCORE_UPDATE event
        LiveEvent scoreEvent = LiveEvent.newBuilder()
                .setCompetitionId(competitionId)
                .setType(EventType.SCORE_UPDATE)
                .setAthleteA("Athlete A")
                .setAthleteB("Athlete B")
                .setScoreA(1)
                .setScoreB(0)
                .setTimestamp(102L)
                .build();
        registry.broadcast(competitionId, scoreEvent);

        // Emit END event
        LiveEvent endEvent = LiveEvent.newBuilder()
                .setCompetitionId(competitionId)
                .setType(EventType.END)
                .setAthleteA("Athlete A")
                .setAthleteB("Athlete B")
                .setScoreA(1)
                .setScoreB(0)
                .setTimestamp(104L)
                .build();
        registry.broadcast(competitionId, endEvent);

        // Wait for both to complete
        boolean c1Done = client1Completed.await(3, TimeUnit.SECONDS);
        boolean c2Done = client2Completed.await(3, TimeUnit.SECONDS);

        assertThat(c1Done).isTrue();
        assertThat(c2Done).isTrue();

        assertThat(client1Events).hasSize(3);
        assertThat(client2Events).hasSize(3);

        assertThat(client1Events.get(0).getType()).isEqualTo(EventType.START);
        assertThat(client1Events.get(1).getType()).isEqualTo(EventType.SCORE_UPDATE);
        assertThat(client1Events.get(2).getType()).isEqualTo(EventType.END);

        assertThat(client2Events.get(0).getType()).isEqualTo(EventType.START);
        assertThat(client2Events.get(1).getType()).isEqualTo(EventType.SCORE_UPDATE);
        assertThat(client2Events.get(2).getType()).isEqualTo(EventType.END);

        // Registry should now be cleared
        assertThat(registry.getSubscriberCount(competitionId)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cancelled subscriber is removed and stops receiving events, while others continue")
    void testCancelledSubscriberIsRemoved() throws InterruptedException {
        String competitionId = "comp-200";
        when(eventRepository.findByCompetitionIdOrderByEventTimestampAsc(competitionId))
                .thenReturn(Collections.emptyList());

        List<LiveEvent> client1Events = new ArrayList<>();
        List<LiveEvent> client2Events = new ArrayList<>();
        CountDownLatch client2Completed = new CountDownLatch(1);
        AtomicBoolean client1Cancelled = new AtomicBoolean(false);

        SubscribeRequest request = SubscribeRequest.newBuilder()
                .setCompetitionId(competitionId)
                .build();

        // Subscribe Client 1 with cancellation support
        asyncStub.subscribe(request, new ClientResponseObserver<SubscribeRequest, LiveEvent>() {
            private ClientCallStreamObserver<SubscribeRequest> requestStream;

            @Override
            public void beforeStart(ClientCallStreamObserver<SubscribeRequest> requestStream) {
                this.requestStream = requestStream;
            }

            @Override
            public void onNext(LiveEvent value) {
                client1Events.add(value);
                // Cancel immediately after first event
                if (requestStream != null) {
                    requestStream.cancel("Client 1 cancel", null);
                    client1Cancelled.set(true);
                }
            }

            @Override
            public void onError(Throwable t) {}

            @Override
            public void onCompleted() {}
        });

        // Subscribe Client 2
        asyncStub.subscribe(request, new StreamObserver<LiveEvent>() {
            @Override
            public void onNext(LiveEvent value) {
                client2Events.add(value);
            }

            @Override
            public void onError(Throwable t) {}

            @Override
            public void onCompleted() {
                client2Completed.countDown();
            }
        });

        assertThat(registry.getSubscriberCount(competitionId)).isEqualTo(2);

        // Emit START event -> Client 1 will cancel after receiving this
        LiveEvent startEvent = LiveEvent.newBuilder()
                .setCompetitionId(competitionId)
                .setType(EventType.START)
                .setAthleteA("Athlete A")
                .setAthleteB("Athlete B")
                .build();
        registry.broadcast(competitionId, startEvent);

        assertThat(client1Cancelled.get()).isTrue();
        assertThat(client1Events).hasSize(1);

        // Wait for cancellation hook to remove Client 1
        for (int i = 0; i < 20; i++) {
            if (registry.getSubscriberCount(competitionId) == 1) {
                break;
            }
            Thread.sleep(50);
        }
        assertThat(registry.getSubscriberCount(competitionId)).isEqualTo(1);

        // Emit SCORE_UPDATE and END
        LiveEvent scoreEvent = LiveEvent.newBuilder()
                .setCompetitionId(competitionId)
                .setType(EventType.SCORE_UPDATE)
                .build();
        registry.broadcast(competitionId, scoreEvent);

        LiveEvent endEvent = LiveEvent.newBuilder()
                .setCompetitionId(competitionId)
                .setType(EventType.END)
                .build();
        registry.broadcast(competitionId, endEvent);

        boolean c2Done = client2Completed.await(3, TimeUnit.SECONDS);
        assertThat(c2Done).isTrue();

        assertThat(client1Events).hasSize(1); // Only received START
        assertThat(client2Events).hasSize(3); // Received START, SCORE_UPDATE, END
    }
}
