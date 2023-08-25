package cn.whiteg.moemaps.utils;

import net.minecraft.world.level.saveddata.maps.WorldMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapView;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class ImageUtils {
    static Color TRANSLUCENT = new Color(0,0,0,0); //透明颜色
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


    /**
     * 限制一张图片的最大地图数量
     *
     * @param maxSize 地图数量(一张地图128x)
     */
    public static BufferedImage scalingImage(BufferedImage image,int maxSize,boolean cut) {
        int w = image.getWidth(), h = image.getHeight(); //当前图片大小
        double bw = w / 128d, bh = h / 128d; //图片方块大小
        double ratio = w / (double) h; //比例
        //剪裁长和宽
        if (bw > maxSize){
            double s = bw - maxSize;
            bw = maxSize;
            bh -= s / ratio;
        }
        if (bh > maxSize){
            double s = bh - maxSize;
            bh = maxSize;
            bw -= (s * ratio);
        }
        //框架大小,使用五舍六入
        int fw = Math.max(1,new BigDecimal(bw).setScale(0,RoundingMode.HALF_DOWN).intValue()) * 128, fh = Math.max(1,new BigDecimal(bh).setScale(0,RoundingMode.HALF_DOWN).intValue()) * 128;//剪裁成128的整数
        return scalingImage(image,fw,fh,cut);
    }

    /**
     * 自动拉伸图片至指定尺寸
     *
     * @param wight 宽
     * @param high  高
     */

    public static BufferedImage scalingImage(BufferedImage image,int wight,int high,boolean cut) {
        try{
            int w = image.getWidth(), h = image.getHeight(); //当前图片大小
            //修正大小
            int modW, modH;

            if (w < 128 && h < 128){  //小于128单个展示框的图片居中显示
                modW = w;
                modH = h;
            } else if (cut){ //剪裁
                double ratio = w / (double) h; //比例
                modW = wight;
                modH = (int) (wight / ratio);
                if (modH < high){
                    int c = high - modH;
                    modH = high;
                    modW += c / ratio;
                }
            } else { //拉伸
                modW = wight;
                modH = high;
            }

            //偏移值
            int modX = 0, modY = 0;
            if (modW != wight){
                modX = (wight - modW) / 2;
            }
            if (modH != high){
                modY = (high - modH) / 2;
            }
            BufferedImage nImage = new BufferedImage(wight,high,image.getColorModel().hasAlpha() ? BufferedImage.TYPE_4BYTE_ABGR : BufferedImage.TYPE_3BYTE_BGR);
            var graphics = nImage.getGraphics();
            graphics.drawImage(image,modX,modY,modW,modH,TRANSLUCENT,(img,infoflags,x,y,width,height) -> true);
            return nImage;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * @param output  输出，可以是File也可以是OutputStream
     * @param quality 图片质量,范围[0-1]
     * @return 是否成功
     */
    public static boolean writeImage(BufferedImage image,Object output,float quality) {
        try{
            ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName(image.getColorModel().hasAlpha() ? "png" : "jpg").next();
            ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
            jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpgWriteParam.setCompressionQuality(quality);
            try (var outputStream = ImageIO.createImageOutputStream(output)){
                jpgWriter.setOutput(outputStream);
                IIOImage outputImage = new IIOImage(image,null,null);
                jpgWriter.write(null,outputImage,jpgWriteParam);
                jpgWriter.dispose();
            }
            return true;
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }
    }

    //写入视角的图片
    public static boolean writeMapView(MapView view,BufferedImage image) {
        if (getWorldMapField == null){
            try{
                //CraftBukkit获取nms
                getWorldMapField = view.getClass().getDeclaredField("worldMap");
                getWorldMapField.setAccessible(true);
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
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

    //重置地图
    public static void resetMap(MapView view) {
        view.setWorld(Bukkit.getWorlds().get(0));
        view.setLocked(false);
        view.setTrackingPosition(true);
    }
}
