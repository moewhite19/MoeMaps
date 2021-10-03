package cn.whiteg.moemaps.commands;

import cn.whiteg.moemaps.MoeMaps;
import cn.whiteg.moemaps.common.HasCommandInterface;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class remove extends HasCommandInterface {
    private final MoeMaps plugin;

    public remove(MoeMaps plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean executo(CommandSender sender,Command cmd,String str,String[] args) {
        if (args.length < 1){
            sender.sendMessage(getDescription());
            return false;
        }
        String name = args[0];
        sender.sendMessage((plugin.removeMap(name,true) ? "§b移除地图: §f" : "§b找不到地图§f") + name);
        return false;
    }

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.hasPermission("moemaps.command.removeimage");
    }

    @Override
    public List<String> complete(CommandSender sender,Command cmd,String label,String[] args) {
        return getMatches(new ArrayList<>(plugin.getMapImageList()),args);
    }

    @Override
    public String getDescription() {
        return "移除地图:§7 <地图>";
    }
}
