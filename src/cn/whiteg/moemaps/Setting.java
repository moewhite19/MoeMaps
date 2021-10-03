package cn.whiteg.moemaps;

import cn.whiteg.moemaps.parameter.EntryBoolean;
import cn.whiteg.moemaps.parameter.EntryFloat;
import cn.whiteg.moemaps.parameter.EntryInt;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class Setting {
    private final static int CONFIGVER = 3;
    private static FileConfiguration storage;
    private final MoeMaps plugin;
    public boolean DEBUG;
    public float compressionQuality; //图片压缩质量
    public int defaultMaxSize; //默认最大图片大小
    public boolean defaultCut; //默认剪裁图片
    public double ugetPrice; //单个地图
    public EntryInt maxSize;
    public EntryBoolean cut;
    public EntryFloat quality;

    public Setting(MoeMaps plugin) {
        this.plugin = plugin;
        reload();
        //下面的东西重载也没用呢，那就只重载一次吧
        maxSize = new EntryInt("size","地图大小",defaultMaxSize);
        cut = new EntryBoolean("cut","剪裁模式",defaultCut);
        quality = new EntryFloat("quality","输出质量",compressionQuality);
    }


    public void reload() {
        File file = new File(MoeMaps.plugin.getDataFolder(),"config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        //自动更新配置文件
        if (config.getInt("ver") < CONFIGVER){
            plugin.saveResource("config.yml",true);
            config.set("ver",CONFIGVER);
            final FileConfiguration newcon = YamlConfiguration.loadConfiguration(file);
            Set<String> keys = newcon.getKeys(true);
            for (String k : keys) {
                if (config.isSet(k)) continue;
                config.set(k,newcon.get(k));
                plugin.logger.info("新增配置节点: " + k);
            }
            try{
                config.save(file);
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        DEBUG = config.getBoolean("debug");

        compressionQuality = Math.min(1f,Math.max(0f,(float) config.getDouble("CompressionQuality",1d)));
        defaultMaxSize = Math.max(1,config.getInt("DefaultMaxSize",6));
        defaultCut = config.getBoolean("DefaultCut",false);
        ugetPrice = config.getDouble("UgetPrice",128);


        storage = new YamlConfiguration();
        storage.options().pathSeparator('/');
        file = new File(file.getParentFile(),"storage.yml");
        try{
            if (file.exists()) storage.load(file);
        }catch (IOException | InvalidConfigurationException e){
            e.printStackTrace();
        }
    }

    public void saveStorage() {
        File file = new File(plugin.getDataFolder(),"storage.yml");
        try{
            storage.save(file);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public FileConfiguration getStorage() {
        return storage;
    }
}
