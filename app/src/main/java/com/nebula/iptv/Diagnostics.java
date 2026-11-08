package com.nebula.iptv;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class Diagnostics {
    private Diagnostics(){}
    public static String create(Context c,JSONObject profile,String lastError,IptvRepository.Library lib){
        String server="não configurado";try{String raw=profile.optString("url","");if(!raw.isEmpty()){Uri u=Uri.parse(raw.contains("://")?raw:"http://"+raw);server=(u.getHost()==null?"inválido":u.getHost())+(u.getPort()>0?":"+u.getPort():"");}}catch(Exception ignored){}
        ConnectivityManager cm=(ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);NetworkInfo ni=cm==null?null:cm.getActiveNetworkInfo();String network=ni!=null&&ni.isConnected()?ni.getTypeName()+" conectado":"sem conexão detectada";
        int live=lib==null?0:lib.live.size(),movies=lib==null?0:lib.movies.size(),series=lib==null?0:lib.series.size();
        return "DIAGNÓSTICO NEBULA IPTV\n"+
            "Gerado: "+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z",Locale.US).format(new Date())+"\n"+
            "Versão: 1.1.0-test (3)\n"+
            "Android: "+Build.VERSION.RELEASE+" (API "+Build.VERSION.SDK_INT+")\n"+
            "Dispositivo: "+Build.MANUFACTURER+" "+Build.MODEL+"\n"+
            "Rede: "+network+"\n"+
            "Tipo de perfil: "+profile.optString("mode","não configurado")+"\n"+
            "Servidor: "+server+"\n"+
            "Catálogo carregado: "+live+" TV, "+movies+" filmes, "+series+" séries\n"+
            "Último erro: "+sanitize(lastError)+"\n"+
            "\nCredenciais, URLs completas, usuário, senha e PIN foram omitidos automaticamente.";
    }
    public static void share(Context c,String report){Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_SUBJECT,"Diagnóstico Nebula IPTV");i.putExtra(Intent.EXTRA_TEXT,report);c.startActivity(Intent.createChooser(i,"Enviar diagnóstico"));}
    private static String sanitize(String s){if(s==null||s.trim().isEmpty())return"nenhum";String v=s.replaceAll("(?i)(username|user|password|pass|senha)=([^&\\s]+)","$1=***");v=v.replaceAll("(?i)/epg/[^/\\s]+/[^/\\s]+","/epg/***/***");return v.length()>300?v.substring(0,300):v;}
}
