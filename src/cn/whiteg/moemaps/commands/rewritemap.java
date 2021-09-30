package cn.whiteg.moemaps.commands;

import cn.whiteg.moemaps.MoeMaps;
import cn.whiteg.moemaps.common.HasCommandInterface;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class rewritemap extends HasCommandInterface {
    private final MoeMaps plugin;

    public rewritemap(MoeMaps plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean executo(CommandSender sender,Command cmd,String str,String[] args) {
        if (args.length < 1){
            sender.sendMessage(getDescription());
            return false;
        }
        String name = args[0];
        try{
            var map = plugin.getMapFormName(name);
            var image = plugin.readImage(name);
            map.rewrite(image);
            sender.sendMessage("已重新写入地图" + name);
        }catch (IllegalArgumentException exception){
            sender.sendMessage(exception.getMessage());
        }
        return false;
    }

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.hasPermission("moemaps.command.rewritemap");
    }

    @Override
    public List<String> complete(CommandSender sender,Command cmd,String label,String[] args) {
        return getMatches(new ArrayList<>(plugin.getImageMap().keySet()),args);
    }

    @Override
    public String getDescription() {
        return "重写指定地图(保留现有id)，当获取到的地图不是指定图片时使用（图片丢失） : <图片>";
    }
}
