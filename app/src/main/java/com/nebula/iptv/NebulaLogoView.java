package com.nebula.iptv;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.View;

/** Orbital play mark and wordmark reconstructed as scalable vector drawing. */
public final class NebulaLogoView extends View {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);private boolean wordmark=true;
    public NebulaLogoView(Context context){super(context);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
    public NebulaLogoView compact(){wordmark=false;invalidate();return this;}
    @Override protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight();float markH=wordmark?h*.62f:h;float cx=w*.5f,cy=markH*.43f,r=Math.min(w,markH)*.23f;
        p.setShader(new RadialGradient(cx-r*.25f,cy-r*.30f,r*1.15f,new int[]{0xFF122B77,0xFF060B31},null,Shader.TileMode.CLAMP));p.setStyle(Paint.Style.FILL);p.setShadowLayer(r*.22f,0,0,0xBB21C5D9);c.drawCircle(cx,cy,r,p);p.clearShadowLayer();p.setShader(null);
        p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeWidth(Math.max(3,r*.15f));RectF orbit=new RectF(cx-r*1.65f,cy-r*.60f,cx+r*1.65f,cy+r*.60f);p.setShader(new LinearGradient(orbit.left,orbit.top,orbit.right,orbit.bottom,new int[]{0xFFB52CFF,0xFF7654F6,0xFF00EAFF},null,Shader.TileMode.CLAMP));p.setShadowLayer(r*.16f,0,0,0xCC7654F6);c.save();c.rotate(-17,cx,cy);c.drawOval(orbit,p);c.rotate(34,cx,cy);c.drawOval(orbit,p);c.restore();p.clearShadowLayer();p.setShader(null);
        Path play=new Path();play.moveTo(cx-r*.23f,cy-r*.43f);play.lineTo(cx+r*.48f,cy);play.lineTo(cx-r*.23f,cy+r*.43f);play.close();p.setStyle(Paint.Style.FILL);p.setColor(Color.WHITE);p.setShadowLayer(8,0,0,Color.WHITE);c.drawPath(play,p);p.clearShadowLayer();
        if(wordmark){p.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));p.setTextAlign(Paint.Align.CENTER);p.setColor(0xFFF5F7FF);p.setTextSize(h*.19f);p.setLetterSpacing(.10f);c.drawText("NEBULA",cx,h*.80f,p);p.setColor(0xFF21C5D9);p.setTextSize(h*.105f);p.setLetterSpacing(.18f);c.drawText("IPTV",cx,h*.94f,p);p.setStrokeWidth(2);p.setColor(0xFF7654F6);c.drawLine(cx-w*.25f,h*.90f,cx-w*.10f,h*.90f,p);c.drawLine(cx+w*.10f,h*.90f,cx+w*.25f,h*.90f,p);}
    }
}
