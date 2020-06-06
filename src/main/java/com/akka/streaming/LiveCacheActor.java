package com.akka.streaming;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class LiveCacheActor extends AbstractActor {
    public static final String GET_ALL = "all";
    public final Map<Integer, ActorRef> connectedClientsCache = new HashMap<>();

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals(GET_ALL, s -> getSender().tell(new HashMap<>(connectedClientsCache), getSelf()))
                .match(
                        Entry.class,
                        entry -> connectedClientsCache.put(entry.port, entry.value))
                .match(
                        Get.class,
                        get -> getSender().tell(new Entry(get.port, connectedClientsCache.get(get.port)), getSelf()))
                .match(
                        Evict.class,
                        evict -> connectedClientsCache.remove(evict.port))
                .build();
    }

    @AllArgsConstructor
    public static final class Evict implements Serializable {
        public final Integer port;
    }

    @AllArgsConstructor
    public static final class Get implements Serializable {
        public final Integer port;
    }

    @AllArgsConstructor
    public static final class Entry implements Serializable {
        public final Integer port;
        public final ActorRef value;
    }
}
