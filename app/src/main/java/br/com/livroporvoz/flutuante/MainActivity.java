package br.com.livroporvoz.flutuante;
import android.Manifest;
import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.widget.*;
import java.io.File;
import java.util.*;
public class MainActivity extends Activity {
  LinearLayout root;
  @Override public void onCreate(Bundle b){super.onCreate(b);root=new LinearLayout(this);root.setOrientation(1);root.setPadding(32,40,32,20);setContentView(root);
    TextView info=new TextView(this);info.setText("Livro por Voz — gravador flutuante\n\nOs áudios são armazenados somente neste aparelho, na área privada deste aplicativo.\n\nAtive o botão enquanto o app está aberto; depois use outros aplicativos.");root.addView(info);
    button("Ativar botão flutuante",()->startFloating());button("Ver gravações locais",()->listRecordings());button("Abrir Livro por Voz (site separado)",()->startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://livro-por-voz.vercel.app"))));
    if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=getPackageManager().PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},12);
  }
  void button(String title,Runnable run){Button b=new Button(this);b.setText(title);root.addView(b);b.setOnClickListener(v->run.run());}
  void startFloating(){if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=getPackageManager().PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},11);return;}if(!Settings.canDrawOverlays(this)){startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())));Toast.makeText(this,"Autorize a sobreposição e volte para ativar.",Toast.LENGTH_LONG).show();return;}Intent i=new Intent(this,FloatingRecorderService.class);startForegroundService(i);Toast.makeText(this,"Botão flutuante ativado",Toast.LENGTH_SHORT).show();}
  void listRecordings(){File dir=new File(getFilesDir(),"recordings");File[] files=dir.listFiles((d,n)->n.endsWith(".m4a"));if(files==null||files.length==0){new AlertDialog.Builder(this).setMessage("Nenhuma gravação salva.").setPositiveButton("OK",null).show();return;}Arrays.sort(files,(a,b)->Long.compare(b.lastModified(),a.lastModified()));String[] names=Arrays.stream(files).map(File::getName).toArray(String[]::new);new AlertDialog.Builder(this).setTitle("Gravações locais").setItems(names,(d,which)->{File f=files[which];new AlertDialog.Builder(this).setTitle(f.getName()).setItems(new String[]{"Reproduzir","Excluir"},(dlg,action)->{if(action==0){try{android.media.MediaPlayer player=new android.media.MediaPlayer();player.setDataSource(f.getAbsolutePath());player.prepare();player.start();player.setOnCompletionListener(p->{p.release();});Toast.makeText(this,"Reproduzindo",Toast.LENGTH_SHORT).show();}catch(Exception e){Toast.makeText(this,"Erro na reprodução",Toast.LENGTH_LONG).show();}}else new AlertDialog.Builder(this).setMessage("Excluir definitivamente este áudio?").setNegativeButton("Cancelar",null).setPositiveButton("Excluir",(x,y)->{if(!f.delete())Toast.makeText(this,"Não foi possível excluir",Toast.LENGTH_LONG).show();}).show();}).show();}).setNegativeButton("Fechar",null).show();}
  @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] results){super.onRequestPermissionsResult(requestCode,permissions,results);if(requestCode==11&&results.length>0&&results[0]==getPackageManager().PERMISSION_GRANTED)startFloating();}
}