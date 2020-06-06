package com.akka.streaming;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class MessageSenderActor extends AbstractActor {
    private final Map<Integer, String> pendingMessages = new HashMap<>();
    private final ActorRef cache;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(LiveCacheActor.Entry.class, r -> {
                    if (r.value != null) {
                        r.value.tell(new SocketStreamHandlerActor.SendData(pendingMessages.remove(r.port)), getSelf());
                    } else {
                        pendingMessages.remove(r.port);
                    }
                })
                .match(SendMessage.class, r -> {
                    pendingMessages.put(r.port, r.data);
                    cache.tell(new LiveCacheActor.Get(r.port), getSelf());
                })
                .build();
    }

    @AllArgsConstructor
    public static final class SendMessage {
        private final Integer port;
        private final String data;
    }
}
