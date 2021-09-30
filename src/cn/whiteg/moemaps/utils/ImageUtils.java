package cn.whiteg.moemaps.utils;

import net.minecraft.world.level.saveddata.maps.WorldMap;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapView;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.lang.reflect.Field;

public class ImageUtils {
    static Color TRANSLUCENT = new Color(0,0,0,0); //透明颜色
    static int STEP = 128; //图片基数
    static Field getWorldMapField = null;
    static Field getBytesField;

    static {
        //nms获取图片字节组
        for (Field field : WorldMap.class.getDeclaredFields()) {
            if (field.getAnnotatedType().getType().getTypeName().equals(byte[].class.getName())){
                getBytesField = field;
                getBytesField.setAccessible(true);
                break;
            }
        }
//        Objects.requireNonNull(getBytesField);
    }

    //自动调整图片大小
    public static BufferedImage automaticScaling(InputStream inputStream,int maxSize) {
        try{
            BufferedImage inImage = ImageIO.read(inputStream);
            int type = inImage.getType() == BufferedImage.TYPE_3BYTE_BGR ? BufferedImage.TYPE_3BYTE_BGR : BufferedImage.TYPE_4BYTE_ABGR;
            int w = inImage.getWidth(), h = inImage.getHeight();
//            float ratio = (float) Math.max(w,h) / (float) Math.min(w,h);
//            if (ratio > 5) return null;
            float ratioHW = (float) h / (float) w;

            //剪裁长和宽
            if (w > maxSize){
                int s = w - maxSize;
                w = maxSize;
                h -= s * ratioHW;
            }
            if (h > maxSize){
                int s = h - maxSize;
                h = maxSize;
                w -= (s / ratioHW);
            }

            int yw = w % STEP, yh = h % STEP;

            int fw = w - yw, fh = h - yh;
            BufferedImage nImage = new BufferedImage(fw,fh,type);
            var graphics = nImage.getGraphics();
            graphics.drawImage(inImage,0,0,fw,fh,TRANSLUCENT,(img,infoflags,x,y,width,height) -> true);
            return nImage;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    //写入视角的图片
    public static boolean writeMapView(MapView view,BufferedImage image) {
        try{
            if (getWorldMapField == null){
                //CraftBukkit获取nms
                getWorldMapField = view.getClass().getDeclaredField("worldMap");
                getWorldMapField.setAccessible(true);
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        if (view != null){
            if (image.getHeight() != 128 || image.getWidth() != 128) return false;
            view.setLocked(true);
            view.setTrackingPosition(false);
            view.setUnlimitedTracking(false);
            try{
                WorldMap worldMap = (WorldMap) getWorldMapField.get(view);
                byte[] fimage = MapPalette.imageToBytes(image);
                byte[] bytes = worldMap.g;
                System.arraycopy(fimage,0,bytes,0,fimage.length);
                return true;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }


    //设置地图物品视角
    public static boolean setMapView(ItemStack item,MapView view) {
        if (item.getItemMeta() instanceof MapMeta mapMeta){
            mapMeta.setMapView(view);
            item.setItemMeta(mapMeta);
            return true;
        }
        return false;
    }

    //设置地图物品图片
    public static boolean setMapView(ItemStack item,BufferedImage image) {
        if (item.getItemMeta() instanceof MapMeta mapMeta){
            var mapView = mapMeta.getMapView();
            if (mapView != null) return writeMapView(mapView,image);
        }
        return false;
    }


    //创建指定视角的地图物品
    public static ItemStack createMapItem(MapView view) {
        ItemStack item = new ItemStack(Material.FILLED_MAP);
        setMapView(item,view);
        return item;
    }
}
