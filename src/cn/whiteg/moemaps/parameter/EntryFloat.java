package cn.whiteg.moemaps.parameter;

import java.util.Collections;
import java.util.List;

public class EntryFloat implements Entry {
    private final String key;
    private final String description;
    private final float def;
    private float current;

    public EntryFloat(String key ,String description,float def) {
        current = def;
        this.key = key;
        this.description = description;
        this.def = def;
    }

    public void set(String str) {
        current = Float.parseFloat(str);
    }

    public List<String> tab(String str) {
        return Collections.singletonList(String.valueOf(def));
    }

    public float getAndReset() {
        float f = current;
        reset();
        return f;
    }

    public float get() {
        return current;
    }

    public void reset() {
        current = def;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
