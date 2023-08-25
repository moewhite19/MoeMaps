package cn.whiteg.moemaps.commands;

import cn.whiteg.moemaps.MoeMaps;
import cn.whiteg.moemaps.common.HasCommandInterface;
import cn.whiteg.moemaps.parameter.Parameter;
import cn.whiteg.moemaps.utils.ImageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class rewrite extends HasCommandInterface {
    private final MoeMaps plugin;
    private final Parameter parameter;

    public rewrite(MoeMaps plugin) {
        this.plugin = plugin;
        parameter = new Parameter();
        parameter.add(plugin.setting.cut);
        parameter.decide();
    }

    @Override
    public boolean executo(CommandSender sender,Command cmd,String str,String[] args) {
        if (args.length < 1){
            sender.sendMessage(getDescription());
            return false;
        }
        String name = args[0];
        String fileName;
        if (args.length >= 2){
            fileName = args[1];
        } else {
            fileName = name;
        }
        try{
            var map = plugin.getMapFormName(name);
            if (map == null){
                sender.sendMessage(" §b找不到地图: §f" + name);
                return false;
            }
            parameter.apply(args,2);
            boolean cut = plugin.setting.cut.getAndReset();
            var image = ImageUtils.scalingImage(plugin.readImage(fileName),map.getHigh() * 128,map.getHigh() * 128,cut);
            map.rewrite(image);
            sender.sendMessage("§b已重新写入地图 §f" + name);
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
        if (args.length == 1) return getMatches(plugin.getMapImageList(),args);
        else if (args.length == 2) return getMatches(plugin.getFileImageList(),args);
        return parameter.tab(args );
    }

    @Override
    public String getDescription() {
        return "重写指定地图:§7<地图> <文件> " + parameter.getDescription();
    }
}
