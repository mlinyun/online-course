package com.mlinyun.onlinecourse.basics.utils;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池配置工具
 */
public class ThreadPoolUtil {

    @Schema(description = "线程的缓冲队列")
    private static BlockingQueue<Runnable> bqueue = new ArrayBlockingQueue<Runnable>(100);

    @Schema(description = "核心线程数")
    private static final int SIZE_CORE_POOL = 5;

    @Schema(description = "最大线程数量")
    private static final int SIZE_MAX_POOL = 10;

    @Schema(description = "空闲线程存活时间")
    private static final long ALIVE_TIME = 2000;

    private static ThreadPoolExecutor pool = new ThreadPoolExecutor(SIZE_CORE_POOL, SIZE_MAX_POOL, ALIVE_TIME, TimeUnit.MILLISECONDS, bqueue, new ThreadPoolExecutor.CallerRunsPolicy());

    static {
        pool.prestartAllCoreThreads();
    }

    @Schema(description = "获取可用线程")
    public static ThreadPoolExecutor getPool() {
        return pool;
    }

}
