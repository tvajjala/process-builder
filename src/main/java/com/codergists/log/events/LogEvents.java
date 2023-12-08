package com.codergists.log.events;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractOutputStreamAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import util.AppUtil;

import java.util.stream.IntStream;

@Slf4j
public class LogEvents {

    public static void main(String[] args) throws Exception {

        long start = System.currentTimeMillis();

        new LogEvents().verifyLogEvents();
        System.out.println("DONE");
        //  Runtime.getRuntime().addShutdownHook(new Thread(() -> log.info("SHUTDOWN")));
        // Thread.currentThread().join();
        log.info("DONE ");

        System.out.println((System.currentTimeMillis() - start));
    }

    private static void verifyLogEvents() {
        log.info("MAIN_PROCESS...");
        AppUtil.runProcess(VolumeCreator.class);
        IntStream.range(1, 5).forEach(i -> log.info("BACK_TO_MAIN_PROCESS {}", i));

        AppUtil.runProcess(VolumeCreator.class);
        IntStream.range(1, 5).forEach(i -> log.info("BACK_TO_MAIN_PROCESS {}", i));
        immediateFlush();

    }

    public static void immediateFlush() {
        Logger logger = org.apache.logging.log4j.core.LoggerContext.getContext().getRootLogger();

        for (Appender appender : logger.getAppenders().values()) {
            System.out.println(appender);
            if (appender instanceof RollingFileAppender) {
                ((RollingFileAppender) appender).getImmediateFlush();
            }
        }
    }

    public static void flushAll() {
        final LoggerContext logCtx = ((LoggerContext) LogManager.getContext());
        for (final org.apache.logging.log4j.core.Logger logger : logCtx.getLoggers()) {
            for (final Appender appender : logger.getAppenders().values()) {
                if (appender instanceof AbstractOutputStreamAppender) {
                    ((AbstractOutputStreamAppender) appender).getManager().flush();
                }
            }
        }
    }


}
