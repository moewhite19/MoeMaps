package cn.whiteg.moemaps.utils;

import java.lang.ref.WeakReference;

public abstract class ReturnCache<T> {
    final long interval;
    WeakReference<T> cache = new WeakReference<>(null);
    long time = 0;

    public ReturnCache(long interval) {
        this.interval = interval;
    }

    public ReturnCache() {
        this.interval = 10000L;
    }

    //更新缓存
    public abstract T update();

    public T get() {
        T obj = cache.get();
        if (obj == null || System.currentTimeMillis() > time){
            obj = update();
            cache = new WeakReference<>(obj);
            time = System.currentTimeMillis() + interval;
        }
        return obj;
    }
}
