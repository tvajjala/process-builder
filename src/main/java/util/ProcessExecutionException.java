/*
 * Copyright (c) Oracle, 2022, Oracle and/or its affiliates. All rights reserved.
 */
package util;

import lombok.Getter;

/**
 * Standard Exception for StorageFactory Validator
 * <a href="https://literatejava.com/exceptions/checked-exceptions-javas-biggest-mistake/">...</a>
 *
 * @author tvajjala
 */
@Getter
public class ProcessExecutionException extends RuntimeException {

    int exitCode = -1;

    /**
     * Constructs a new runtime exception with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public ProcessExecutionException(String message) {
        super(message);
    }

    /**
     * Constructs a new runtime exception with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     *
     * @param message  the detail message. The detail message is saved for
     *                 later retrieval by the {@link #getMessage()} method.
     * @param exitCode exitCode in exception for non-zero exitCodes
     */
    public ProcessExecutionException(String message, int exitCode) {

        super(message);
        this.exitCode = exitCode;
    }

    /**
     * Constructs a new runtime exception with the specified detail message and
     * cause.  <p>Note that the detail message associated with
     * {@code cause} is <i>not</i> automatically incorporated in
     * this runtime exception's detail message.
     *
     * @param message  the detail message (which is saved for later retrieval
     *                 by the {@link #getMessage()} method).
     * @param cause    the cause (which is saved for later retrieval by the
     *                 {@link #getCause()} method).  (A <tt>null</tt> value is
     *                 permitted, and indicates that the cause is nonexistent or
     *                 unknown.)
     * @param exitCode exitCode in exception for non-zero exitCodes
     */
    public ProcessExecutionException(String message, Throwable cause, int exitCode) {
        super(message, cause);
        this.exitCode = exitCode;
    }

}
