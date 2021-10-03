package cn.whiteg.moemaps.commands;

import cn.whiteg.moemaps.ImageMap;
import cn.whiteg.moemaps.MoeMaps;
import cn.whiteg.moemaps.common.HasCommandInterface;
import cn.whiteg.moemaps.listener.Placing;
import cn.whiteg.moemaps.parameter.EntryBoolean;
import cn.whiteg.moemaps.parameter.Parameter;
import cn.whiteg.moemaps.utils.CommonUtils;
import cn.whiteg.moemaps.utils.Downloader;
import cn.whiteg.moemaps.utils.ImageUtils;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class uget extends HasCommandInterface {
    private final MoeMaps plugin;
    private final Parameter parameter;
    private final EntryBoolean plac;

    public uget(MoeMaps plugin) {
        this.plugin = plugin;
        parameter = new Parameter();
        parameter.add(plugin.setting.maxSize);
        parameter.add(plugin.setting.cut);
        plac = parameter.add(new EntryBoolean("plac","放置模式",true));
        parameter.decide();
    }

    @Override
    public boolean executo(CommandSender sender,Command cmd,String str,String[] args) {
        if (!(sender instanceof Player player)) return false;
        if (args.length < 1){
            sender.sendMessage(getDescription());
            sender.sendMessage("§f max§b属性代表地图的尺寸,越大越清晰，但也会更贵");
            sender.sendMessage("§f cut§b为剪裁模式,当图片尺寸较小时建议打开，不然拉伸会导致图片变形严重");
            sender.sendMessage("§f place§b的参数如果改成0的话，请一定要在获取地图前放下背包内的杂物。");
            return false;
        }
        String url = args[0];
        if (plugin.downloader != null && !plugin.downloader.isClosed()){
            sender.sendMessage(" §b当前有个下载计划未完成，再等等吧！");
        }


        try{
            parameter.apply(args,1);
        }catch (IllegalArgumentException e){
            sender.sendMessage(e.getMessage());
        }
        boolean place = this.plac.getAndReset();
        int maxSize = plugin.setting.maxSize.getAndReset();
        boolean cut = plugin.setting.cut.getAndReset();

        EconomyResponse response = plugin.getEconomy().withdrawPlayer(player,maxSize * plugin.setting.ugetPrice);
        if (response.type != EconomyResponse.ResponseType.SUCCESS){
            sender.sendMessage(response.errorMessage);
            return false;
        } else {
            sender.sendMessage(" §b消费" + response.amount + "下载地图");
        }

        plugin.downloader = new Downloader(url,sender) {
            BufferedImage image;

            @Override
            public void readInputStream(InputStream inputStream) throws IOException {
                String protocol = getConn().getURL().getProtocol();
                if (!protocol.equals("http") && !protocol.equals("https")){
                    info("不支持的协议" + protocol);
                    onError();
                    return;
                }
                var size = plugin.downloader.getSize();
                if (size <= 0 || size > 1024 * 1024 * 8){ //8MB限制
                    info("大小" + CommonUtils.tanSpace(size) + "超出范围");
                    onError();
                    return;
                }
                image = ImageUtils.scalingImage(ImageIO.read(inputStream),maxSize,cut);
            }

            @Override
            public void onError() {
                super.onError();
                plugin.downloader = null;
                Bukkit.getScheduler().runTask(plugin,() -> {
                    double back = response.amount * 0.8;
                    back = plugin.getEconomy().depositPlayer(player,back).amount;
                    info(" §b下载失败，已退回§f" + back);
                });
            }

            @Override
            public void onDone() {
                super.onDone();
                plugin.downloader = null;
                var map = ImageMap.create(image);
                Bukkit.getScheduler().runTask(plugin,() -> {
                    if (place){
                        Placing placing = new Placing(plugin,player,map);
                        placing.setFailClose(false);
                        placing.setFramType(ItemFrame.class);
                        player.sendMessage("§b 右键墙面的左上角来放置地图画");
                        Bukkit.getPluginManager().registerEvents(placing,plugin);
                    } else {
                        var items = map.createItems();
                        var callBack = player.getInventory().addItem(items);
                        if (!callBack.isEmpty()){
                            var loc = player.getLocation();
                            for (Map.Entry<Integer, ItemStack> entry : callBack.entrySet()) {
                                ItemStack item = entry.getValue();
                                player.getWorld().dropItem(loc,item);
                            }
                        }
                    }
                });
            }
        };
        plugin.downloader.start();
        return true;
    }

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return plugin.setting.ugetPrice > 0 && plugin.getEconomy() != null;
    }

    @Override
    public List<String> complete(CommandSender sender,Command cmd,String label,String[] args) {
        if (args.length == 1){
            return null;
        } else {
            return parameter.tab(args);
        }
    }

    @Override
    public String getDescription() {
        return "§b从网络上下载图片:§7<图片地址> " + parameter.getDescription();
    }
}
