/*
 * Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
 */
package com.codergists.log.appender.lock;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;

/**
 * Implements session file locking functionality
 *
 * @author tvajjala
 */
@Slf4j
final class SessionLock implements ISessionLock {


    /**
     * fileChannel
     */
    private FileChannel fileChannel;

    /**
     * fileLock
     */
    private FileLock fileLock;

    String fileName;

    /**
     * private constructor
     */
    SessionLock(String fileName) {
        this.fileName = fileName;
        log.info("Creating SessionLock instance on session {}", fileName);
        if (null == fileName) {
            log.error("SessionId required to create SessionLock");
            throw new AssertionError("fileName required");
        }
    }

    /**
     * This channel allows us to create new session file. if file already exists it throws exception
     * use this only to create new session file.
     *
     * @throws SessionStateException when session file already exists
     */
    void createFileChannel() throws SessionStateException {
        try {
            Path sessionFile = Paths.get(fileName);
            log.info("Attempting to acquire create lock on sessionFile {}", sessionFile);
            this.fileChannel = FileChannel.open(
                    sessionFile,
                    StandardOpenOption.WRITE);
        } catch (FileAlreadyExistsException alreadyExistsException) {
            log.error("Session already exists", alreadyExistsException);
            throw new SessionStateException("active session already exists", alreadyExistsException);
        } catch (IOException ioException) {
            log.error("Failed to create new file", ioException);
            throw new SessionStateException(ioException.getMessage(), ioException);
        }
    }


    /**
     * Try to acquire lock of the session file
     *
     * @throws SessionStateException failed to acquire lock
     */
    void acquireLock() throws SessionStateException {
        try {
            log.debug("Trying to acquire lock on fileChannel");
            FileLock _lock = fileChannel.tryLock();
            while (null == _lock) {//it only enters into loop if lock not acquired
                log.warn("Unable to acquire lock on fileChannel, This attempt will be retried after 1 second");
                waitForSeconds(1);
                _lock = fileChannel.tryLock();
            }
            log.debug("Acquired lock. isValid? {}, isShared {}", _lock.isValid(), _lock.isShared());
            this.fileLock = _lock;
        } catch (IOException ioException) {
            log.error("Failed to acquire lock", ioException);
            throw new SessionStateException("Failed to acquire lock", ioException);
        }
    }

    static void waitForSeconds(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException interruptedException) {
            log.debug("Failed to wait", interruptedException);
        }
    }


    /**
     * Release lock and close fileChannel
     */
    private void releaseLock() {
        try {
            if (null != fileLock && fileLock.isValid()) {
                fileLock.release();
                log.debug("FileChannel Lock {} released successfully", fileLock);
            }
        } catch (IOException exception) {
            log.warn("Failed to release lock", exception);
        }
        releaseChannel();
    }

    /**
     * release fileChannel
     */
    private void releaseChannel() {
        try {
            if (null != fileChannel && fileChannel.isOpen()) {
                fileChannel.close();
                log.debug("Channel {} closed successfully", fileChannel);
            }
        } catch (Exception exception) {
            log.warn("Failed to close channel", exception);
        }
    }

    /**
     * Close lock and channels
     */
    @Override
    public void close() {
        releaseLock();
    }

}
