package com.codergists.log.events;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VolumeCreator {

    public void run() throws Exception {
        for (int i = 0; i < 11; i++) {
            log.info("INSIDE_CREATION {} {}", i, System.getenv("suid"));
            Thread.sleep(1);
        }
        log.info("VOLUME_CREATION_SUCCESSFUL");
    }

    public static void main(String[] args) throws Exception {
        new VolumeCreator().run();
        System.out.println("running" + System.getenv().get("suid"));
        log.info("VOLUME_CREATION_SUCCESSFUL_MAIN");
    }
}
