package cn.whiteg.moemaps.commands;

import cn.whiteg.moemaps.MoeMaps;
import cn.whiteg.moemaps.common.HasCommandInterface;
import cn.whiteg.moemaps.utils.ImageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collections;
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


        int maxSize = plugin.setting.defaultMaxSize;
        boolean cut = plugin.setting.defaultCut;
        float quality = plugin.setting.compressionQuality;
        if (args.length > 1){
            for (int i = 1; i < args.length; i++) {
                String arg = args[args.length - 1];
                if (arg == null || arg.length() < 3){
                    sender.sendMessage("无效参数: " + arg);
                    return false;
                }
                char key = arg.charAt(0);
                String o = arg.substring(2);
                try{
                    if (key == 'm'){ //最大地图
                        maxSize = Math.max(Integer.parseInt(o),1);
                    } else if (key == 'c'){ //剪裁
                        cut = Short.parseShort(o) > 0;
                    } else if (key == 'q'){ //质量
                        quality = Math.max(Math.min(Float.parseFloat(o),1f),0f);
                    }
                }catch (NumberFormatException e){
                    sender.sendMessage("无效参数: " + arg);
                    return false;
                }
            }
        }

        try (FileInputStream input = new FileInputStream(file)){
            BufferedImage image = ImageUtils.scalingImage(input,maxSize,cut);
            int type_i = name.lastIndexOf(".");
            if (type_i <= 0) type_i = name.length();
            File out = new File(plugin.imagesDir,name.substring(0,type_i) + "_" + (int) Math.ceil(image.getWidth() / 128f) + "x" + (int) Math.ceil(image.getHeight() / 128f) + name.substring(type_i));
            boolean done = ImageUtils.writeImage(image,out,quality);
            sender.sendMessage(done ? "缩放图片已保存至:" + out : "缩放图片输出失败");
            return done;

        }catch (Exception e){
            sender.sendMessage("转换图片过程出现异常");
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
        if (args.length == 1){
            return getMatches(plugin.getFileImageList(),args);
        } else {
            String arg = args[args.length - 1];
            if (arg == null || arg.isEmpty()){
                return getMatches(arg,Arrays.asList("m:","c:","q:"));
            } else if (arg.startsWith("m")){
                return Collections.singletonList("m:" + plugin.setting.defaultMaxSize);
            } else if (arg.startsWith("c")){
                return getMatches(args,Collections.singletonList("c:" + (plugin.setting.defaultCut ? 1 : 0)));
            } else if (arg.startsWith("q")){
                return Collections.singletonList("m:" + plugin.setting.compressionQuality);
            }
        }
        return null;
    }

    @Override
    public String getDescription() {
        return "§b自动调整指定图片尺寸，使其能和地图边框对其§f: <图片名> [m:最大尺寸] [q:质量] [c:剪裁模式]";
    }
}
