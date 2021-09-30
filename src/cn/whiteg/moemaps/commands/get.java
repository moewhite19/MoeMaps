package cn.whiteg.moemaps.commands;

import cn.whiteg.moemaps.ImageMap;
import cn.whiteg.moemaps.MoeMaps;
import cn.whiteg.moemaps.common.HasCommandInterface;
import cn.whiteg.moemaps.utils.ImageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;

import java.util.List;
import java.util.Map;

public class get extends HasCommandInterface {
    private final MoeMaps plugin;

    public get(MoeMaps plugin) {
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
        } else if (sender instanceof Player){
            player = (Player) sender;
        }
        if (player == null){
            sender.sendMessage("找不到玩家");
            return false;
        }
        try{
            ImageMap map = plugin.createMap(name);
            if (map == null){
                sender.sendMessage("§b地图§f " + name + " §b不存在");
                return false;
            }

            if (args.length >= 4){
                try{
                    int x = Integer.parseInt(args[2]), y = Integer.parseInt(args[3]);
                    MapView view = map.getMapView(x,y);
                    player.getInventory().addItem(ImageUtils.createMapItem(view));
                }catch (NumberFormatException e){
                    sender.sendMessage("参数有误");
                    return false;
                }
            } else {
                var callBack = player.getInventory().addItem(map.createItems());
                //如果背包满了丢地上
                if (!callBack.isEmpty()){
                    var loc = player.getLocation();
                    var world = loc.getWorld();
                    for (Map.Entry<Integer, ItemStack> entry : callBack.entrySet()) {
                        world.dropItem(loc,entry.getValue());
                    }
                }
            }


        }catch (Exception exception){
            sender.sendMessage(exception.getMessage());
            exception.printStackTrace();
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
        return "获取图片地图: <图片名称> [给与玩家] [图片x] [图片y]";
    }
}
