package cn.whiteg.moemaps;

import cn.whiteg.moemaps.common.CommandManage;
import cn.whiteg.moemaps.common.PluginBase;
import cn.whiteg.moemaps.utils.ReturnCache;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.RegisteredServiceProvider;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;


public class MoeMaps extends PluginBase {
    public static MoeMaps plugin;
    public final File imagesDir;
    private final ReturnCache<List<String>> mapsCache;
    public Logger logger;
    public CommandManage mainCommand;
    public Setting setting;
    Map<String, ImageMap> imageMaps = new HashMap<>();
    private Economy economy;

    public MoeMaps() {
        plugin = this;
        logger = getLogger();
        imagesDir = new File(plugin.getDataFolder(),"images");
        mapsCache = new ReturnCache<>() {
            @Override
            public List<String> update() {
                var set = new HashSet<>(imageMaps.keySet());
                var files = imagesDir.list();
                if (files != null) set.addAll(Arrays.asList(files));
                return new ArrayList<>(set);
            }
        };
    }

    public void onLoad() {
        saveDefaultConfig();
    }

    public void onEnable() {
        logger.info("开始加载插件");
        if (!imagesDir.exists()) imagesDir.mkdir();
        setting = new Setting(plugin);
        if (setting.DEBUG) logger.info("§a调试模式已开启");
        PluginCommand pc = getCommand(getName().toLowerCase());
        if (pc != null){
            mainCommand = new CommandManage(this);
            pc.setExecutor(mainCommand);
            pc.setTabCompleter(mainCommand);
        } else {
            logger.info("没用注册指令(忘记添加指令到plugin.yml啦?)");
        }
        loadMaps();

        logger.info("全部加载完成" + imageMaps.size());
        Bukkit.getScheduler().runTask(this,() -> {
            if (Bukkit.getPluginManager().getPlugin("Vault") != null){
                RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
                if (economyProvider != null){
                    this.economy = economyProvider.getProvider();
                }
            }
        });
    }

    public void onDisable() {
        //注销所有监听器
        unregListener();
        logger.info("插件已关闭");
    }

    public void onReload() {
        logger.info("--开始重载--");
        setting.reload();
        imageMaps.clear();
        loadMaps();
        logger.info("--重载完成--");
    }

    private void loadMaps() {
        ConfigurationSection cs = getMapStore();
        var keys = cs.getKeys(false);
        for (String key : keys) {
            var ms = cs.getConfigurationSection(key);
            if (ms == null) continue;
            try{
                putMap(key,ImageMap.deserialize(ms),false);
            }catch (Exception exception){
                exception.printStackTrace();
            }
        }
    }

    public ConfigurationSection getMapStore() {
        var cs = setting.getStorage().getConfigurationSection("maps");
        if (cs == null) cs = setting.getStorage().createSection("maps");
        return cs;
    }

    public void putMap(String name,ImageMap imageMap,boolean save) {
        imageMaps.put(name,imageMap);
        if (save){
            var ms = getMapStore().createSection(name);
            imageMap.serialize(ms);
            setting.saveStorage();
        }
    }

    public boolean removeMap(String name,boolean save) {
        if (imageMaps.remove(name) != null){
            if (save){
                getMapStore().set(name,null);
                setting.saveStorage();
            }
            return true;
        }
        return false;
    }

    public ImageMap getMapFormName(String name) {
        return imageMaps.get(name);
    }

    public Map<String, ImageMap> getImageMap() {
        return imageMaps;
    }

    //用名字获取地图
    public ImageMap createMap(String name) {
        ImageMap map = getMapFormName(name);
        if (map == null){
            BufferedImage image = plugin.readImage(name);
            map = ImageMap.create(image);
            putMap(name,map,true);
        }
        return map;
    }

    public BufferedImage readImage(String name) {
        var file = new File(imagesDir,name);
        if (!file.exists() || file.isDirectory()) throw new IllegalArgumentException("找不到图片" + file);
        try{
            return ImageIO.read(file);
        }catch (IOException e){
            throw new IllegalArgumentException(e);
        }
    }

    //获取所有可用图片
    public List<String> getAllMaps() {
        return mapsCache.get();
    }

    public Economy getEconomy() {
        return economy;
    }
}
