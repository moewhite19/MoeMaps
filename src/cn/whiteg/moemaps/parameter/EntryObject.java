package cn.whiteg.moemaps.parameter;

import java.util.Collections;
import java.util.List;

public abstract class EntryObject<T> implements Entry {
    private final String key;
    private final T def;
    private T current;

    public EntryObject(String key,T def) {
        current = def;
        this.key = key;
        this.def = def;
    }

    public abstract T toCurrent(String current);

    public void set(String str) {
        current = toCurrent(str);
    }

    public T getAndReset() {
        T o = current;
        reset();
        return o;
    }

    public T get() {
        return current;
    }

    public List<String> tab(String str) {
        return Collections.singletonList(String.valueOf(def));
    }

    public void reset() {
        current = def;
    }

    @Override
    public String getKey() {
        return key;
    }
}
