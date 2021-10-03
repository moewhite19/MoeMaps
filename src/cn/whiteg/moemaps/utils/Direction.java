package cn.whiteg.moemaps.utils;

public enum Direction {

    UP("上"),DOWN("下"),
    LEFT("左"),RIGHT("右"),
    MIDDLE("中");
    private final String locale;

    Direction(String locale) {
        this.locale = locale;
    }

    public static Direction getFromLocale(String key) {
        for (Direction value : values()) {
            if (value.locale.equals(key)) return value;
        }
        return Direction.valueOf(key);
    }

    public String getLocale() {
        return locale;
    }

    @Override
    public String toString() {
        return getLocale();
    }
}
