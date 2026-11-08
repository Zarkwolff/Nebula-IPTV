package com.nebula.iptv;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;

public class SecureStore {
    private static final String KEY_ALIAS="nebula_profile_aes_v1", PREF="nebula_secure", PROFILE="profile", PIN="parental_pin";
    private final SharedPreferences encrypted;

    public SecureStore(Context context){encrypted=context.getSharedPreferences(PREF,Context.MODE_PRIVATE);}

    public void migrateLegacy(SharedPreferences legacy) throws Exception {
        if(encrypted.contains(PROFILE)||!legacy.contains("mode"))return;
        putProfile(legacy.getString("mode",""),legacy.getString("url",""),legacy.getString("user",""),legacy.getString("pass",""),legacy.getString("epg",""));
        legacy.edit().remove("mode").remove("url").remove("user").remove("pass").remove("epg").apply();
    }

    public void putProfile(String mode,String url,String user,String pass,String epg) throws Exception {
        JSONObject o=new JSONObject();o.put("mode",safe(mode));o.put("url",safe(url));o.put("user",safe(user));o.put("pass",safe(pass));o.put("epg",safe(epg));encrypted.edit().putString(PROFILE,encrypt(o.toString())).apply();
    }

    public JSONObject getProfile(){try{String value=encrypted.getString(PROFILE,"");return value.isEmpty()?new JSONObject():new JSONObject(decrypt(value));}catch(Exception e){return new JSONObject();}}
    public void clearProfile(){encrypted.edit().remove(PROFILE).apply();}

    public void setPin(String pin) throws Exception {
        if(pin==null||!pin.matches("\\d{4,8}"))throw new IllegalArgumentException("O PIN deve ter de 4 a 8 números.");
        byte[] salt=new byte[16];new SecureRandom().nextBytes(salt);byte[] hash=derive(pin,salt);JSONObject o=new JSONObject();o.put("salt",Base64.encodeToString(salt,Base64.NO_WRAP));o.put("hash",Base64.encodeToString(hash,Base64.NO_WRAP));encrypted.edit().putString(PIN,encrypt(o.toString())).apply();
    }
    public boolean hasPin(){return encrypted.contains(PIN);}
    public boolean verifyPin(String pin){try{JSONObject o=new JSONObject(decrypt(encrypted.getString(PIN,"")));byte[] salt=Base64.decode(o.getString("salt"),Base64.NO_WRAP);byte[] expected=Base64.decode(o.getString("hash"),Base64.NO_WRAP);return MessageDigest.isEqual(expected,derive(pin,salt));}catch(Exception e){return false;}}
    public void clearPin(){encrypted.edit().remove(PIN).apply();}

    private byte[] derive(String pin,byte[] salt)throws Exception{KeySpec spec=new PBEKeySpec(pin.toCharArray(),salt,120000,256);try{return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();}catch(Exception e){return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).getEncoded();}}
    private String encrypt(String plain)throws Exception{Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.ENCRYPT_MODE,key());byte[] iv=c.getIV(),data=c.doFinal(plain.getBytes(StandardCharsets.UTF_8));byte[] all=new byte[iv.length+data.length];System.arraycopy(iv,0,all,0,iv.length);System.arraycopy(data,0,all,iv.length,data.length);return Base64.encodeToString(all,Base64.NO_WRAP);}
    private String decrypt(String encoded)throws Exception{byte[] all=Base64.decode(encoded,Base64.NO_WRAP),iv=new byte[12],data=new byte[all.length-12];System.arraycopy(all,0,iv,0,12);System.arraycopy(all,12,data,0,data.length);Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.DECRYPT_MODE,key(),new GCMParameterSpec(128,iv));return new String(c.doFinal(data),StandardCharsets.UTF_8);}
    private SecretKey key()throws Exception{KeyStore ks=KeyStore.getInstance("AndroidKeyStore");ks.load(null);if(ks.containsAlias(KEY_ALIAS))return((KeyStore.SecretKeyEntry)ks.getEntry(KEY_ALIAS,null)).getSecretKey();KeyGenerator kg=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");kg.init(new KeyGenParameterSpec.Builder(KEY_ALIAS,KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).setRandomizedEncryptionRequired(true).build());return kg.generateKey();}
    private static String safe(String s){return s==null?"":s;}
}
