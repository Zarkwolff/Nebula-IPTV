package com.nebula.iptv;

import android.net.Uri;
import android.util.Base64;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;

public final class ConfigImporter {
    public static class Config { public String mode,url,user,pass,epg; }
    private ConfigImporter(){}
    public static Config parse(Uri uri)throws Exception{
        if(uri==null||!"nebula".equalsIgnoreCase(uri.getScheme())||!"import".equalsIgnoreCase(uri.getHost()))throw new Exception("QR Code não pertence ao Nebula IPTV.");
        JSONObject o;String data=uri.getQueryParameter("data");
        if(data!=null&&!data.isEmpty()){byte[] raw=Base64.decode(data,Base64.URL_SAFE|Base64.NO_WRAP|Base64.NO_PADDING);if(raw.length>8192)throw new Exception("Configuração QR muito grande.");o=new JSONObject(new String(raw,StandardCharsets.UTF_8));}
        else{o=new JSONObject();o.put("mode",uri.getQueryParameter("mode"));o.put("url",first(uri,"url","server"));o.put("user",uri.getQueryParameter("user"));o.put("pass",first(uri,"pass","password"));o.put("epg",uri.getQueryParameter("epg"));}
        Config c=new Config();c.mode=clean(o.optString("mode","xtream"),16);c.url=clean(o.optString("url",o.optString("server","")),2048);c.user=clean(o.optString("user",""),512);c.pass=clean(o.optString("pass",o.optString("password","")),512);c.epg=clean(o.optString("epg",""),2048);
        if(!c.mode.matches("xtream|m3u|portal"))throw new Exception("Tipo de perfil desconhecido.");validateUrl(c.url,"servidor/playlist");if(!c.epg.isEmpty())validateUrl(c.epg,"EPG");if("xtream".equals(c.mode)&&(c.user.isEmpty()||c.pass.isEmpty()))throw new Exception("QR Xtream sem usuário ou senha.");return c;
    }
    private static String first(Uri u,String a,String b){String v=u.getQueryParameter(a);return v==null?u.getQueryParameter(b):v;}
    private static String clean(String s,int max)throws Exception{if(s==null)return"";s=s.trim();if(s.length()>max)throw new Exception("Campo de configuração excede o limite.");return s;}
    private static void validateUrl(String value,String label)throws Exception{if(value.isEmpty())throw new Exception("QR sem "+label+".");Uri u=Uri.parse(value);String scheme=u.getScheme();if(!"http".equalsIgnoreCase(scheme)&&!"https".equalsIgnoreCase(scheme))throw new Exception("URL de "+label+" deve usar HTTP ou HTTPS.");if(u.getHost()==null||u.getHost().isEmpty())throw new Exception("Host inválido no "+label+".");}
}
