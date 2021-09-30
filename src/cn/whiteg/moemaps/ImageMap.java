package cn.whiteg.moemaps;

import cn.whiteg.moemaps.utils.ImageUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;

import java.awt.image.BufferedImage;
import java.lang.ref.WeakReference;
import java.util.List;

public class ImageMap {
    final int wight;
    final int high;
    final int[] maps;
    WeakReference<MapView[]> mapView = new WeakReference<>(null); //缓存地图视角


    public ImageMap(int wight,int high,int[] maps) {
        this.wight = wight;
        this.high = high;
        this.maps = maps;
        checkSize();
    }

    public ImageMap(int wight,int high,List<Integer> maps) {
        this.wight = wight;
        this.high = high;
        this.maps = new int[maps.size()];
        checkSize();
        for (int i = 0; i < this.maps.length; i++) {
            this.maps[i] = maps.get(i);
        }
    }

    //反序列化
    public static ImageMap deserialize(ConfigurationSection cs) {
        int w = cs.getInt("wight");
        int h = cs.getInt("high");
        var ids = cs.getIntegerList("maps");
        return new ImageMap(w,h,ids);
    }

    //从一张图片创建图片地图
    public static ImageMap create(BufferedImage image) {
//        int type = image.getType() == BufferedImage.TYPE_4BYTE_ABGR ? BufferedImage.TYPE_4BYTE_ABGR : BufferedImage.TYPE_3BYTE_BGR;
        int w = (int) Math.ceil(((float) image.getWidth()) / 128f);
        int h = (int) Math.ceil(((float) image.getHeight()) / 128f);
        MapView[] views = new MapView[w * h];
        int[] ids = new int[views.length];
        int i = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                BufferedImage bufferedImage = new BufferedImage(128,128,BufferedImage.TYPE_4BYTE_ABGR);
                var graphics = bufferedImage.getGraphics();
                graphics.drawImage(image,0,0,128,128,x * 128,y * 128,x * 128 + 128,y * 128 + 128,(img,infoflags,x1,y1,width,height) -> true);
                var view = Bukkit.createMap(Bukkit.getWorlds().get(0));
                if (!ImageUtils.writeMapView(view,bufferedImage)){
                    throw new IllegalArgumentException("setMapView失败");
                }
                ids[i] = view.getId();
                views[i++] = view;
            }
        }
        ImageMap map = new ImageMap(w,h,ids);
        map.mapView = new WeakReference<>(views);
        return map;
    }

    //重新写入地图
    public void rewrite(BufferedImage image) {
        int w = (int) Math.ceil(((float) image.getWidth()) / 128f);
        int h = (int) Math.ceil(((float) image.getHeight()) / 128f);
        if (maps.length != w * h) throw new IllegalArgumentException("图片尺寸有变，无法重写");
        var views = getMapViews();
        int i = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                BufferedImage bufferedImage = new BufferedImage(128,128,BufferedImage.TYPE_4BYTE_ABGR);
                var graphics = bufferedImage.getGraphics();
                graphics.drawImage(image,0,0,128,128,x * 128,y * 128,x * 128 + 128,y * 128 + 128,(img,infoflags,x1,y1,width,height) -> true);
                var view = views[i++];
                if (!ImageUtils.writeMapView(view,bufferedImage)){
                    throw new IllegalArgumentException("writeMapView失败");
                }
            }
        }
    }

    public int getWight() {
        return wight;
    }

    public int getHigh() {
        return high;
    }

    //将坐标转换为索引
    public int getIndex(int x,int y) {
        //三种方式
//        return y * wight + x; //直接返回
        return Math.max(0,Math.min(high - 1,y)) * wight + Math.max(0,Math.min(wight - 1,x)); //限制x和y轴范围
//        return Math.max(0,Math.min(maps.length - 1,y * wight + x)); //限制返回值大小
    }

    //获取指定坐标地图Id
    public int getMapId(int x,int y) {
        return maps[getIndex(x,y)];
    }

    //获取指定坐标地图块
    public MapView getMapView(int x,int y) {
        return getMapViews()[getIndex(x,y)];
    }

    //获取地图视角
    public MapView[] getMapViews() {
        var views = mapView.get();
        if (views == null){
            views = new MapView[maps.length];
            for (int i = 0; i < views.length; i++) {
                views[i] = Bukkit.getMap(maps[i]);
            }
            mapView = new WeakReference<>(views);
        }
        return views;
    }

    //创建整个图片的物品数组
    public ItemStack[] createItems() {
        MapView[] views = getMapViews();
        ItemStack[] items = new ItemStack[views.length];
        for (int i = 0; i < views.length; i++) {
            items[i] = ImageUtils.createMapItem(views[i]);
        }
        return items;
    }

    public void serialize(ConfigurationSection cs) {
        cs.set("wight",wight);
        cs.set("high",high);
        cs.set("maps",maps);
    }

    void checkSize() {
        if (maps.length != wight * high){
            throw new IllegalArgumentException("无效地图大小" + wight + "x" + high + ":" + maps.length);
        }
    }


}
