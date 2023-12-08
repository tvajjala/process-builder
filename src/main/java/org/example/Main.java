package org.example;

import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import static java.util.concurrent.TimeUnit.MICROSECONDS;

public class Main {

    static Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        MDC.put("pid", String.valueOf(getProcessId()));

        // log.info("Received req");
        int i = 1;

        do {
            log.info("writing to logger {}", i++);

            LogManager.shutdown(true);
            //waitForSeconds(1);

            // log.info("Completed writing {}", i);
        } while (i < 3000);

    }

    public static Long getProcessId() {
        try {
            final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
            String processId = runtimeMXBean.getName().split("@")[0];
            // to display on processId on log files
            MDC.put("pid", processId);
            return Long.parseLong(processId);
        } catch (final Exception e) {
            log.error("Error while reading process ", e);
        }
        return 0L;
    }

    public static void waitForSeconds(int seconds) {
        try {
            MICROSECONDS.sleep(seconds);
        } catch (InterruptedException interruptedException) {
            log.warn("Sleep timer interrupted", interruptedException);
        }
    }
}