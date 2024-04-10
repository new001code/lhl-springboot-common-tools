package com.lhlwork.aop;

import com.lhlwork.anno.TimeRecorder;
import com.lhlwork.tool.TimeRecorderInterface;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

@Component
@Aspect
@Slf4j
public class TimeRecorderAop {


    @Getter
    static class TimeRecorderThreadPool {
        static class TimeRecorderThreadFactory implements ThreadFactory {
            @Override
            public Thread newThread(@NonNull Runnable r) {
                Thread t = new Thread(r);
                t.setName("TimeRecorder");
                t.setDaemon(true);
                return t;
            }
        }

        // 使用volatile关键字保证多线程间的可见性
        private volatile static TimeRecorderThreadPool instance;
        // 提供获取线程池的方法
        private final ExecutorService threadPool;

        // 私有化构造函数
        private TimeRecorderThreadPool() {
            threadPool = new ThreadPoolExecutor(1, 1, 1L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100), new TimeRecorderThreadFactory(), new ThreadPoolExecutor.DiscardOldestPolicy());

        }

        // 提供获取实例的方法
        public static TimeRecorderThreadPool getInstance() {
            if (instance == null) {
                synchronized (TimeRecorderThreadPool.class) {
                    if (instance == null) {
                        instance = new TimeRecorderThreadPool();
                    }
                }
            }
            return instance;
        }

    }


    @Around("@annotation(timeRecorder)")
    public Object around(ProceedingJoinPoint proceedingJoinPoint, TimeRecorder timeRecorder) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return proceedingJoinPoint.proceed();
        } finally {
            long end = System.currentTimeMillis();
            long t = end - start;
            if (t >= timeRecorder.targetTime()) {
                Class<? extends TimeRecorderInterface> recorder = timeRecorder.recorder();
                TimeRecorderInterface timeRecorderInterface = recorder.getConstructor().newInstance();
                if (timeRecorder.async()) {
                    TimeRecorderThreadPool.getInstance().getThreadPool().execute(() -> timeRecorderInterface.record(proceedingJoinPoint.getTarget().getClass().getCanonicalName() + "." + proceedingJoinPoint.getSignature().getName(), t));
                } else {
                    timeRecorderInterface.record(proceedingJoinPoint.getTarget().getClass().getCanonicalName() + "." + proceedingJoinPoint.getSignature().getName(), t);
                }
            }
        }
    }
}
