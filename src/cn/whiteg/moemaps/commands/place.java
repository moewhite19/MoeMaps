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
            ImageMap map = plugin.createMap(name);
            if (map == null){
                sender.sendMessage("§b地图§f " + name + " §b不存在");
                return false;
            }
            if (placing != null) placing.close();
            placing = new Placing(player,map);
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

    public class Placing implements Listener {
        private final Player player;
        private final ImageMap imageMap;

        public Placing(Player player,ImageMap imageMap) {
            this.player = player;
            this.imageMap = imageMap;
        }

        @EventHandler
        public void playerQuit(PlayerQuitEvent event) {
            if (event.getPlayer().equals(player)) close();
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
        public void onInteract(final PlayerInteractEvent e) {
            if (!e.getAction().isRightClick()) return;
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

            //检查空余位置
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
            ItemFrame frame = null;
            try{
                frame = block.getWorld().spawn(block.getLocation(),GlowItemFrame.class);
            }catch (IllegalArgumentException e){
                //创建展示框失败
                var loc = block.getLocation();
                for (var itemFrame : loc.getNearbyEntitiesByType(ItemFrame.class,2D,2D,2D)) {
                    var loc1 = itemFrame.getLocation();
                    if (loc.getBlockX() == loc1.getBlockX() && loc.getBlockY() == loc1.getBlockY() && loc.getBlockZ() == loc1.getBlockZ()){
                        frame = itemFrame;
                        break;
                    }
                }
                if (frame == null) return;
            }
            frame.setFacingDirection(face,false);
            frame.setItem(item);
        }

        public void close() {
            HandlerList.unregisterAll(this);
            placing = null;
        }
    }
}