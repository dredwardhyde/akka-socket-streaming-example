package com.akka.streaming;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.io.Tcp;
import akka.pattern.Patterns;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static com.akka.streaming.LiveCacheActor.GET_ALL;

@SuppressWarnings("unchecked")
public class SocketStreamingMain {
    private static final int PORT_START = 10_000;
    private static final int PORT_END = 50_000;

    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("streaming-system");
        final ActorRef cache = system.actorOf(Props.create(LiveCacheActor.class));
        final ActorRef manager = Tcp.get(system).manager();
        IntStream.range(PORT_START, PORT_END).forEach(i -> system.actorOf(Props.create(SocketListenerActor.class, manager, cache, i)));
        final ActorRef messageSender = system.actorOf(Props.create(MessageSenderActor.class, cache));
        final Runnable runnable = () -> Patterns.ask(cache, GET_ALL, Duration.ofSeconds(20))
                .thenAccept(result -> ((Map<Integer, ActorRef>) result).keySet()
                .forEach(port -> messageSender.tell(new MessageSenderActor.SendMessage(port, System.currentTimeMillis() + System.lineSeparator()), ActorRef.noSender())
        ));
        final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(runnable, 0, 50, TimeUnit.MILLISECONDS);
    }
}
