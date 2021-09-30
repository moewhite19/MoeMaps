package cn.whiteg.moemaps.commands;

import cn.whiteg.moemaps.ImageMap;
import cn.whiteg.moemaps.MoeMaps;
import cn.whiteg.moemaps.common.HasCommandInterface;
import cn.whiteg.moemaps.utils.ImageUtils;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.GlowItemFrame;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class placimage extends HasCommandInterface {
    private final MoeMaps plugin;

    public placimage(MoeMaps plugin) {
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
            sender.sendMessage("这个指令只能管理员使用");
            return false;
        }

        try{
            ImageMap map = plugin.getMapFormName(name);
            if (map == null){
                sender.sendMessage("地图 " + name + " 不存在");
                return false;
            }

            Bukkit.getPluginManager().registerEvents(new placing(player,map),plugin);
            player.sendMessage("右键墙壁放置");
        }catch (Exception exception){
            sender.sendMessage(exception.getMessage());
            exception.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean canUseCommand(CommandSender sender) {
        return sender.hasPermission("moemaps.command.placimage");
    }

    @Override
    public List<String> complete(CommandSender sender,Command cmd,String label,String[] args) {
        if (args.length == 1){
            return getMatches(plugin.getLoadedImageList(),args);
        } else if (args.length == 2){
            return getPlayersList(args);
        }
        return null;
    }

    @Override
    public String getDescription() {
        return "放置图片地图: <图片名称>";
    }

    public class placing implements Listener {
        private final Player player;
        private final ImageMap imageMap;

        public placing(Player player,ImageMap imageMap) {
            this.player = player;
            this.imageMap = imageMap;
        }

        @EventHandler
        public void playerQuit(PlayerQuitEvent event) {
            if (event.getPlayer().equals(player)) close();
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
        public void onInteract(final PlayerInteractEvent e) {
            var block = e.getClickedBlock();
            if (block == null) return;
            var face = e.getBlockFace();
            close();
            int xMod = 0;
            int zMod = 0;
            switch (face) {
                case EAST -> {
                    zMod = -1;
                }
                case WEST -> {
                    zMod = 1;
                }
                case SOUTH -> {
                    xMod = 1;
                }
                case NORTH -> {
                    xMod = -1;
                }
                default -> {
                    player.sendMessage("无效方块方向: " + face);
                    return;
                }
            }
            final Block relative = block.getRelative(face);
            final int width = imageMap.getWight();
            final int height = imageMap.getHigh();
            for (int x = 0; x < width; ++x) {
                for (int y = 0; y < height; ++y) {
                    if (!block.getRelative(x * xMod,-y,x * zMod).getType().isSolid()){
                        player.sendMessage("没有多余的墙面放置展示框");
                        return;
                    }
                    if (block.getRelative(x * xMod - zMod,-y,x * zMod + xMod).getType().isSolid()){
                        player.sendMessage("没有多余的空间放置展示框");
                        return;
                    }
                }
            }
            for (int x = 0; x < width; ++x) {
                for (int y = 0; y < height; ++y) {
                    this.setItemFrame(relative.getRelative(x * xMod,-y,x * zMod),face,ImageUtils.createMapItem(imageMap.getMapView(x,y)));
                }
            }
        }

        private void setItemFrame(final Block block,final BlockFace face,ItemStack item) {
            ItemFrame i;
            i = block.getWorld().spawn(block.getLocation(),GlowItemFrame.class);
            i.setFacingDirection(face,false);
            i.setItem(item);
        }

        public void close() {
            HandlerList.unregisterAll(this);
        }
    }
}
