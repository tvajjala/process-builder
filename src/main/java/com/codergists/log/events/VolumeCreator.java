package com.codergists.log.events;

import lombok.extern.slf4j.Slf4j;

import java.util.stream.IntStream;

@Slf4j
public class VolumeCreator {

    public void run() {
        IntStream.range(1, 10000).forEach(i -> log.info("INSIDE_CREATION {}", i));
        log.info("VOLUME_CREATION_SUCCESSFUL");
    }

    public static void main(String[] args) {

        new VolumeCreator().run();
        log.info("VOLUME_CREATION_SUCCESSFUL_MAIN");
        // Runtime.getRuntime().addShutdownHook(new Thread(() -> LogManager.shutdown()));
        //log.info("AFTER_SHUTDOWN");
    }
}
