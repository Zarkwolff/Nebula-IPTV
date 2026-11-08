package com.nebula.iptv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;
import java.text.Normalizer;
import java.util.Locale;

public final class ParentalControl {
    public interface Success { void run(); }
    private final Activity activity;private final SecureStore secure;private final SharedPreferences prefs;
    public ParentalControl(Activity activity,SecureStore secure,SharedPreferences prefs){this.activity=activity;this.secure=secure;this.prefs=prefs;}
    public boolean enabled(){return secure.hasPin()&&prefs.getBoolean("parental_enabled",true);}
    public boolean restricted(MediaItemData m){if(!enabled()||m==null)return false;return containsAdultMarker(m.title+" "+m.group+" "+m.subtitle);}
    static boolean containsAdultMarker(String value){String raw=value==null?"":value.toLowerCase(Locale.ROOT);String s=Normalizer.normalize(raw,Normalizer.Form.NFD).replaceAll("\\p{M}","");if(s.contains("adult swim")&&!s.matches(".*\\b(xxx|porno|porn|erotico|eroticos|sexy|hustler|playboy)\\b.*"))return false;String terms="adultos?|xxx|porn|porno|pornografia|erotic|erotico|eroticos|sex|sexy|playboy|hustler|penthouse|redlight|private|prive|onlyfans|mature";return s.matches(".*(^|[^a-z0-9])("+terms+")([^a-z0-9]|$).*")||s.matches(".*(^|[^0-9])(18\\+|\\+18|18\\s*anos)([^0-9]|$).*");}
    public void unlock(Success success){if(!enabled()){success.run();return;}promptVerify(success);}
    public void configure(){if(secure.hasPin())promptVerify(this::showOptions);else createPin();}
    private void promptVerify(Success success){EditText pin=pinField();AlertDialog dialog=new AlertDialog.Builder(activity).setTitle("Controle parental").setMessage("Digite o PIN para continuar.").setView(pin).setPositiveButton("Desbloquear",null).setNegativeButton("Cancelar",null).create();dialog.setOnShowListener(ignored->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{if(secure.verifyPin(pin.getText().toString())){dialog.dismiss();success.run();}else{pin.setError("PIN incorreto");pin.selectAll();}}));dialog.show();}
    private void showOptions(){String status=prefs.getBoolean("parental_enabled",true)?"Desativar bloqueio":"Ativar bloqueio";new AlertDialog.Builder(activity).setTitle("Controle parental").setItems(new String[]{status,"Alterar PIN","Remover PIN"},(d,which)->{if(which==0){boolean next=!prefs.getBoolean("parental_enabled",true);prefs.edit().putBoolean("parental_enabled",next).apply();Toast.makeText(activity,next?"Bloqueio ativado.":"Bloqueio desativado.",Toast.LENGTH_SHORT).show();}else if(which==1)createPin();else{secure.clearPin();prefs.edit().remove("parental_enabled").apply();Toast.makeText(activity,"PIN removido.",Toast.LENGTH_SHORT).show();}}).show();}
    private void createPin(){EditText first=pinField();AlertDialog dialog=new AlertDialog.Builder(activity).setTitle(secure.hasPin()?"Alterar PIN":"Criar PIN").setMessage("Use de 4 a 8 números.").setView(first).setPositiveButton("Continuar",null).setNegativeButton("Cancelar",null).create();dialog.setOnShowListener(ignored->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{String value=first.getText().toString();if(!value.matches("\\d{4,8}")){first.setError("Use de 4 a 8 números");return;}dialog.dismiss();confirmPin(value);}));dialog.show();}
    private void confirmPin(String value){EditText confirm=pinField();AlertDialog dialog=new AlertDialog.Builder(activity).setTitle("Confirmar PIN").setMessage("Digite novamente o mesmo PIN.").setView(confirm).setPositiveButton("Salvar",null).setNegativeButton("Cancelar",null).create();dialog.setOnShowListener(ignored->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{if(!value.equals(confirm.getText().toString())){confirm.setError("Os PINs não coincidem");confirm.selectAll();return;}try{secure.setPin(value);prefs.edit().putBoolean("parental_enabled",true).commit();dialog.dismiss();Toast.makeText(activity,"Controle parental ativado.",Toast.LENGTH_SHORT).show();}catch(Exception e){Toast.makeText(activity,"Não foi possível proteger o PIN.",Toast.LENGTH_LONG).show();}}));dialog.show();}
    private EditText pinField(){EditText e=new EditText(activity);e.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_PASSWORD);e.setSingleLine(true);return e;}
}
