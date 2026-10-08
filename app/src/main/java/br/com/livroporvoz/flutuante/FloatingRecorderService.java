package br.com.livroporvoz.flutuante;
import android.app.*;
import android.content.*;
import android.graphics.PixelFormat;
import android.media.MediaRecorder;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
public class FloatingRecorderService extends Service {
  static final String CHANNEL="recorder_status";WindowManager wm;View bubble;MediaRecorder recorder;File recording;boolean recordingNow=false;
  @Override public void onCreate(){super.onCreate();NotificationManager nm=getSystemService(NotificationManager.class);nm.createNotificationChannel(new NotificationChannel(CHANNEL,"Gravador ativo",NotificationManager.IMPORTANCE_LOW));startForeground(101,notification("Botão flutuante ativo — toque para gravar"));showBubble();}
  Notification notification(String message){Intent open=new Intent(this,MainActivity.class);PendingIntent pi=PendingIntent.getActivity(this,0,open,PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);return new Notification.Builder(this,CHANNEL).setSmallIcon(android.R.drawable.ic_btn_speak_now).setContentTitle("Livro por Voz").setContentText(message).setContentIntent(pi).setOngoing(true).build();}
  void showBubble(){if(!Settings.canDrawOverlays(this)){stopSelf();return;}wm=(WindowManager)getSystemService(WINDOW_SERVICE);TextView b=new TextView(this);b.setText("🎙");b.setTextSize(29);b.setGravity(Gravity.CENTER);b.setBackgroundColor(0xff26344b);b.setTextColor(0xffffffff);bubble=b;WindowManager.LayoutParams lp=new WindowManager.LayoutParams(150,150,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);lp.gravity=Gravity.TOP|Gravity.START;lp.x=20;lp.y=280;wm.addView(bubble,lp);b.setOnTouchListener(new View.OnTouchListener(){float dx,dy;int x,y;public boolean onTouch(View v,MotionEvent e){switch(e.getAction()){case MotionEvent.ACTION_DOWN:x=lp.x;y=lp.y;dx=e.getRawX();dy=e.getRawY();return true;case MotionEvent.ACTION_MOVE:lp.x=x+(int)(e.getRawX()-dx);lp.y=y+(int)(e.getRawY()-dy);wm.updateViewLayout(bubble,lp);return true;case MotionEvent.ACTION_UP:if(Math.abs(e.getRawX()-dx)<20&&Math.abs(e.getRawY()-dy)<20)v.performClick();return true;}return false;}});b.setOnClickListener(v->{if(recordingNow)stopRecording();else startRecording();});b.setOnLongClickListener(v->{if(recordingNow)stopRecording();stopSelf();return true;});}
  void startRecording(){try{File dir=new File(getFilesDir(),"recordings");if(!dir.exists()&&!dir.mkdirs())throw new Exception("Pasta indisponível");recording=new File(dir,"voz_"+new SimpleDateFormat("yyyyMMdd_HHmmss_SSS",Locale.ROOT).format(new Date())+".m4a");recorder=new MediaRecorder();recorder.setAudioSource(MediaRecorder.AudioSource.MIC);recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);recorder.setOutputFile(recording.getAbsolutePath());recorder.prepare();recorder.start();recordingNow=true;((TextView)bubble).setText("■");((TextView)bubble).setBackgroundColor(0xffb91c1c);getSystemService(NotificationManager.class).notify(101,notification("Gravando áudio — toque no botão vermelho para parar"));}catch(Exception e){releaseRecorder();Toast.makeText(this,"Falha ao gravar: "+e.getMessage(),Toast.LENGTH_LONG).show();}}
  void stopRecording(){if(!recordingNow)return;recordingNow=false;boolean saved=true;try{recorder.stop();}catch(Exception e){saved=false;if(recording!=null)recording.delete();}finally{releaseRecorder();}((TextView)bubble).setText("🎙");((TextView)bubble).setBackgroundColor(0xff26344b);getSystemService(NotificationManager.class).notify(101,notification("Botão flutuante ativo — toque para gravar"));Toast.makeText(this,saved?"Áudio salvo neste aparelho":"Falha ao finalizar gravação",Toast.LENGTH_SHORT).show();}
  void releaseRecorder(){if(recorder!=null){try{recorder.reset();recorder.release();}catch(Exception ignored){}recorder=null;}}
  @Override public int onStartCommand(Intent intent,int flags,int startId){return START_NOT_STICKY;}
  @Override public void onDestroy(){if(recordingNow)stopRecording();releaseRecorder();if(wm!=null&&bubble!=null)wm.removeView(bubble);super.onDestroy();}
  @Override public IBinder onBind(Intent i){return null;}
}