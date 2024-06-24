/*
 * Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
 */
package com.codergists.log.appender.lock;

import lombok.extern.slf4j.Slf4j;

/**
 * Interface to {@link SessionLock} implementation
 *
 * @author tvajjala
 */
public interface ISessionLock extends AutoCloseable {

    /**
     * close and release session lock
     */
    @Override
    void close();

    /**
     * SessionLock builder to open differentTypes Lock
     */
    @Slf4j
    class Builder {

        /**
         * To create new session file
         *
         * @return SessionLock {@link SessionLock}
         * @throws SessionStateException failed to open
         */
        public static ISessionLock openNewSessionLock(String fileName) throws SessionStateException {
            SessionLock sessionLock = new SessionLock(fileName);
            sessionLock.createFileChannel();
            sessionLock.acquireLock();
            return sessionLock;
        }


    }
}
