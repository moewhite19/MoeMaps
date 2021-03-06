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
        logger.info("??????????????????");
        if (!imagesDir.exists()) imagesDir.mkdir();
        setting = new Setting(plugin);
        if (setting.DEBUG) logger.info("??a?????????????????????");
        PluginCommand pc = getCommand(getName().toLowerCase());
        if (pc != null){
            mainCommand = new CommandManage(this);
            pc.setExecutor(mainCommand);
            pc.setTabCompleter(mainCommand);
        } else {
            logger.info("??????????????????(?????????????????????plugin.yml????)");
        }
        loadMaps();

        autoSaveListener = new AutoSaveListener(this);

        logger.info("??????????????????" + imageMaps.size());
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
        //?????????????????????
        if (downloader != null) downloader.close();
        autoSaveListener.onSave(null);
        unregListener();
        logger.info("???????????????");
    }

    public void onReload() {
        logger.info("--????????????--");
        autoSaveListener.onSave(null);
        setting.reload();
        imageMaps.clear();
        loadMaps();
        logger.info("--????????????--");
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
        if (!file.exists() || file.isDirectory()) throw new IllegalArgumentException("???????????????" + file);
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

    //????????????????????????
    public boolean hasMultiPlayer() {
        return mmocoreSupport && economy != null;
    }
}
