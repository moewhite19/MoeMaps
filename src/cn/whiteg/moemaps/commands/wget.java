package cn.whiteg.moemaps.commands;

import cn.whiteg.moemaps.MoeMaps;
import cn.whiteg.moemaps.common.HasCommandInterface;
import cn.whiteg.moemaps.utils.Downloader;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class wget extends HasCommandInterface {
    private final MoeMaps plugin;

    public wget(MoeMaps plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean executo(CommandSender sender,Command cmd,String str,String[] args) {
        if (args.length < 1){
            sender.sendMessage(getDescription());
            return false;
        }
        String url = args[0];

        if (plugin.downloader != null && !plugin.downloader.isClosed()) plugin.downloader.close();

        plugin.downloader = new Downloader(url,sender) {
            long downloaded = 0;
            File file;

            @Override
            public void readInputStream(InputStream inputStream) throws IOException {
                String name;
                if (args.length >= 2){
                    name = args[1];
                } else {
                    name = getFileName(getConn().getURL());
                }
                file = new File(plugin.imagesDir,name);
                info(" §b正在下载至文件:§f " + file);
                try{
                    if (!file.exists() || file.isDirectory()) file.delete();
                    try (var output = new FileOutputStream(file)){
                        byte[] buffer = new byte[2048];
                        int i;
                        while ((i = inputStream.read(buffer)) != -1) {
                            output.write(buffer,0,i);
                            downloaded += i;
                        }
                    }
                }catch (Exception exception){
                    exception.printStackTrace();
                }
            }

            @Override
            public void onError() {
                super.onError();
                plugin.downloader = null;
                if (file != null) file.delete();
            }

            @Override
            public void onDone() {
                super.onDone();
                plugin.downloader = null;
            }

            @Override
            public long getDownloaded() {
                return downloaded;
            }
        };
        plugin.downloader.start();
        return true;
    }

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.hasPermission("moemaps.command.wget");
    }

    @Override
    public List<String> complete(CommandSender sender,Command cmd,String label,String[] args) {
        if (args.length == 2){
            return getMatches(plugin.getFileImageList(),args);
        }
        return null;
    }

    @Override
    public String getDescription() {
        return "从网络上下载图片:§7<图片地址> [储存名称:默认从链接获取]";
    }
}
