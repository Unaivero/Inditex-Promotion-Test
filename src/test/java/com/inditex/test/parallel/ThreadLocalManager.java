package com.inditex.test.parallel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Manages ThreadLocal instances across parallel test execution
 * Ensures proper cleanup and prevents memory leaks
 */
public class ThreadLocalManager {
    private static final Logger logger = LoggerFactory.getLogger(ThreadLocalManager.class);
    
    private static final ConcurrentHashMap<String, ThreadLocal<?>> threadLocals = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, String> threadInfo = new ConcurrentHashMap<>();
    private static final AtomicInteger threadCounter = new AtomicInteger(0);
    
    static {
        // Add shutdown hook to clean up all ThreadLocals
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown hook triggered - cleaning up all ThreadLocal instances");
            cleanupAllThreadLocals();
        }));
    }
    
    /**
     * Creates or retrieves a ThreadLocal instance with the given key
     */
    @SuppressWarnings("unchecked")
    public static <T> ThreadLocal<T> getOrCreateThreadLocal(String key, Supplier<T> initialValueSupplier) {
        return (ThreadLocal<T>) threadLocals.computeIfAbsent(key, k -> {
            logger.debug("Creating new ThreadLocal for key: {}", key);
            return ThreadLocal.withInitial(initialValueSupplier);
        });
    }
    
    /**
     * Gets value from ThreadLocal with the given key
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        ThreadLocal<T> threadLocal = (ThreadLocal<T>) threadLocals.get(key);
        if (threadLocal != null) {
            return threadLocal.get();
        }
        return null;
    }
    
    /**
     * Sets value in ThreadLocal with the given key
     */
    @SuppressWarnings("unchecked")
    public static <T> void set(String key, T value) {
        ThreadLocal<T> threadLocal = (ThreadLocal<T>) threadLocals.get(key);
        if (threadLocal != null) {
            threadLocal.set(value);
        } else {
            logger.warn("ThreadLocal with key '{}' does not exist", key);
        }
    }
    
    /**
     * Removes value from ThreadLocal with the given key for current thread
     */
    public static void remove(String key) {
        ThreadLocal<?> threadLocal = threadLocals.get(key);
        if (threadLocal != null) {
            threadLocal.remove();
            logger.debug("Removed ThreadLocal value for key: {} from thread: {}", key, Thread.currentThread().getId());
        }
    }
    
    /**
     * Removes all ThreadLocal values for current thread
     */
    public static void removeAll() {
        long threadId = Thread.currentThread().getId();
        logger.debug("Removing all ThreadLocal values for thread: {}", threadId);
        
        threadLocals.values().forEach(ThreadLocal::remove);
        threadInfo.remove(threadId);
        
        logger.debug("Cleaned up ThreadLocal values for thread: {}", threadId);
    }
    
    /**
     * Registers current thread with a descriptive name
     */
    public static void registerThread(String threadName) {
        long threadId = Thread.currentThread().getId();
        int threadNumber = threadCounter.incrementAndGet();
        String fullThreadName = String.format("%s-Thread-%d", threadName, threadNumber);
        
        threadInfo.put(threadId, fullThreadName);
        Thread.currentThread().setName(fullThreadName);
        
        logger.info("Registered thread: {} (ID: {})", fullThreadName, threadId);
    }
    
    /**
     * Gets the registered name for current thread
     */
    public static String getCurrentThreadName() {
        long threadId = Thread.currentThread().getId();
        return threadInfo.getOrDefault(threadId, "Unknown-Thread-" + threadId);
    }
    
    /**
     * Gets information about all active threads
     */
    public static ConcurrentHashMap<Long, String> getActiveThreads() {
        return new ConcurrentHashMap<>(threadInfo);
    }
    
    /**
     * Gets count of active ThreadLocal instances
     */
    public static int getActiveThreadLocalCount() {
        return threadLocals.size();
    }
    
    /**
     * Gets count of active threads
     */
    public static int getActiveThreadCount() {
        return threadInfo.size();
    }
    
    /**
     * Cleanup all ThreadLocal instances
     */
    public static void cleanupAllThreadLocals() {
        logger.info("Cleaning up {} ThreadLocal instances for {} threads", 
                   threadLocals.size(), threadInfo.size());
        
        // Remove all values from all ThreadLocals
        threadLocals.values().forEach(ThreadLocal::remove);
        
        // Clear the registries
        threadLocals.clear();
        threadInfo.clear();
        
        logger.info("ThreadLocal cleanup completed");
    }
    
    /**
     * Force cleanup for a specific thread (use with caution)
     */
    public static void cleanupThread(long threadId) {
        logger.debug("Force cleaning up ThreadLocal values for thread: {}", threadId);
        
        threadLocals.values().forEach(threadLocal -> {
            try {
                threadLocal.remove();
            } catch (Exception e) {
                logger.warn("Error removing ThreadLocal for thread {}: {}", threadId, e.getMessage());
            }
        });
        
        threadInfo.remove(threadId);
        logger.debug("Force cleanup completed for thread: {}", threadId);
    }
    
    /**
     * Health check for ThreadLocal manager
     */
    public static ThreadLocalHealthInfo getHealthInfo() {
        return new ThreadLocalHealthInfo(
            threadLocals.size(),
            threadInfo.size(),
            threadCounter.get(),
            Thread.activeCount()
        );
    }
    
    /**
     * Health information data class
     */
    public static class ThreadLocalHealthInfo {
        private final int threadLocalCount;
        private final int registeredThreads;
        private final int totalThreadsCreated;
        private final int systemActiveThreads;
        
        public ThreadLocalHealthInfo(int threadLocalCount, int registeredThreads, 
                                   int totalThreadsCreated, int systemActiveThreads) {
            this.threadLocalCount = threadLocalCount;
            this.registeredThreads = registeredThreads;
            this.totalThreadsCreated = totalThreadsCreated;
            this.systemActiveThreads = systemActiveThreads;
        }
        
        public int getThreadLocalCount() { return threadLocalCount; }
        public int getRegisteredThreads() { return registeredThreads; }
        public int getTotalThreadsCreated() { return totalThreadsCreated; }
        public int getSystemActiveThreads() { return systemActiveThreads; }
        
        public boolean isHealthy() {
            // Consider healthy if we don't have too many orphaned ThreadLocals
            return threadLocalCount <= registeredThreads * 2;
        }
        
        @Override
        public String toString() {
            return String.format(
                "ThreadLocalHealth{threadLocals=%d, registeredThreads=%d, totalCreated=%d, systemActive=%d, healthy=%s}",
                threadLocalCount, registeredThreads, totalThreadsCreated, systemActiveThreads, isHealthy()
            );
        }
    }
}