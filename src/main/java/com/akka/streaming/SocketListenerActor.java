package com.akka.streaming;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.io.Tcp;
import akka.io.TcpMessage;
import lombok.AllArgsConstructor;

import java.net.InetSocketAddress;

@AllArgsConstructor
public class SocketListenerActor extends AbstractActor {

    final ActorRef manager;
    final ActorRef cache;
    final Integer port;

    @Override
    public void preStart() {
        final ActorRef tcp = Tcp.get(getContext().getSystem()).manager();
        tcp.tell(TcpMessage.bind(getSelf(), new InetSocketAddress("localhost", port), 100), getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(
                        Tcp.Bound.class,
                        msg -> manager.tell(msg, getSelf()))
                .match(
                        Tcp.CommandFailed.class,
                        msg -> getContext().stop(getSelf()))
                .match(
                        Tcp.Connected.class,
                        conn -> {
                            manager.tell(conn, getSelf());
                            final ActorRef handler = getContext().actorOf(Props.create(SocketStreamHandlerActor.class, getSender(), cache, port));
                            getSender().tell(TcpMessage.register(handler), getSelf());
                            cache.tell(new LiveCacheActor.Entry(port, handler), getSelf());
                        })
                .build();
    }
}
