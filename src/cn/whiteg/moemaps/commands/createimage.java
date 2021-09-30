package cn.whiteg.moemaps.commands;

import cn.whiteg.moemaps.ImageMap;
import cn.whiteg.moemaps.MoeMaps;
import cn.whiteg.moemaps.common.HasCommandInterface;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class createimage extends HasCommandInterface {
    private final MoeMaps plugin;

    public createimage(MoeMaps plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean executo(CommandSender sender,Command cmd,String str,String[] args) {
        if (args.length < 1){
            sender.sendMessage(getDescription());
            return false;
        }
        String name = args[0];
        Player player = null;
        if (args.length >= 2){
            player = Bukkit.getPlayer(args[1]);
        }
//        else if (sender instanceof Player){
//            player = (Player) sender;
//        }
//        else {
//            sender.sendMessage("找不到玩家");
//        }
        try{
            ImageMap map = plugin.createMap(name);
            if (player != null){
                var callBack = player.getInventory().addItem(map.createItems());
                //如果背包满了丢地上
                if (!callBack.isEmpty()){
                    var loc = player.getLocation();
                    var world = loc.getWorld();
                    for (Map.Entry<Integer, ItemStack> entry : callBack.entrySet()) {
                        world.dropItem(loc,entry.getValue());
                    }
                }
                sender.sendMessage((sender == player ? "§b已获取地图§f" + name : "§b已将地图§f + " + name + " §b发送给§f " + player.getDisplayName()));
            } else {
                sender.sendMessage("§b已创建地图§f " + name);
            }
        }catch (IllegalArgumentException e){
            sender.sendMessage(e.getMessage());
        }catch (Exception exception){
            sender.sendMessage(exception.getMessage());
            exception.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.hasPermission("moemaps.command.createiamge");
    }

    @Override
    public List<String> complete(CommandSender sender,Command cmd,String label,String[] args) {
        return getMatches(plugin.getAllImageList(),args);
    }

    @Override
    public String getDescription() {
        return "创建图片地图: <图片名称> [给与玩家]";
    }
}
