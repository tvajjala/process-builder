package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.AppUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProcessBuilderDemo {

    /**
     * logger
     */
    static Logger log = LoggerFactory.getLogger(ProcessBuilderDemo.class);

    public static void main(String[] args) throws Exception {
        new ProcessBuilderDemo().runMultiProcess();
    }

    void runMultiProcess() {

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 10; i++) {
            executorService.submit(new ProcessRunner());
        }
        executorService.shutdown();
        //executorService.awaitTermination(5, TimeUnit.SECONDS);
    }

    class ProcessRunner implements Runnable {
        @Override
        public void run() {

            AppUtil.runProcess(Main.class);
        }
    }


}


