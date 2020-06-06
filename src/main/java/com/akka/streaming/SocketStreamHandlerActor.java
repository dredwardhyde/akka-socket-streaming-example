package com.akka.streaming;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.io.Tcp;
import akka.io.TcpMessage;
import akka.util.ByteString;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SocketStreamHandlerActor extends AbstractActor {
    final ActorRef conn;
    final ActorRef cache;
    final Integer port;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SendData.class, r -> conn.tell(TcpMessage.write(ByteString.fromString(r.data)), getSelf()))
                .match(
                        Tcp.ConnectionClosed.class, msg -> {
                            cache.tell(new LiveCacheActor.Evict(port), getSelf());
                            getContext().stop(getSelf());
                        })
                .build();
    }

    @AllArgsConstructor
    public static final class SendData {
        final String data;
    }
}

