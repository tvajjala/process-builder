/*
 * Copyright (c) $originalComment.match("Copyright \(c\) (\d+)", 1, "-")2022, Oracle and/or its affiliates. All
 * rights reserved.
 */

package util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * ProcessExecutor to run list of commands and return success response.
 * <p>
 * This Singleton class executes array of commands on {@link Runtime} and wait for specified Time Period to get the
 * response back.
 * <p>
 * Ex: ProcessExecutor.getInstance().execute("whoami");
 */
@Slf4j
final class ProcessExecutor {

    /**
     * runtime
     */
    private static final Runtime RUNTIME = Runtime.getRuntime();
    /**
     * process execution
     */
    private static final ProcessExecutor _INSTANCE = new ProcessExecutor();

    /**
     * private constructor
     */
    private ProcessExecutor() {
    }

    /**
     * Return instance of {@link com.oracle.pic.redisservice.resiliency.ProcessExecutor}
     *
     * @return CommandExecutor
     */
    public static ProcessExecutor getInstance() {
        return _INSTANCE;
    }

    /**
     * Execute commands and return response with default TimeoutPeriod ( 3 seconds)
     *
     * @param commands arrays of commands to be executed
     * @return success response as string
     */
    public String execute(final String... commands) {
        return execute(3, commands);
    }

    /**
     * Execute commands and return response with specific timeOutPeriod in seconds
     *
     * @param timeOutInSeconds timeOutInSeconds
     * @param commands         arrays of commands to be executed
     * @return success response as string
     */
    public String execute(final long timeOutInSeconds, final String... commands) {

        Process process = null;

        try {
            log.info("Executing command {}", Arrays.toString(commands));
            process = (commands.length == 1) ? RUNTIME.exec(commands[0]) : RUNTIME.exec(commands);
            final int exitCode = waitFor(process, timeOutInSeconds, TimeUnit.SECONDS);

            if (0 == exitCode) {
                log.info("Returning success response");
                return parseStream(process.getInputStream(), exitCode);
            }

            log.info("Received non-zero exitCode {}", exitCode);
            final String errorResponse = parseStream(process.getErrorStream(), exitCode);
            log.info("Received error response  {}", errorResponse);
            throw new ProcessExecutionException(errorResponse, exitCode);
        } catch (final ProcessExecutionException processExecutionException) {
            //don't log as error, caller will decide whether error or not
            log.warn("Unable to execute command {}", Arrays.toString(commands));
            throw processExecutionException;
        } catch (final Exception e) {
            //don't log as error, caller will decide whether error or not
            log.warn("Unable to execute command {}", Arrays.toString(commands));
            throw new ProcessExecutionException("Unable to read stream", e, -1);
        } finally {
            try {
                if (null != process) {
                    process.destroy();
                }
                log.debug("Process destroyed successfully");
            } catch (final Exception processCloseException) {
                log.warn("Error while destroying the process", processCloseException);
            }
        }
    }

    private String parseStream(final InputStream inputStream, int exitCode) {

        final StringBuffer stringBuffer = new StringBuffer();

        try (final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }
            log.info("Returning response {}", stringBuffer);
            return stringBuffer.toString();
        } catch (final IOException e) {
            log.warn("Error while reading the stream {}", e.getMessage());
            throw new ProcessExecutionException("Error while reading the stream", e, exitCode);
        }

    }

    /**
     * Wrapper method for Process to wait the command to be executed.
     *
     * @param process          process
     * @param timeOutInSeconds timeOutInSeconds
     * @param timeUnit         timeUnit
     * @return exitValue 0 if successful execution
     */
    int waitFor(final Process process, final long timeOutInSeconds, final TimeUnit timeUnit) {

        final Callable<Integer> call = () -> {
            process.waitFor();
            return process.exitValue();
        };

        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        final Future<Integer> integerFuture = executorService.submit(call);

        try {
            return integerFuture.get(timeOutInSeconds, timeUnit);
        } catch (final TimeoutException | ExecutionException | InterruptedException ex) {
            log.warn("Error while waiting For process to be completed " + ex.getMessage());
            throw new ProcessExecutionException("Error while waiting for process to be completed", ex, -1);
        } finally {
            /*  Must shutdown the process , otherwise process still runs in the background*/
            executorService.shutdown();
            log.debug("ExecutorService shutdown completed");
        }

    }

}
