package cn.whiteg.moemaps.listener;

import cn.whiteg.moemaps.MoeMaps;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldSaveEvent;

public class AutoSaveListener implements Listener {
    private final MoeMaps plugin;
    boolean change = false;

    public AutoSaveListener(MoeMaps plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSave(WorldSaveEvent event) {
        if (change){
            plugin.setting.saveStorage();
            change = false;
        }
    }

    public void setChange(boolean change) {
        this.change = change;
    }
}
