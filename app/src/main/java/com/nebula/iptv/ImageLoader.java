package com.nebula.iptv;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ImageLoader {
    private static final LruCache<String,Bitmap> CACHE=new LruCache<String,Bitmap>(Math.min(8192,(int)(Runtime.getRuntime().maxMemory()/1024/10))){@Override protected int sizeOf(String k,Bitmap b){return b.getByteCount()/1024;}};
    private static final ExecutorService POOL=Executors.newFixedThreadPool(4);private static final Handler MAIN=new Handler(Looper.getMainLooper());
    private ImageLoader(){}
    public static void load(String url,ImageView view,int placeholder){view.setTag(url);view.setImageResource(placeholder);if(url==null||(!url.startsWith("http://")&&!url.startsWith("https://")))return;Bitmap cached=CACHE.get(url);if(cached!=null){view.setImageBitmap(cached);return;}POOL.execute(()->{Bitmap bitmap=download(url);if(bitmap!=null){CACHE.put(url,bitmap);MAIN.post(()->{if(url.equals(view.getTag()))view.setImageBitmap(bitmap);});}});}
    private static Bitmap download(String value){HttpURLConnection c=null;try{c=(HttpURLConnection)new URL(value).openConnection();c.setConnectTimeout(7000);c.setReadTimeout(10000);c.setInstanceFollowRedirects(true);c.setRequestProperty("User-Agent","NebulaIPTV/1.2");if(c.getResponseCode()<200||c.getResponseCode()>=400)return null;InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buf=new byte[8192];int n,total=0;while((n=in.read(buf))>0&&total<5_000_000){out.write(buf,0,n);total+=n;}in.close();byte[] data=out.toByteArray();BitmapFactory.Options bounds=new BitmapFactory.Options();bounds.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(data,0,data.length,bounds);int sample=1;while(bounds.outWidth/sample>700||bounds.outHeight/sample>1000)sample*=2;BitmapFactory.Options opts=new BitmapFactory.Options();opts.inSampleSize=sample;opts.inPreferredConfig=Bitmap.Config.RGB_565;return BitmapFactory.decodeByteArray(data,0,data.length,opts);}catch(Exception ignored){return null;}finally{if(c!=null)c.disconnect();}}
}
