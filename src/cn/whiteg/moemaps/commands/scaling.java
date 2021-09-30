package cn.whiteg.moemaps.commands;

import cn.whiteg.moemaps.MoeMaps;
import cn.whiteg.moemaps.common.HasCommandInterface;
import cn.whiteg.moemaps.utils.ImageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
        if (args.length >= 2){
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
                int type_i = name.lastIndexOf(".");
                if (type_i <= 0) type_i = name.length();
                File out = new File(plugin.imagesDir,name.substring(0,type_i) + "_" + image.getWidth() + "x" + image.getHeight() + name.substring(type_i));
                boolean isARGB = image.getType() == BufferedImage.TYPE_4BYTE_ABGR;
                boolean done = false;
                try{
                    ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName(isARGB ? "png" : "jpg").next();
                    ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
                    jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    jpgWriteParam.setCompressionQuality(plugin.setting.compressionQuality);
                    jpgWriter.setOutput(ImageIO.createImageOutputStream(out));
                    IIOImage outputImage = new IIOImage(image,null,null);
                    jpgWriter.write(null,outputImage,jpgWriteParam);
                    jpgWriter.dispose();
                    done = true;
                }catch (IOException e){
                    e.printStackTrace();
                    done = false;
                }
//                boolean write = ImageIO.write(image,isARGB ? "PNG" : "JPEG",out);
                sender.sendMessage(done ? "缩放图片已保存至:" + out : "缩放图片输出失败");
                return done;
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
        return "自动调整指定图片尺寸，使其能和地图边框对其: <图片名> [图片最大大小:默认1024]";
    }
}
