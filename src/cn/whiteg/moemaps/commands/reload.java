package cn.whiteg.moemaps.commands;

import cn.whiteg.moemaps.MoeMaps;
import cn.whiteg.moemaps.common.HasCommandInterface;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class reload extends HasCommandInterface {
    private final MoeMaps plugin;

    public reload(MoeMaps plugins) {
        this.plugin = plugins;
    }

    @Override
    public boolean executo(CommandSender sender,Command cmd,String str,String[] args) {
        plugin.onReload();
        sender.sendMessage("§b重载完成");
        return true;
    }

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.hasPermission("moemaps.command.reload");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender,Command cmd,String label,String[] args) {
        return null;
    }
}
