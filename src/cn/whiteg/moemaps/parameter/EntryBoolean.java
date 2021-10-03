package cn.whiteg.moemaps.parameter;

import java.util.Collections;
import java.util.List;

public class EntryBoolean implements Entry {
    private final String key;
    private final String description;
    private final boolean def;
    private boolean current;

    public EntryBoolean(String key,String description,boolean def) {
        current = def;
        this.key = key;
        this.description = description;
        this.def = def;
    }

    public void set(String str) {
        current = Byte.parseByte(str) > 0;
    }

    public List<String> tab(String str) {
        return Collections.singletonList(def ? "1" : "0");
    }

    public boolean getAndReset() {
        boolean b = current;
        reset();
        return b;
    }

    public boolean get() {
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
        return current ? "0" : "1";
    }

    @Override
    public String getDescription() {
        return description;
    }
}
