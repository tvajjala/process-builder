package util;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Generic ResiliencyHandler to simplify retry operations with fixed/exponential backoff delay
 * strategies
 *
 * <p>Scenario-1: Verifying namespace activeness with resiliency
 *
 * <pre>
 *     ResiliencyHandler.buildFor(Boolean.class)
 *                 .usingDefaultJitterStrategy()//default jitter strategy 3 attempts
 *                 .tryWithResiliency(() -> {// this will be retried
 *                     getInstance().execute(NAMESPACE_LIST_CMD, "| grep ", namespaceId);
 *                     return true;
 *                 })
 *                 .fallbackOnFail(() -> {// this will be fallback
 *                     getInstance().execute("ps -ef", "| grep ", namespaceId, "| grep mv-creator");
 *                     return true;
 *                 }).getResult(false); //then return default value
 * </pre>
 *
 * <p>Scenario-2: Reading sessionState with resiliency
 *
 * <pre>
 *           CreatorSessionState creatorSessionState =
 *                   ResiliencyHandler.buildFor(CreatorSessionState.class)
 *                           .usingJitterStrategy(3, 5000L)
 *                           .tryWithResiliency(() -> sessionMgr.getActiveSession())
 *                           .fallbackOnFail(()->sessionMgr.getArchivedSession())
 *                           .getResult();// return result
 * </pre>
 *
 * @param <R> ResultType
 * @author tvajjala
 */
@Slf4j
public class ResiliencyHandler<R> {

    /** Expected Result */
    @Getter
    private R result;

    /* lock with fair mode during re-attempts */

    /** builder instance */
    private final Builder<R> builder;

    /**
     * @param builder builder
     */
    private ResiliencyHandler(Builder<R> builder) {
        this.builder = builder;
    }

    /**
     * @param clazz clazz
     * @param <R> ofType
     * @return Builder
     */
    public static <R> Builder<R> buildFor(Class<R> clazz) {
        log.info("Creating builder instance for clazz {}", clazz);
        return new Builder<>();
    }

    RuntimeException runtimeException;

    /**
     * Retry function
     *
     * @param supplier Supplier
     * @return ResiliencyHandler
     */
    public ResiliencyHandler<R> tryWithResiliency(Supplier<R> supplier) {

        int counter = 0;
        do {
            try {
                this.result = supplier.get();
                builder.setSuccess(true);
                return this;
            } catch (Exception exception) {
                log.trace("Failure occurred", exception);
                log.warn("Operation failed due to {}", exception.getMessage());
                this.runtimeException = new ResiliencyException(exception.getMessage(), exception);
            }

            if (counter++ == builder.getMaxAttempts()) {
                break;
            }
            builder.setAttemptsMade(counter);
            builder.waitForNextAttempt();
            log.info("Retry Attempt: {}", counter);
        } while (counter <= builder.getMaxAttempts());

        return this;
    }

    /**
     * Execute fallback behaviour if retry attempts fail
     *
     * @param supplier Supplier
     * @return ResiliencyHandler
     */
    public ResiliencyHandler<R> fallbackOnFail(Supplier<R> supplier) {

        if (!builder.isSuccess()) {
            log.info("Executing fallback behaviour");
            this.result = supplier.get();
        }
        return this;
    }

    /**
     * Return result onCompletion
     *
     * @return result
     */
    public R getResultOrRethrow() {
        if (null == result && null != runtimeException) {
            throw runtimeException;
        }
        return result;
    }

    /**
     * @param customException must be RuntimeException when you work with java8 lamda/functional
     *     interfaces
     * @return result if available
     */
    public R getResultOrRethrow(RuntimeException customException) {
        if (null == result && null != customException) {
            throw customException;
        }
        return result;
    }

    /**
     * Return result or default value
     *
     * @param defaultResult defaultResult
     * @return result
     */
    public R getResult(R defaultResult) {
        if (null == result) {
            return defaultResult;
        }
        return result;
    }

    /**
     * Builder class
     *
     * @param <R>
     */
    public static class Builder<R> {

