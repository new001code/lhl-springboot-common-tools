package com.lhlwork.tool;

import com.lhlwork.exception.ServiceException;

import java.util.function.BiFunction;
import java.util.function.Function;

public class AssertThrowExceptionUtil {

    /**
     * 单例模式实现，采用线程安全的懒汉模式。
     * 注意：由于是静态实例，测试时可能需要额外的策略来替换或模拟实例。
     */
    private static volatile AssertThrowExceptionUtil INSTANCE;

    /**
     * 获取AssertThrowExceptionUtil的单例实例。
     * 该方法是线程安全的，采用双重检查锁定实现延迟加载。
     *
     * @return AssertThrowExceptionUtil的单例实例
     */
    public static AssertThrowExceptionUtil getInstance() {
        if (INSTANCE == null) {
            synchronized (AssertThrowExceptionUtil.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AssertThrowExceptionUtil();
                }
            }
        }
        return INSTANCE;
    }

    public static <F, E extends Throwable> void assertThrowException(Function<F, Boolean> function, F param, E exception) throws E {
        if (!function.apply(param)) {
            throw exception;
        }
    }

    public static <F> void assertThrowException(Function<F, Boolean> function, F param, String message) throws ServiceException {
        if (!function.apply(param)) {
            throw new ServiceException(message);
        }
    }

    public static <F, G, E extends Throwable> void assertThrowException(BiFunction<F, G, Boolean> function, F param, G param2, String message, E exception) throws E {
        if (!function.apply(param, param2)) {
            throw exception;
        }
    }

    public static <F, G> void assertThrowException(BiFunction<F, G, Boolean> function, F param, G param2, String message) throws ServiceException {
        if (!function.apply(param, param2)) {
            throw new ServiceException(message);
        }
    }

    public <F> AssertThrowExceptionUtil multiAssertThrowException(Function<F, Boolean> function, F param, String message) throws ServiceException {
        if (!function.apply(param)) {
            throw new ServiceException(message);
        }
        return this;
    }


    public <F, G> AssertThrowExceptionUtil multiAssertThrowException(BiFunction<F, G, Boolean> function, F param, G param2, String message) throws ServiceException {
        if (!function.apply(param, param2)) {
            throw new ServiceException(message);
        }
        return this;
    }

    public <F, E extends Throwable> AssertThrowExceptionUtil multiAssertThrowException(Function<F, Boolean> function, F param, E exception) throws E {
        if (!function.apply(param)) {
            throw exception;
        }
        return this;
    }

    public <F, G, E extends Throwable> AssertThrowExceptionUtil multiAssertThrowException(BiFunction<F, G, Boolean> function, F param, G param2, E exception) throws E {
        if (!function.apply(param, param2)) {
            throw exception;
        }
        return this;
    }


}
