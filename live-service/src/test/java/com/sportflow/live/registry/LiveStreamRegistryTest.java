package com.sportflow.live.registry;

import com.sportflow.live.grpc.EventType;
import com.sportflow.live.grpc.LiveEvent;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LiveStreamRegistryTest {

    private LiveStreamRegistry registry;

    @Mock
    private StreamObserver<LiveEvent> observer1;

    @Mock
    private StreamObserver<LiveEvent> observer2;

    @BeforeEach
    void setUp() {
        registry = new LiveStreamRegistry();
    }

    @Test
    @DisplayName("Subscribers can be added and removed cleanly")
    void testAddAndRemove() {
        registry.addSubscriber("comp-1", observer1);
        registry.addSubscriber("comp-1", observer2);

        assertThat(registry.getSubscriberCount("comp-1")).isEqualTo(2);

        registry.removeSubscriber("comp-1", observer1);
        assertThat(registry.getSubscriberCount("comp-1")).isEqualTo(1);

        registry.removeSubscriber("comp-1", observer2);
        assertThat(registry.getSubscriberCount("comp-1")).isEqualTo(0);
    }

    @Test
    @DisplayName("Broadcast sends event to all active subscribers for the competition")
    void testBroadcastToMultipleSubscribers() {
        registry.addSubscriber("comp-1", observer1);
        registry.addSubscriber("comp-1", observer2);

        LiveEvent event = LiveEvent.newBuilder()
                .setCompetitionId("comp-1")
                .setType(EventType.SCORE_UPDATE)
                .setAthleteA("Athlete A")
                .setAthleteB("Athlete B")
                .setScoreA(1)
                .setScoreB(0)
                .setTimestamp(1000L)
                .build();

        registry.broadcast("comp-1", event);

        verify(observer1).onNext(event);
        verify(observer2).onNext(event);
        verify(observer1, never()).onCompleted();
        verify(observer2, never()).onCompleted();
    }

    @Test
    @DisplayName("Match END event completes all active observers and clears registry")
    void testBroadcastEndCompletesAndClears() {
        registry.addSubscriber("comp-1", observer1);
        registry.addSubscriber("comp-1", observer2);

        LiveEvent endEvent = LiveEvent.newBuilder()
                .setCompetitionId("comp-1")
                .setType(EventType.END)
                .setAthleteA("Athlete A")
                .setAthleteB("Athlete B")
                .setScoreA(10)
                .setScoreB(5)
                .setTimestamp(2000L)
                .build();

        registry.broadcast("comp-1", endEvent);

        verify(observer1).onNext(endEvent);
        verify(observer2).onNext(endEvent);
        verify(observer1).onCompleted();
        verify(observer2).onCompleted();

        assertThat(registry.getSubscriberCount("comp-1")).isEqualTo(0);
    }

    @Test
    @DisplayName("Failing observer is automatically evicted from registry during broadcast")
    void testFailingObserverEviction() {
        registry.addSubscriber("comp-1", observer1);
        registry.addSubscriber("comp-1", observer2);

        doThrow(new RuntimeException("Connection aborted")).when(observer1).onNext(any());

        LiveEvent event = LiveEvent.newBuilder()
                .setCompetitionId("comp-1")
                .setType(EventType.SCORE_UPDATE)
                .build();

        registry.broadcast("comp-1", event);

        // Observer 2 should still receive the event
        verify(observer2).onNext(event);
        // Failing observer 1 should have been removed
        assertThat(registry.getSubscriberCount("comp-1")).isEqualTo(1);
        assertThat(registry.getSubscribers("comp-1")).containsExactly(observer2);
    }
}
