package cn.whiteg.moemaps.commands;

import cn.whiteg.moemaps.MoeMaps;
import cn.whiteg.moemaps.common.HasCommandInterface;
import cn.whiteg.moemaps.utils.ImageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

public class scaling extends HasCommandInterface {
    private final MoeMaps plugin;

    public scaling(MoeMaps plugin) {
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
        if (!file.exists() || file.isDirectory()){
            sender.sendMessage("图片不存在");
            //伸缩
            return false;
        }

        int maxSize;
        if (args.length >= 2){
            try{
                maxSize = Math.max(Integer.parseInt(args[1]),1);
            }catch (NumberFormatException e){
                sender.sendMessage("无效参数: " + args[1]);
                return false;
            }
        } else {
            maxSize = plugin.setting.defaultMaxSize;
        }

        boolean cut;
        if (args.length >= 3){
            try{
                cut = Boolean.parseBoolean(args[2]);
            }catch (NumberFormatException e){
                sender.sendMessage("无效参数: " + args[2]);
                return false;
            }
        } else {
            cut = plugin.setting.defaultCut;
        }

        try{
            try (FileInputStream input = new FileInputStream(file)){
                BufferedImage image = ImageUtils.scalingImage(input,maxSize,cut);
                int type_i = name.lastIndexOf(".");
                if (type_i <= 0) type_i = name.length();
                File out = new File(plugin.imagesDir,name.substring(0,type_i) + "_" + (int) Math.ceil(image.getWidth() / 128f) + "x" + (int) Math.ceil(image.getHeight() / 128f) + name.substring(type_i));
                boolean done = ImageUtils.writeImage(image,file,plugin.setting.compressionQuality);
                sender.sendMessage(done ? "缩放图片已保存至:" + out : "缩放图片输出失败");
                return done;
            }
        }catch (Exception e){
            sender.sendMessage("转换文件过程出现异常");
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.hasPermission("moemaps.command.scaling");
    }

    @Override
    public List<String> complete(CommandSender sender,Command cmd,String label,String[] args) {
        return getMatches(plugin.getFileImageList(),args);
    }

    @Override
    public String getDescription() {
        return "§b自动调整指定图片尺寸，使其能和地图边框对其§f: <图片名> [图片最大大小:默认§7" + plugin.setting.defaultMaxSize + "§f]";
    }
}