        /** flag to check retry success */
        private boolean isSuccess;

        public boolean isSuccess() {
            return isSuccess;
        }

        public void setSuccess(boolean success) {
            isSuccess = success;
        }

        /** default max retry attempts= 3 */
        @Getter
        private int maxAttempts = 3;

        /** Default fixedDelay after each retry (5 seconds) */
        private long fixedDelayInMillis = 1000L; // 1000 ms

        /** Default maximum delay with jitter config */
        private long maxDelayInMillis = 10000L; // 10K ms max

        /** Total attempts made (used runtime)
         * -- GETTER --
         *  Returns attempts made so far
         *
         */
        @Setter
        @Getter
        private int attemptsMade;

        /** flag to check if jitterConfiguration requested by client */
        private boolean isJitterEnabled;

        /**
         */
        public boolean isJitterEnabled() {
            return isJitterEnabled;
        }

        /**
         * Used jitterConfiguration with exponential backoff used
         *
         * @param maxAttempts maxAttempts
         * @param maxDelayInMillis maxDelayInMillis after each retry, wait time increases
         *     exponentially until it reaches maxDelay
         * @return ResiliencyHandler instance
         */
        public ResiliencyHandler<R> usingJitterStrategy(int maxAttempts, long maxDelayInMillis) {
            this.isJitterEnabled = true;
            this.maxAttempts = maxAttempts;
            this.maxDelayInMillis = maxDelayInMillis;
            return new ResiliencyHandler<>(this);
        }

        /**
         * With default jitter configuration used
         *
         * @return ResiliencyHandler instance
         */
        public ResiliencyHandler<R> usingDefaultJitterStrategy() {
            this.isJitterEnabled = true;

            return new ResiliencyHandler<>(this);
        }

        public ResiliencyHandler<R> usingDefaultFixedDelayStrategy() {
            this.isJitterEnabled = false;
            return new ResiliencyHandler<>(this);
        }

        /**
         * Fixed delay configuration
         *
         * @param maxAttempts maxAttempts
         * @param fixedDelayInMillis fixedDelayInMillis
         * @return ResiliencyHandler instance
         */
        public ResiliencyHandler<R> usingFixedDelayStrategy(
                int maxAttempts, long fixedDelayInMillis) {
            this.maxAttempts = maxAttempts;
            this.fixedDelayInMillis = fixedDelayInMillis;
            return new ResiliencyHandler<>(this);
        }

        private void waitForNextAttempt() {
            long delayDuration = isJitterEnabled() ? jitterDelay() : fixedDelay();
            try {
                log.info("Retry attempted after {} ms", delayDuration);
                TimeUnit.MILLISECONDS.sleep(delayDuration);
            } catch (InterruptedException interruptedException) {
                log.trace("Failed to wait for next attempt", interruptedException);
            }
        }

        private long fixedDelay() {
            return fixedDelayInMillis;
        }

        /**
         * Exponential back-off strategy
         *
         * @return delay in seconds
         */
        public long exponentialDelay() {

            long delay = (long) Math.pow(2, getAttemptsMade());
            delay *= 1000;
            if (delay <= 0) {
                return maxDelayInMillis;
            }
            return Math.min(delay, maxDelayInMillis);
        }

        /**
         * Exponential with jitter
         *
         * @return delay in seconds
         */
        public long jitterDelay() {
            double random = Math.random();
            long jitterValue = Math.round(random * 1000);
            return exponentialDelay() + jitterValue;
        }
    }

    /** CustomException can be used in case APIs throw checked exception during resiliency run */
    public static class ResiliencyException extends RuntimeException {

        private final String message;

        public ResiliencyException(String message) {
            super(message);
            this.message = message;
        }

        public ResiliencyException(String message, Throwable throwable) {
            super(message, throwable);
            this.message = message;
        }

        public ResiliencyException(Throwable throwable) {
            super(throwable);
            this.message = throwable.getMessage();
        }

        @Override
        public String getMessage() {
            return message;
        }
    }
}
