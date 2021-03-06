package cn.whiteg.moemaps.parameter;

import java.util.List;

public interface Entry {
    void set(String str);

    List<String> tab(String arg);

    void reset();

    String getKey();

    String getStringValue();

    default String getDescription() {
        return getKey();
    }
}
