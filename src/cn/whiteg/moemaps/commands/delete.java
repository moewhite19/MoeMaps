package cn.whiteg.moemaps.commands;

import cn.whiteg.moemaps.MoeMaps;
import cn.whiteg.moemaps.common.HasCommandInterface;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.List;

public class delete extends HasCommandInterface {
    private final MoeMaps plugin;

    public delete(MoeMaps plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean executo(CommandSender sender,Command cmd,String str,String[] args) {
        if (args.length < 1){
            sender.sendMessage(getDescription());
            return false;
        }
        String name = args[0];
        var file = new File(plugin.imagesDir,name);
        if (!file.exists()){
            sender.sendMessage(" §b文件不存在");
            return false;
        }
        var back = file.delete();
        sender.sendMessage((back ? " §b已删除文件: §f" : " §b未删除§f ") + file);
        return back;
    }

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.hasPermission("moemaps.command.delete");
    }

    @Override
    public List<String> complete(CommandSender sender,Command cmd,String label,String[] args) {
        if (args.length == 1){
            return getMatches(plugin.getFileImageList(),args);
        }
        return null;
    }

    @Override
    public String getDescription() {
        return "删除图片:§7<文件名>";
    }
}
