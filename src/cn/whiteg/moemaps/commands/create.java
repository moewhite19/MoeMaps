package cn.whiteg.moemaps.commands;

import cn.whiteg.moemaps.ImageMap;
import cn.whiteg.moemaps.MoeMaps;
import cn.whiteg.moemaps.common.HasCommandInterface;
import cn.whiteg.moemaps.parameter.Parameter;
import cn.whiteg.moemaps.utils.ImageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.awt.image.BufferedImage;
import java.util.List;

public class create extends HasCommandInterface {
    private final MoeMaps plugin;
    Parameter parameter;


    public create(MoeMaps plugin) {
        this.plugin = plugin;
        parameter = new Parameter();
        parameter.add(plugin.setting.maxSize);
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
        parameter.apply(args,1);
        int size = plugin.setting.maxSize.getAndReset();
        boolean cut = plugin.setting.cut.getAndReset();
        try{
            ImageMap existing = plugin.getMapFormName(name);
            if (existing != null){
                BufferedImage image = ImageUtils.scalingImage(plugin.readImage(name),existing.getWight() * 128,existing.getHigh() * 128,cut);
                existing.rewrite(image);
            } else {
                BufferedImage image = ImageUtils.scalingImage(plugin.readImage(name),size,cut);
                ImageMap imageMap = ImageMap.create(image);
                int index = name.lastIndexOf('.');
                name = index == -1 ? name : name.substring(0,index);
                plugin.putMap(name + "_" + imageMap.getWight() + "x" + imageMap.getHigh(),imageMap,true);
            }
        }catch (Exception exception){
            sender.sendMessage(exception.getMessage());
        }

        return false;
    }

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.hasPermission("moemaps.command.get");
    }

    @Override
    public List<String> complete(CommandSender sender,Command cmd,String label,String[] args) {
        if (args.length == 1){
            return getMatches(plugin.getFullImageList(),args);
        } else if (args.length == 2){
            return getPlayersList(args);
        }
        return null;
    }

    @Override
    public String getDescription() {
        return "从文件创建地图:§7 <图片文件> " + parameter.getDescription();
    }
}
