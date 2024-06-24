package com.codergists.log.events;

import lombok.extern.slf4j.Slf4j;
import util.AppUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class LogEvents {

    public static void main(String[] args) throws Exception {

        System.setProperty("log4j2.enable.direct.encoders", "false");

        // System.out.println(System.getProperty("log4j2.enable.direct.encoders"));

        // long start = System.currentTimeMillis();

        new LogEvents().parallelWrites();

        //System.out.println((System.currentTimeMillis() - start));
    }

    private static void parallelWrites() {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        // to demonstrate multiple jobs running in parallel
        executorService.submit(() -> AppUtil.runProcess(VolumeCreator.class));
        executorService.submit(() -> AppUtil.runProcess(VolumeCreator.class));
        executorService.submit(() -> AppUtil.runProcess(VolumeCreator.class));
        executorService.submit(() -> AppUtil.runProcess(VolumeCreator.class));
        executorService.submit(() -> AppUtil.runProcess(VolumeCreator.class));

        executorService.shutdown();
    }


    private static void sequentialWrites() {
        AppUtil.runProcess(VolumeCreator.class);
        // IntStream.range(1, 5).forEach(i -> log.info("BACK_TO_MAIN_PROCESS {}", i));

        // AppUtil.runProcess(VolumeCreator.class);
        // IntStream.range(1, 5).forEach(i -> log.info("BACK_TO_MAIN_PROCESS {}", i));

    }


}
