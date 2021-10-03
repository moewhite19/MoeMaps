package cn.whiteg.moemaps.commands;

import cn.whiteg.moemaps.MoeMaps;
import cn.whiteg.moemaps.common.HasCommandInterface;
import cn.whiteg.moemaps.parameter.Parameter;
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
    private final Parameter parameter;

    public scaling(MoeMaps plugin) {
        this.plugin = plugin;
        parameter = new Parameter();
        var setting = plugin.setting;
        parameter.add(setting.maxSize);
        parameter.add(setting.cut);
        parameter.add(setting.quality);
        parameter.decide();
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

        parameter.apply(args,1);
        int size = plugin.setting.maxSize.getAndReset();
        boolean cut = plugin.setting.cut.getAndReset();
        float quality = plugin.setting.quality.getAndReset();
        try (FileInputStream input = new FileInputStream(file)){
            BufferedImage image = ImageUtils.scalingImage(ImageIO.read(input),size,cut);
            int type_i = name.lastIndexOf(".");
            if (type_i <= 0) type_i = name.length(); //如果找不到小数点就直接储存全名
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
            return parameter.tab(args);
        }
    }

    @Override
    public String getDescription() {
        return "§b自动调整图片尺寸并输出到文件:§7<图片名> " + parameter.getDescription();
    }
}
