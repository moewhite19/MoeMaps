package cn.whiteg.moemaps;

import cn.whiteg.moemaps.common.CommandManage;
import cn.whiteg.moemaps.common.PluginBase;
import cn.whiteg.moemaps.listener.AutoSaveListener;
import cn.whiteg.moemaps.utils.Downloader;
import cn.whiteg.moemaps.utils.ReturnCache;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;


public class MoeMaps extends PluginBase implements Listener {
    public static MoeMaps plugin;
    public final File imagesDir;
    private final ReturnCache<List<String>> loadImageListCache;
    private final ReturnCache<List<String>> fileImageListCache;
    private final ReturnCache<List<String>> allImageListCache;
    public Logger logger;
    public CommandManage mainCommand;
    public Setting setting;
    public Downloader downloader = null;
    Map<String, ImageMap> imageMaps = new HashMap<>();
    private Economy economy;
    private AutoSaveListener autoSaveListener;
    private boolean mmocoreSupport;
    private boolean residenceSupport;

    public MoeMaps() {
        plugin = this;
        logger = getLogger();
        imagesDir = new File(plugin.getDataFolder(),"images");
        loadImageListCache = new ReturnCache<>(200) {
            @Override
            public List<String> update() {
                return new ArrayList<>(imageMaps.keySet());
            }
        };

        fileImageListCache = new ReturnCache<>(200) {
            @Override
            public List<String> update() {
                var files = imagesDir.list();
                if (files == null) return null;
                return Arrays.asList(files);
            }
        };

        allImageListCache = new ReturnCache<>() {
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

        autoSaveListener = new AutoSaveListener(this);

        logger.info("全部加载完成" + imageMaps.size());
        Bukkit.getScheduler().runTask(this,() -> {
            if (Bukkit.getPluginManager().getPlugin("Vault") != null){
                RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
                if (economyProvider != null){
                    this.economy = economyProvider.getProvider();
                }
            }

            mmocoreSupport = Bukkit.getPluginManager().isPluginEnabled("MMOCore");
            residenceSupport = Bukkit.getPluginManager().isPluginEnabled("Residence");
        });
    }

    public void onDisable() {
        //注销所有监听器
        if (downloader != null) downloader.close();
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
            autoSaveListener.setChange(true);
        }
    }

    public boolean removeMap(String name,boolean save) {
        var image = imageMaps.remove(name);
        if (image != null){
            image.reset();
            if (save){
                getMapStore().set(name,null);
                autoSaveListener.setChange(true);
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

    public BufferedImage readImage(String name) {
        var file = new File(imagesDir,name);
        if (!file.exists() || file.isDirectory()) throw new IllegalArgumentException("找不到图片" + file);
        try{
            return ImageIO.read(file);
        }catch (IOException e){
            throw new IllegalArgumentException(e);
        }
    }

    public List<String> getMapImageList() {
        return loadImageListCache.get();
    }

    public List<String> getFileImageList() {
        return fileImageListCache.get();
    }

    public List<String> getFullImageList() {
        return allImageListCache.get();
    }

    public Economy getEconomy() {
        return economy;
    }

    public boolean hasResidence() {
        return residenceSupport;
    }

    //是否启用多人指令
    public boolean hasMultiPlayer() {
        return mmocoreSupport && economy != null;
    }
}
