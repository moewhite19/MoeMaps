package cn.whiteg.moemaps.parameter;

import java.util.Collections;
import java.util.List;

public class EntryInt implements Entry {
    private final String key;
    private final String description;
    private final int def;
    private int current;

    public EntryInt(String key,String description,int def) {
        current = def;
        this.key = key;
        this.description = description;
        this.def = def;
    }

    public void set(String str) {
        current = Integer.parseInt(str);
    }

    public List<String> tab(String str) {
        return Collections.singletonList(String.valueOf(def));
    }

    public int getAndReset() {
        int i = current;
        reset();
        return i;
    }

    public int get() {
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
    public String getStringValue() {
        return String.valueOf(current);
    }

    @Override
    public String getDescription() {
        return description;
    }
}
