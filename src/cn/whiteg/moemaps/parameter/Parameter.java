package cn.whiteg.moemaps.parameter;

import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parameter {
    Map<String, Entry> entries = new HashMap<>();
    List<String> keys;

    public <T extends Entry> T add(T entry) {
        entries.put(entry.getKey(),entry);
        return entry;
    }

    public void apply(String[] args,int pos) {
        if (args.length <= pos) return;
        for (int i = pos; i < args.length; i++) {
            String str = args[i];
            int in = str.indexOf(':');
            if (in <= 0 || in == str.length() - 1) throw new IllegalArgumentException("无效参数: " + str);
            String key = str.substring(0,in);
            String valve = str.substring(in + 1);
            Entry entry = entries.get(key);
            if (entry != null){
                try{
                    entry.set(valve);
                }catch (Exception exception){
                    throw new IllegalArgumentException("参数异常" + key + ":" + exception.getMessage());
                }
            } else {
                throw new IllegalArgumentException("未知参数:" + key);
            }
        }
    }

    public void resetAll() {
        entries.forEach((s,entry) -> {
            entry.reset();
        });
    }

    public List<String> tab(String[] args) {
        if (args.length == 0) return new ArrayList<>();
        String late = args[args.length - 1];
        int in = late.indexOf(':');
        String key;
        String valve;
        if (in <= 0){
            key = late;
            valve = "";
        } else {
            key = late.substring(0,in);
            valve = late.substring(in + 1);
        }
        Entry entry = entries.get(key);
        if (entry == null) return keys;
        List<String> backs = entry.tab(valve);
        if (backs == null || backs.isEmpty()) return keys;
        List<String> tabs = new ArrayList<>(backs.size());
        for (String back : backs) {
            String keyAndBAck = key.concat(":").concat(back);
            if (keyAndBAck.startsWith(late)){
                tabs.add(keyAndBAck);
            }
        }
        return tabs;
    }

    //完成设定
    public void decide() {
        entries = ImmutableMap.copyOf(entries);
        keys = new ArrayList<>(entries.keySet());
        for (int i = 0; i < keys.size(); i++) {
            keys.set(i,keys.get(i) + ":");
        }
    }

    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        for (Entry value : entries.values()) {
            sb.append('[').append(value.getKey()).append(':').append(value.getDescription()).append("] ");
        }
        return sb.toString();
    }

}