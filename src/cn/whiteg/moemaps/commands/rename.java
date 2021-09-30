package cn.whiteg.moemaps.commands;

import cn.whiteg.moemaps.MoeMaps;
import cn.whiteg.moemaps.common.HasCommandInterface;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.List;

public class rename extends HasCommandInterface {
    private final MoeMaps plugin;

    public rename(MoeMaps plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean executo(CommandSender sender,Command cmd,String str,String[] args) {
        if (args.length < 2){
            sender.sendMessage(getDescription());
            return false;
        }
        String name = args[0];
        String newName = args[1];
        StringBuilder sb = new StringBuilder(" §b");
        var image = plugin.getMapFormName(name);
        if (image != null){
            var map = plugin.getImageMap();
            map.remove(name);
            map.put(newName,image);
            var store = plugin.getMapStore();
            store.set(newName,store.get(name));
            store.set(name,null);
            sb.append("已重命名图片，");
            plugin.setting.saveStorage();
        }

        var file = new File(plugin.imagesDir,name);
        if (file.exists() && file.renameTo(new File(plugin.imagesDir,newName))){
            sb.append("已重命名文件");
        }
        boolean flag = sb.length() > 8;
        sender.sendMessage(flag ? sb.toString() : " §c没有找到图片");
        return false;
    }

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.hasPermission("moemaps.command.rename");
    }

    @Override
    public List<String> complete(CommandSender sender,Command cmd,String label,String[] args) {
        return plugin.getAllImageList();
    }

    @Override
    public String getDescription() {
        return "重命名地图: <图片名称> <新图片名称>";
    }
}
