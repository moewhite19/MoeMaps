package cn.whiteg.moemaps.commands;

import cn.whiteg.moemaps.MoeMaps;
import cn.whiteg.moemaps.common.HasCommandInterface;
import cn.whiteg.moemaps.utils.ImageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import javax.imageio.ImageIO;
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
            return false;
        }

        int maxSize;
        if (args.length > 2){
            try{
                maxSize = Integer.parseInt(args[1]);
            }catch (NumberFormatException e){
                sender.sendMessage("无效参数: " + args[1]);
                return false;
            }
        } else {
            maxSize = 1024;
        }

        try{
            try (FileInputStream input = new FileInputStream(file)){
                BufferedImage image = ImageUtils.automaticScaling(input,maxSize);
                if (image == null){
                    sender.sendMessage("未知原因打开失败");
                    return false;
                }

                boolean isARGB = image.getType() == BufferedImage.TYPE_4BYTE_ABGR;
                File out = new File(plugin.imagesDir,name + "_" + image.getWidth() + "_" + image.getHeight() + (isARGB ? ".png" : ".jpg"));
                boolean write = ImageIO.write(image,isARGB ? "PNG" : "JPEG",out);
                sender.sendMessage(write ? "缩放图片已保存至:" + out : "缩放图片输出失败");
                return write;
            }
        }catch (Exception e){
            sender.sendMessage("打开文件出现异常");
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
        return "自动缩放指定图片: <图片名> [图片最大大小:默认1024]";
    }
}
