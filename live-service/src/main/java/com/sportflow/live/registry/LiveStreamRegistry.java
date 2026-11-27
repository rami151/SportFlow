package com.sportflow.live.registry;

import com.sportflow.live.grpc.EventType;
import com.sportflow.live.grpc.LiveEvent;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LiveStreamRegistry {

    private static final Logger log = LoggerFactory.getLogger(LiveStreamRegistry.class);

    private final ConcurrentHashMap<String, Set<StreamObserver<LiveEvent>>> subscribers = new ConcurrentHashMap<>();

    public void addSubscriber(String competitionId, StreamObserver<LiveEvent> observer) {
        subscribers.computeIfAbsent(competitionId, k -> ConcurrentHashMap.newKeySet()).add(observer);
        log.info("Subscriber registered for competition {}. Current subscribers: {}",
                competitionId, getSubscriberCount(competitionId));
    }

    public void removeSubscriber(String competitionId, StreamObserver<LiveEvent> observer) {
        Set<StreamObserver<LiveEvent>> set = subscribers.get(competitionId);
        if (set != null) {
            set.remove(observer);
            if (set.isEmpty()) {
                subscribers.remove(competitionId);
            }
        }
        log.info("Subscriber removed for competition {}. Remaining subscribers: {}",
                competitionId, getSubscriberCount(competitionId));
    }

    public int getSubscriberCount(String competitionId) {
        Set<StreamObserver<LiveEvent>> set = subscribers.get(competitionId);
        return set != null ? set.size() : 0;
    }

    public Set<StreamObserver<LiveEvent>> getSubscribers(String competitionId) {
        Set<StreamObserver<LiveEvent>> set = subscribers.get(competitionId);
        return set != null ? Collections.unmodifiableSet(set) : Collections.emptySet();
    }

    public void broadcast(String competitionId, LiveEvent event) {
        Set<StreamObserver<LiveEvent>> set = subscribers.get(competitionId);
        if (set == null || set.isEmpty()) {
            log.debug("No active subscribers for competition: {}", competitionId);
            return;
        }

        log.info("Broadcasting event [{}] to {} subscriber(s) for competition: {}",
                event.getType(), set.size(), competitionId);

        for (StreamObserver<LiveEvent> observer : set) {
            try {
                observer.onNext(event);
            } catch (Exception e) {
                log.warn("Error sending event to subscriber for competition {}. Evicting: {}",
                        competitionId, e.getMessage());
                set.remove(observer);
            }
        }

        if (event.getType() == EventType.END) {
            log.info("Match END event broadcast. Completing all streams for competition: {}", competitionId);
            for (StreamObserver<LiveEvent> observer : set) {
                try {
                    observer.onCompleted();
                } catch (Exception e) {
                    log.debug("Observer completion exception: {}", e.getMessage());
                }
            }
            subscribers.remove(competitionId);
        }
    }

    public void clear() {
        subscribers.clear();
    }
}
