package cn.whiteg.moemaps.listener;

import cn.whiteg.moemaps.ImageMap;
import cn.whiteg.moemaps.MoeMaps;
import cn.whiteg.moemaps.utils.ImageUtils;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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

public class Placing implements Listener {
    private final MoeMaps plugin;
    private final Player player;
    private final ImageMap imageMap;
    boolean failClose = true; //失败后关闭
    private Class<? extends ItemFrame> framType = GlowItemFrame.class; //展示框类型

    public Placing(MoeMaps plugin,Player player,ImageMap imageMap) {
        this.plugin = plugin;
        this.player = player;
        this.imageMap = imageMap;
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent event) {
        if (event.getPlayer().equals(player)) close();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onInteract(final PlayerInteractEvent e) {
        if (!player.equals(e.getPlayer()) || !e.getAction().isRightClick()) return;
        var block = e.getClickedBlock();
        if (block == null) return;
        var face = e.getBlockFace();
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
                if (failClose) close();
                return;
            }
        }
        final Block relative = block.getRelative(face);
        final int width = imageMap.getWight();
        final int height = imageMap.getHigh();

        //检查空余位置
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                Block b;
                if (!(b = block.getRelative(x * xMod,-y,x * zMod)).getType().isSolid() ||
                        !(plugin.hasResidence() && Residence.getInstance().getPermsByLocForPlayer(b.getLocation(),player).playerHasHints(player,Flags.build,true))){
                    player.sendMessage("没有多余的墙面放置展示框");
                    if (failClose) close();
                    return;
                }
                if (block.getRelative(x * xMod - zMod,-y,x * zMod + xMod).getType().isSolid()){
                    player.sendMessage("没有多余的空间放置展示框");
                    if (failClose) close();
                    return;
                }
            }
        }
        close();
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                this.setItemFrame(relative.getRelative(x * xMod,-y,x * zMod),face,ImageUtils.createMapItem(imageMap.getMapView(x,y)));
            }
        }
    }

    private void setItemFrame(final Block block,final BlockFace face,ItemStack item) {
        ItemFrame frame = null;
        try{
            frame = block.getWorld().spawn(block.getLocation(),framType);
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

    public Placing setFramType(Class<? extends ItemFrame> framType) {
        this.framType = framType;
        return this;
    }

    public Placing setFailClose(boolean b) {
        failClose = b;
        return this;
    }

    public void close() {
        HandlerList.unregisterAll(this);
    }
}
