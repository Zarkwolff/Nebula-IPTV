package com.nebula.iptv;

import android.net.Uri;
import android.util.Xml;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.StringReader;

public class IptvRepository {
    public static class Library {
        public final List<MediaItemData> live = new ArrayList<>(), movies = new ArrayList<>(), series = new ArrayList<>();
        public final List<MediaItemData> all = new ArrayList<>();
        public String sourceLabel = "";
        public void finish() { all.clear(); all.addAll(live); all.addAll(movies); all.addAll(series); }
    }

    private static String get(String rawUrl) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(rawUrl).openConnection();
        c.setConnectTimeout(15000); c.setReadTimeout(25000); c.setInstanceFollowRedirects(true);
        c.setRequestProperty("User-Agent", "NebulaIPTV/1.0 AuthorizedClient");
        int status = c.getResponseCode();
        InputStream in = status >= 200 && status < 400 ? c.getInputStream() : c.getErrorStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        StringBuilder out = new StringBuilder(); String line;
        while ((line = br.readLine()) != null) { out.append(line).append('\n'); if (out.length() > 25_000_000) break; }
        br.close();
        if (status < 200 || status >= 400) throw new Exception("Servidor respondeu HTTP " + status);
        return out.toString();
    }

    public Library loadM3u(String url) throws Exception { return loadM3u(url, ""); }

    public Library loadM3u(String url, String epgOverride) throws Exception {
        String body = get(url.trim());
        return parseM3u(body, epgOverride, "Playlist M3U");
    }

    public Library loadM3u(InputStream input, String epgOverride) throws Exception {
        BufferedReader reader=new BufferedReader(new InputStreamReader(input,"UTF-8"));StringBuilder body=new StringBuilder();String line;
        while((line=reader.readLine())!=null){body.append(line).append('\n');if(body.length()>25_000_000)break;}reader.close();
        return parseM3u(body.toString(),epgOverride,"Playlist local");
    }

    private Library parseM3u(String body,String epgOverride,String sourceLabel) throws Exception {
        if (!body.contains("#EXTM3U") && !body.contains("#EXTINF")) throw new Exception("A fonte não retornou uma playlist M3U válida. Portais proprietários exigem uma API documentada pelo provedor.");
        Library lib = new Library(); lib.sourceLabel = sourceLabel;
        String[] lines = body.replace("\r", "").split("\n");
        String epgUrl = epgOverride == null ? "" : epgOverride.trim();
        if (epgUrl.isEmpty() && lines.length > 0) { epgUrl = attr(lines[0], "x-tvg-url"); if (epgUrl.isEmpty()) epgUrl = attr(lines[0], "url-tvg"); }
        String info = null; int count = 0;
        for (String raw : lines) {
            String line = raw.trim();
            if (line.startsWith("#EXTINF")) info = line;
            else if (info != null && !line.isEmpty() && !line.startsWith("#")) {
                String title = info.contains(",") ? info.substring(info.lastIndexOf(',') + 1).trim() : "Canal " + (count + 1);
                String id = attr(info, "tvg-id"), logo = attr(info, "tvg-logo"), group = attr(info, "group-title");
                String kind = inferKind(group, line);
                MediaItemData item = new MediaItemData(id.isEmpty() ? "m3u-" + count : id, title, group, line, logo, group, kind);
                item.subtitleUrl = attr(info, "sub-title"); if (item.subtitleUrl.isEmpty()) item.subtitleUrl = attr(info, "tvg-subtitle");
                item.catchup = !attr(info, "catchup").isEmpty() || !attr(info, "timeshift").isEmpty();
                if ("movie".equals(kind)) lib.movies.add(item); else if ("series".equals(kind)) lib.series.add(item); else lib.live.add(item);
                count++; info = null;
            }
        }
        if (count == 0) throw new Exception("Nenhum item reproduzível foi encontrado na playlist.");
        if (!epgUrl.isEmpty()) { try { applyCurrentEpg(lib.live, epgUrl); } catch (Exception ignored) { } }
        lib.finish(); return lib;
    }

    public Library loadXtream(String base, String user, String pass) throws Exception { return loadXtream(base, user, pass, ""); }

    public Library loadXtream(String base, String user, String pass, String epgOverride) throws Exception {
        base = base.trim().replaceAll("/+$", "");
        String auth = base + "/player_api.php?username=" + enc(user) + "&password=" + enc(pass);
        JSONObject account = new JSONObject(get(auth));
        JSONObject ui = account.optJSONObject("user_info");
        if (ui == null || !"1".equals(ui.optString("auth", "0"))) throw new Exception("Credenciais Xtream recusadas pelo servidor.");
        Library lib = new Library(); lib.sourceLabel = "Xtream Codes";
        Map<String,String> liveCategories=categoryMap(auth+"&action=get_live_categories");
        Map<String,String> vodCategories=categoryMap(auth+"&action=get_vod_categories");
        Map<String,String> seriesCategories=categoryMap(auth+"&action=get_series_categories");
        JSONArray live = new JSONArray(get(auth + "&action=get_live_streams"));
        for (int i=0; i<live.length(); i++) {
            JSONObject o=live.getJSONObject(i); String id=o.optString("stream_id"),category=o.optString("category_id");
            MediaItemData m=new MediaItemData("live-"+id,o.optString("name"),o.optString("epg_channel_id"),base+"/live/"+enc(user)+"/"+enc(pass)+"/"+id+".ts",o.optString("stream_icon"),categoryName(liveCategories,category),"live");
            m.catchup="1".equals(o.optString("tv_archive")); lib.live.add(m);
        }
        try {
            JSONArray vod = new JSONArray(get(auth + "&action=get_vod_streams"));
            for (int i=0;i<vod.length();i++){ JSONObject o=vod.getJSONObject(i);String id=o.optString("stream_id"),ext=o.optString("container_extension","mp4"),category=o.optString("category_id");lib.movies.add(new MediaItemData("movie-"+id,o.optString("name"),o.optString("rating"),base+"/movie/"+enc(user)+"/"+enc(pass)+"/"+id+"."+ext,o.optString("stream_icon"),categoryName(vodCategories,category),"movie")); }
        } catch (Exception ignored) { }
        try {
            JSONArray ser = new JSONArray(get(auth + "&action=get_series"));
            for (int i=0;i<ser.length();i++){ JSONObject o=ser.getJSONObject(i);String id=o.optString("series_id"),category=o.optString("category_id");MediaItemData m=new MediaItemData("series-"+id,o.optString("name"),o.optString("plot"),"xtream-series://"+id,o.optString("cover"),categoryName(seriesCategories,category),"series");m.sourceData=base+"|"+user+"|"+pass;lib.series.add(m); }
        } catch (Exception ignored) { }
        String epg = epgOverride == null ? "" : epgOverride.trim();
        if (!epg.isEmpty()) { try { applyCurrentEpg(lib.live, epg); } catch (Exception ignored) { } }
        else { try { if (applyCurrentEpg(lib.live, base + "/xmltv.php?username=" + enc(user) + "&password=" + enc(pass)) == 0) applyCurrentEpg(lib.live, base + "/epg/" + enc(user) + "/" + enc(pass)); } catch (Exception ignored) { } }
        lib.finish(); return lib;
    }

    public List<MediaItemData> loadSeriesEpisodes(MediaItemData series) throws Exception {
        String[] p=series.sourceData.split("\\|",-1); if(p.length<3) throw new Exception("Dados da série incompletos.");
        String id=series.url.substring(series.url.indexOf("//")+2);
        String api=p[0]+"/player_api.php?username="+enc(p[1])+"&password="+enc(p[2])+"&action=get_series_info&series_id="+enc(id);
        JSONObject root=new JSONObject(get(api)); JSONObject eps=root.optJSONObject("episodes"); List<MediaItemData> out=new ArrayList<>();
        if(eps!=null) { Iterator<String> seasons=eps.keys(); while(seasons.hasNext()){String season=seasons.next();JSONArray a=eps.optJSONArray(season);if(a==null)continue;for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);String eid=o.optString("id"),ext=o.optString("container_extension","mp4");out.add(new MediaItemData("episode-"+eid,"T"+season+" · E"+o.optString("episode_num")+" — "+o.optString("title"),o.optString("info"),p[0]+"/series/"+enc(p[1])+"/"+enc(p[2])+"/"+eid+"."+ext,series.logo,series.title,"episode"));}} }
        return out;
    }

    public Library demonstration() {
        Library l=new Library(); l.sourceLabel="Demonstração autorizada";
        l.live.add(new MediaItemData("demo-live","Nebula Nature 4K","Demonstração · conteúdo aberto","https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4","","Natureza","live"));
        l.movies.add(new MediaItemData("bbb","Big Buck Bunny","Filme aberto · Blender Foundation","https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4","","Animação","movie"));
        l.movies.add(new MediaItemData("ed","Elephant Dream","Filme aberto · Blender Foundation","https://storage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4","","Ficção científica","movie"));
        l.series.add(new MediaItemData("sintel","Sintel","Curta aberto · Blender Foundation","https://storage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4","","Aventura","episode"));
        l.finish(); return l;
    }

    private static String attr(String s,String name){Matcher m=Pattern.compile(name+"=\\\"([^\\\"]*)\\\"",Pattern.CASE_INSENSITIVE).matcher(s);return m.find()?m.group(1):"";}
    private static Map<String,String> categoryMap(String url){Map<String,String> out=new HashMap<>();try{JSONArray categories=new JSONArray(get(url));for(int i=0;i<categories.length();i++){JSONObject c=categories.optJSONObject(i);if(c==null)continue;String id=c.optString("category_id"),name=c.optString("category_name").trim();if(!id.isEmpty()&&!name.isEmpty())out.put(id,name);}}catch(Exception ignored){}return out;}
    private static String categoryName(Map<String,String> names,String id){String name=names.get(id);return name==null||name.trim().isEmpty()?"Outros":name.trim();}
    private static int applyCurrentEpg(List<MediaItemData> channels,String url) throws Exception {
        String xml=get(url); XmlPullParser p=Xml.newPullParser();p.setInput(new StringReader(xml));long now=System.currentTimeMillis();String channel="",programmeTitle="";long start=0,stop=0;Map<String,String> current=new HashMap<>();
        for(int event=p.getEventType();event!=XmlPullParser.END_DOCUMENT;event=p.next()){
            if(event==XmlPullParser.START_TAG&&"programme".equals(p.getName())){channel=p.getAttributeValue(null,"channel");start=parseXmltv(p.getAttributeValue(null,"start"));stop=parseXmltv(p.getAttributeValue(null,"stop"));programmeTitle="";}
            else if(event==XmlPullParser.START_TAG&&"title".equals(p.getName())&&start<=now&&stop>=now){programmeTitle=p.nextText();}
            else if(event==XmlPullParser.END_TAG&&"programme".equals(p.getName())&&start<=now&&stop>=now&&!programmeTitle.isEmpty()){current.put(channel,programmeTitle);}
        }
        int applied=0;for(MediaItemData m:channels){String nowTitle=current.get(m.id);if(nowTitle!=null){m.subtitle="Agora: "+nowTitle;applied++;}}return applied;
    }
    private static long parseXmltv(String value){if(value==null)return 0;try{String v=value.trim();if(v.length()>=20)return new SimpleDateFormat("yyyyMMddHHmmss Z",Locale.US).parse(v.substring(0,20)).getTime();return new SimpleDateFormat("yyyyMMddHHmmss",Locale.US).parse(v.substring(0,14)).getTime();}catch(Exception e){return 0;}}
    private static String inferKind(String group,String url){String s=(group+" "+url).toLowerCase(Locale.US);if(s.contains("series")||s.contains("serie")||s.contains("episode"))return "series";if(s.contains("movie")||s.contains("filme")||s.contains("vod"))return "movie";return "live";}
    private static String enc(String s) throws Exception{return URLEncoder.encode(s,"UTF-8");}
}
