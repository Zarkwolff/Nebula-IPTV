package com.nebula.iptv;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.content.res.Configuration;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class NebulaCatalogAdapter extends BaseAdapter {
    public interface Actions {void open(MediaItemData item);void toggleFavorite(MediaItemData item);boolean isFavorite(MediaItemData item);boolean isRestricted(MediaItemData item);}
    private final Context context;private final Actions actions;private final boolean posterMode;private final List<MediaItemData> items=new ArrayList<>();private final int white=Color.rgb(245,247,255),muted=Color.rgb(146,156,181),purple=Color.rgb(118,84,246),cyan=Color.rgb(33,197,217);
    public NebulaCatalogAdapter(Context c,List<MediaItemData> data,boolean posterMode,Actions actions){context=c;this.posterMode=posterMode;this.actions=actions;items.addAll(data);}
    public void replace(List<MediaItemData> data){items.clear();items.addAll(data);notifyDataSetChanged();}
    @Override public int getCount(){return items.size();}@Override public MediaItemData getItem(int p){return items.get(p);}@Override public long getItemId(int p){return p;}
    @Override public View getView(int position,View convert,ViewGroup parent){Holder h;if(convert==null){h=create();convert=h.root;convert.setTag(h);}else h=(Holder)convert.getTag();MediaItemData item=getItem(position);boolean locked=actions.isRestricted(item);h.root.setContentDescription(locked?"Conteúdo protegido":item.title);h.title.setText(locked?"Conteúdo protegido":item.title);h.meta.setText(locked?"PIN necessário":meta(item));h.icon.setText(locked?"🔒":item.kind.equals("live")?"TV":item.kind.equals("movie")?"FILME":"SÉRIE");h.icon.setTextColor(item.kind.equals("live")?cyan:purple);h.favorite.setText(actions.isFavorite(item)?"♥":"♡");h.favorite.setContentDescription(actions.isFavorite(item)?"Remover dos favoritos":"Adicionar aos favoritos");h.favorite.setOnClickListener(v->{actions.toggleFavorite(item);notifyDataSetChanged();});h.root.setOnClickListener(v->actions.open(item));if(locked){h.poster.setImageResource(R.drawable.nebula_logo);h.poster.setTag(null);}else ImageLoader.load(item.logo,h.poster,R.drawable.nebula_logo);return convert;}
    private Holder create(){boolean compact=!isTv()&&context.getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE;Holder h=new Holder();h.root=new LinearLayout(context);h.root.setOrientation(posterMode?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);h.root.setGravity(Gravity.CENTER_VERTICAL);h.root.setFocusable(true);h.root.setClickable(true);h.root.setBackgroundResource(R.drawable.bg_card);h.root.setPadding(dp(compact?6:8),dp(compact?6:8),dp(compact?6:8),dp(compact?6:8));h.poster=new ImageView(context);h.poster.setScaleType(ImageView.ScaleType.CENTER_CROP);h.poster.setBackgroundResource(R.drawable.bg_poster);int imageW=posterMode?-1:dp(compact?48:58),imageH=posterMode?dp(compact?108:170):dp(compact?48:58);h.root.addView(h.poster,new LinearLayout.LayoutParams(imageW,imageH));LinearLayout text=new LinearLayout(context);text.setOrientation(LinearLayout.VERTICAL);text.setPadding(posterMode?0:dp(compact?7:10),posterMode?dp(compact?5:8):0,0,0);h.root.addView(text,new LinearLayout.LayoutParams(posterMode?-1:0,-2,posterMode?0:1));h.icon=label("",compact?8:9,purple);h.icon.setTypeface(h.icon.getTypeface(),Typeface.BOLD);text.addView(h.icon);h.title=label("",compact?12:15,white);h.title.setTypeface(h.title.getTypeface(),Typeface.BOLD);h.title.setMaxLines(2);text.addView(h.title);h.meta=label("",compact?9:11,muted);h.meta.setMaxLines(1);text.addView(h.meta);h.favorite=new Button(context);h.favorite.setTextColor(Color.rgb(177,123,255));h.favorite.setTextSize(compact?15:19);h.favorite.setBackgroundResource(R.drawable.bg_nav_item);h.favorite.setFocusable(false);h.favorite.setClickable(true);LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(posterMode?-1:dp(compact?40:48),posterMode?dp(compact?34:42):dp(compact?46:54));h.root.addView(h.favorite,fp);h.root.setMinimumHeight(posterMode?dp(compact?185:265):dp(compact?62:76));return h;}
    private String meta(MediaItemData m){String s=m.subtitle==null||m.subtitle.isEmpty()?m.group:m.subtitle;return s==null?"":s;}
    private TextView label(String s,int sp,int c){TextView t=new TextView(context);t.setText(s);t.setTextSize(sp);t.setTextColor(c);t.setLineSpacing(0,1.08f);return t;}
    private int dp(int n){return(int)(n*context.getResources().getDisplayMetrics().density+.5f);}
    private boolean isTv(){return(context.getResources().getConfiguration().uiMode&Configuration.UI_MODE_TYPE_MASK)==Configuration.UI_MODE_TYPE_TELEVISION;}
    private static class Holder{LinearLayout root;ImageView poster;TextView icon,title,meta;Button favorite;}
}
