package com.nebula.iptv;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.widget.FrameLayout;

/** Code-native star field used on every screen, matching the supplied Nebula art direction. */
public final class NebulaBackdropLayout extends FrameLayout {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);private boolean horizon;
    public NebulaBackdropLayout(Context context){super(context);setWillNotDraw(false);setFitsSystemWindows(true);setOnApplyWindowInsetsListener((view,insets)->{view.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets;});}
    public NebulaBackdropLayout horizon(boolean value){horizon=value;invalidate();return this;}
    @Override protected void onDraw(Canvas c){super.onDraw(c);int w=getWidth(),h=getHeight();if(w<=0||h<=0)return;
        paint.setShader(new LinearGradient(0,0,w,h,new int[]{Color.rgb(2,5,22),Color.rgb(7,8,35),Color.rgb(3,15,39)},null,Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,paint);
        glow(c,w*.18f,h*.38f,Math.max(w,h)*.38f,0x994E0AAE);glow(c,w*.78f,h*.20f,Math.max(w,h)*.32f,0x773200A4);glow(c,w*.62f,h*.72f,Math.max(w,h)*.30f,0x5521C5D9);
        paint.setShader(null);for(int i=0;i<115;i++){long n=(i*1103515245L+12345L)&0x7fffffffL;float x=(n%1000)/1000f*w;float y=((n/1000)%1000)/1000f*h;float radius=i%17==0?2.2f:i%5==0?1.35f:.75f;paint.setColor(i%9==0?0xCC21C5D9:i%7==0?0xCCB17BFF:0xBDF5F7FF);c.drawCircle(x,y,radius,paint);}
        if(horizon){RectF planet=new RectF(-w*.08f,h*.72f,w*1.08f,h*1.33f);paint.setStyle(Paint.Style.FILL);paint.setShader(new LinearGradient(0,h*.74f,0,h,new int[]{0xFF123F79,0xFF061024,0xFF020511},null,Shader.TileMode.CLAMP));c.drawOval(planet,paint);paint.setShader(null);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(Math.max(3,w*.004f));paint.setColor(0xFF21C5D9);c.drawArc(planet,198,144,false,paint);paint.setStrokeWidth(Math.max(7,w*.009f));paint.setColor(0xAA7654F6);c.drawArc(planet,202,136,false,paint);paint.setStyle(Paint.Style.FILL);}
    }
    private void glow(Canvas c,float x,float y,float radius,int color){paint.setShader(new RadialGradient(x,y,radius,new int[]{color,color&0x00FFFFFF},null,Shader.TileMode.CLAMP));c.drawCircle(x,y,radius,paint);paint.setShader(null);}
}
