package cn.whiteg.moemaps.commands;

import cn.whiteg.moemaps.ImageMap;
import cn.whiteg.moemaps.MoeMaps;
import cn.whiteg.moemaps.common.HasCommandInterface;
import cn.whiteg.moemaps.listener.Placing;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class place extends HasCommandInterface {
    private final MoeMaps plugin;
    Placing placing = null;

    public place(MoeMaps plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean executo(CommandSender sender,Command cmd,String str,String[] args) {
        if (args.length < 1){
            sender.sendMessage(getDescription());
            return false;
        }
        String name = args[0];
        Player player;
        if (sender instanceof Player){
            player = (Player) sender;
        } else {
            sender.sendMessage("§c这个指令只能玩家使用");
            return false;
        }

        try{
            ImageMap map = plugin.getMapFormName(name);
            if (map == null){
                sender.sendMessage("§b地图§f " + name + " §b不存在");
                return false;
            }
            if (placing != null) placing.close();
            placing = new Placing(plugin,player,map);
            Bukkit.getPluginManager().registerEvents(placing,plugin);
            player.sendMessage(" §b右键墙壁放置§f" + name + " §a" + map.getWight() + "x" + map.getHigh());
        }catch (Exception exception){
            sender.sendMessage(exception.getMessage());
            exception.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.hasPermission("moemaps.command.place");
    }

    @Override
    public List<String> complete(CommandSender sender,Command cmd,String label,String[] args) {
        if (args.length == 1){
            return getMatches(plugin.getMapImageList(),args);
        } else if (args.length == 2){
            return getPlayersList(args);
        }
        return null;
    }

    @Override
    public String getDescription() {
        return "放置图片地图:§7<图片名称>";
    }

}
