package com.nebula.iptv;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.media3.common.C;
import androidx.media3.common.text.Cue;
import androidx.media3.common.text.CueGroup;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlayer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlayerActivity extends Activity {
    private MediaItemData item;private ExoPlayer player;private LinearLayout controls;private SeekBar seek;private TextView time,subtitle,status;private Button play,cc;private ProgressBar loading;private SharedPreferences prefs;private SurfaceView surface;private final Handler handler=new Handler();private int retries=0;private boolean captions=true;private long pendingPosition=0;
    private final Runnable ticker=new Runnable(){public void run(){if(player!=null){long pos=player.getCurrentPosition(),dur=player.getDuration();if(dur>0&&dur!=C.TIME_UNSET){seek.setMax((int)Math.min(Integer.MAX_VALUE,dur));seek.setProgress((int)Math.min(Integer.MAX_VALUE,pos));time.setText(format(pos)+" / "+format(dur));}else time.setText("AO VIVO");}handler.postDelayed(this,1000);}};

    @Override public void onCreate(Bundle b){super.onCreate(b);requestWindowFeature(Window.FEATURE_NO_TITLE);getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);hideSystemUi();item=(MediaItemData)getIntent().getSerializableExtra("item");prefs=getSharedPreferences("nebula",MODE_PRIVATE);buildUi();buildPlayer();handler.post(ticker);showControls();}
    private void buildUi(){boolean compact=getResources().getConfiguration().orientation==android.content.res.Configuration.ORIENTATION_LANDSCAPE;FrameLayout root=new FrameLayout(this);root.setBackgroundColor(Color.BLACK);surface=new SurfaceView(this);root.addView(surface,new FrameLayout.LayoutParams(-1,-1));
        subtitle=text("",compact?16:20,Color.WHITE);subtitle.setGravity(Gravity.CENTER);subtitle.setShadowLayer(5,0,2,Color.BLACK);FrameLayout.LayoutParams sp=new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);sp.setMargins(dp(30),0,dp(30),dp(compact?86:115));root.addView(subtitle,sp);
        loading=new ProgressBar(this);root.addView(loading,new FrameLayout.LayoutParams(dp(65),dp(65),Gravity.CENTER));status=text("",14,Color.WHITE);status.setGravity(Gravity.CENTER);FrameLayout.LayoutParams st=new FrameLayout.LayoutParams(-1,-2,Gravity.CENTER);st.topMargin=dp(85);root.addView(status,st);
        controls=new LinearLayout(this);controls.setOrientation(LinearLayout.VERTICAL);controls.setPadding(dp(compact?10:20),dp(compact?7:12),dp(compact?10:20),dp(compact?7:12));controls.setBackgroundResource(R.drawable.bg_panel);FrameLayout.LayoutParams controlsParams=new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM);controlsParams.setMargins(dp(6),0,dp(6),dp(6));root.addView(controls,controlsParams);LinearLayout heading=new LinearLayout(this);heading.setGravity(Gravity.CENTER_VERTICAL);TextView title=text(item.title,compact?15:18,Color.rgb(245,247,255));title.setTypeface(title.getTypeface(),1);title.setMaxLines(1);heading.addView(title,new LinearLayout.LayoutParams(0,-2,1));TextView brand=text("NEBULA IPTV",compact?9:11,Color.rgb(33,197,217));heading.addView(brand);controls.addView(heading);
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);controls.addView(row,new LinearLayout.LayoutParams(-1,dp(compact?48:58)));int buttonWidth=dp(compact?48:64),buttonHeight=dp(compact?42:50);Button back=button("‹");back.setOnClickListener(v->finish());row.addView(back,new LinearLayout.LayoutParams(buttonWidth,buttonHeight));Button rew=button("−10");rew.setOnClickListener(v->seekBy(-10000));row.addView(rew,new LinearLayout.LayoutParams(buttonWidth,buttonHeight));play=button("▶");play.setOnClickListener(v->toggle());row.addView(play,new LinearLayout.LayoutParams(buttonWidth,buttonHeight));Button fwd=button("+10");fwd.setOnClickListener(v->seekBy(10000));row.addView(fwd,new LinearLayout.LayoutParams(buttonWidth,buttonHeight));cc=button("CC");cc.setOnClickListener(v->toggleCaptions());row.addView(cc,new LinearLayout.LayoutParams(buttonWidth,buttonHeight));seek=new SeekBar(this);seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar s,int p,boolean from){if(from&&player!=null)player.seekTo(p);}public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){showControls();}});row.addView(seek,new LinearLayout.LayoutParams(0,dp(compact?40:48),1));time=text("00:00",compact?10:13,Color.WHITE);time.setGravity(Gravity.CENTER);row.addView(time,new LinearLayout.LayoutParams(dp(compact?112:145),dp(compact?40:48)));setContentView(root);play.requestFocus();}
    private void buildPlayer(){DefaultLoadControl load=new DefaultLoadControl.Builder().setBufferDurationsMs(15000,50000,1500,3000).build();player=new ExoPlayer.Builder(this).setLoadControl(load).build();player.setVideoSurfaceView(surface);player.addListener(new Player.Listener(){@Override public void onPlaybackStateChanged(int state){if(state==Player.STATE_BUFFERING){loading.setVisibility(View.VISIBLE);status.setText(retries>0?"Reconectando…":"Carregando…");}else if(state==Player.STATE_READY){loading.setVisibility(View.GONE);status.setText("");play.setText(player.getPlayWhenReady()?"Ⅱ":"▶");retries=0;showControls();}else if(state==Player.STATE_ENDED){play.setText("▶");prefs.edit().remove("resume_"+item.id).apply();}}@Override public void onPlayerError(PlaybackException error){retry(error);}@Override public void onCues(CueGroup group){StringBuilder s=new StringBuilder();for(Cue cue:group.cues)if(cue.text!=null){if(s.length()>0)s.append('\n');s.append(cue.text);}subtitle.setText(s.toString());}});pendingPosition=prefs.getLong("resume_"+item.id,0);prepare(pendingPosition);}
    private void prepare(long position){MediaItem.Builder b=new MediaItem.Builder().setUri(item.url);if(item.subtitleUrl!=null&&!item.subtitleUrl.isEmpty()){String lower=item.subtitleUrl.toLowerCase(Locale.ROOT);String mime=lower.contains(".vtt")?MimeTypes.TEXT_VTT:MimeTypes.APPLICATION_SUBRIP;MediaItem.SubtitleConfiguration sub=new MediaItem.SubtitleConfiguration.Builder(Uri.parse(item.subtitleUrl)).setMimeType(mime).setLanguage("pt").setSelectionFlags(C.SELECTION_FLAG_DEFAULT).build();List<MediaItem.SubtitleConfiguration> list=new ArrayList<>();list.add(sub);b.setSubtitleConfigurations(list);}player.setMediaItem(b.build(),Math.max(0,position));player.prepare();player.setPlayWhenReady(true);}
    private void retry(PlaybackException error){pendingPosition=player==null?0:player.getCurrentPosition();if(retries<3){retries++;long delay=1000L*(1L<<retries);status.setText("Falha de rede. Nova tentativa "+retries+"/3…");loading.setVisibility(View.VISIBLE);handler.postDelayed(()->{if(player!=null)prepare(pendingPosition);},delay);}else{loading.setVisibility(View.GONE);status.setText("Não foi possível restabelecer a reprodução.");Toast.makeText(this,"Falha no stream: "+friendly(error),Toast.LENGTH_LONG).show();}}
    private String friendly(PlaybackException e){String m=e.getMessage();return m==null?"servidor ou formato indisponível":m.replaceAll("(?i)(username|password)=([^&\\s]+)","$1=***");}
    private void toggle(){if(player==null)return;if(player.isPlaying()){player.pause();play.setText("▶");}else{player.play();play.setText("Ⅱ");}showControls();}
    private void seekBy(long d){if(player==null)return;long dur=player.getDuration();if(dur<=0||dur==C.TIME_UNSET)return;player.seekTo(Math.max(0,Math.min(dur,player.getCurrentPosition()+d)));showControls();}
    private void toggleCaptions(){captions=!captions;player.setTrackSelectionParameters(player.getTrackSelectionParameters().buildUpon().setTrackTypeDisabled(C.TRACK_TYPE_TEXT,!captions).build());cc.setText(captions?"CC":"CC×");}
    private void showControls(){controls.setVisibility(View.VISIBLE);handler.removeCallbacks(hide);handler.postDelayed(hide,3500);}
    private final Runnable hide=()->{controls.setVisibility(View.GONE);hideSystemUi();};
    private void hideSystemUi(){getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);}
    @Override public boolean dispatchTouchEvent(MotionEvent event){if(event.getAction()==MotionEvent.ACTION_DOWN)showControls();return super.dispatchTouchEvent(event);}
    @Override public void onWindowFocusChanged(boolean hasFocus){super.onWindowFocusChanged(hasFocus);if(hasFocus)hideSystemUi();}
    @Override public void onConfigurationChanged(android.content.res.Configuration configuration){super.onConfigurationChanged(configuration);buildUi();if(player!=null)player.setVideoSurfaceView(surface);hideSystemUi();showControls();}
    @Override public boolean dispatchKeyEvent(KeyEvent e){if(e.getAction()==KeyEvent.ACTION_DOWN){if(e.getKeyCode()==KeyEvent.KEYCODE_DPAD_CENTER||e.getKeyCode()==KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE){toggle();return true;}if(e.getKeyCode()==KeyEvent.KEYCODE_DPAD_LEFT){seekBy(-10000);return true;}if(e.getKeyCode()==KeyEvent.KEYCODE_DPAD_RIGHT){seekBy(10000);return true;}showControls();}return super.dispatchKeyEvent(e);}
    private void save(){if(player!=null&&player.getDuration()>0&&player.getDuration()!=C.TIME_UNSET)prefs.edit().putLong("resume_"+item.id,player.getCurrentPosition()).putLong("seen_"+item.id,System.currentTimeMillis()).apply();}
    @Override public void onPause(){save();if(player!=null)player.pause();super.onPause();}
    @Override public void onResume(){super.onResume();hideSystemUi();if(player!=null)player.play();showControls();}
    @Override public void onDestroy(){save();handler.removeCallbacksAndMessages(null);if(player!=null){player.release();player=null;}super.onDestroy();}
    private TextView text(String s,int sp,int c){TextView t=new TextView(this);t.setText(s);t.setTextSize(sp);t.setTextColor(c);return t;}
    private Button button(String s){Button b=new Button(this);b.setText(s);b.setTextColor(Color.WHITE);b.setTextSize(getResources().getConfiguration().orientation==android.content.res.Configuration.ORIENTATION_LANDSCAPE?12:15);b.setMinWidth(0);b.setMinimumWidth(0);b.setPadding(0,0,0,0);b.setBackgroundResource(R.drawable.bg_nav_item);b.setFocusable(true);return b;}
    private String format(long ms){long sec=ms/1000;return String.format(Locale.US,"%02d:%02d:%02d",sec/3600,(sec/60)%60,sec%60);}
    private int dp(int n){return(int)(n*getResources().getDisplayMetrics().density+.5f);}
}
