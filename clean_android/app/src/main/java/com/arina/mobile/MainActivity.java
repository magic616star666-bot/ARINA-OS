package com.arina.mobile;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity implements RecognitionListener {
 private static final int REQ_MIC=1001;
 private SpeechRecognizer sr; private Intent sri; private TextView status, heard, log;
 @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(Color.rgb(5,6,10));getWindow().setNavigationBarColor(Color.rgb(5,6,10));setContentView(ui());setupSpeech();requestMic();addLog("ARINA clean voice core ready");}
 private View ui(){ScrollView s=new ScrollView(this);s.setBackgroundColor(Color.rgb(5,6,10));LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.VERTICAL);r.setPadding(dp(22),dp(28),dp(22),dp(28));s.addView(r);
  TextView t=txt("ARINA",36,Color.WHITE);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);r.addView(t);r.addView(txt("Clean Android Voice Core",14,Color.rgb(124,238,255)),m(0,2,0,20));
  TextView orb=txt("●",90,Color.rgb(124,238,255));orb.setGravity(Gravity.CENTER);r.addView(orb,new LinearLayout.LayoutParams(-1,dp(115)));
  status=txt("Voice status: READY",15,Color.rgb(220,230,238));status.setGravity(Gravity.CENTER);r.addView(status,m(0,0,0,14));
  Button speak=btn("TAP TO SPEAK");speak.setOnClickListener(v->listen());r.addView(speak,new LinearLayout.LayoutParams(-1,dp(58)));
  heard=txt("Say: camera, dialer, SMS, browser, or settings",15,Color.rgb(195,210,218));heard.setPadding(dp(14),dp(14),dp(14),dp(14));heard.setBackground(card());r.addView(heard,m(0,18,0,16));
  r.addView(action("Open Camera",v->camera()),m(0,0,0,8));r.addView(action("Open Dialer",v->dial()),m(0,0,0,8));r.addView(action("Open SMS Composer",v->sms()),m(0,0,0,8));r.addView(action("Open Browser",v->browser()),m(0,0,0,8));r.addView(action("Open Settings",v->settings()),m(0,0,0,16));
  TextView p=txt("Only microphone permission. No contacts, direct calls, SMS sending, camera, location, overlay, accessibility, notification listener, package scan, or APK-install permission.",13,Color.rgb(160,177,187));p.setPadding(dp(14),dp(14),dp(14),dp(14));p.setBackground(card());r.addView(p,m(0,0,0,16));
  log=txt("",12,Color.rgb(175,190,200));log.setPadding(dp(14),dp(14),dp(14),dp(14));log.setMinHeight(dp(100));log.setBackground(card());r.addView(log);return s;}
 private void setupSpeech(){if(!SpeechRecognizer.isRecognitionAvailable(this)){setStatus("Speech service unavailable");return;}sr=SpeechRecognizer.createSpeechRecognizer(this);sr.setRecognitionListener(this);sri=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);sri.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);sri.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault().toLanguageTag());sri.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,true);}
 private void requestMic(){if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},REQ_MIC);}
 private void listen(){if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){requestMic();return;}if(sr!=null){setStatus("Listening…");sr.cancel();sr.startListening(sri);addLog("STT started");}}
 private void route(String x){String c=x.toLowerCase(Locale.ROOT);if(c.contains("camera")||c.contains("photo"))camera();else if(c.contains("call")||c.contains("dial"))dial();else if(c.contains("message")||c.contains("sms")||c.contains("text"))sms();else if(c.contains("browser")||c.contains("google")||c.contains("web"))browser();else if(c.contains("setting"))settings();else {setStatus("Command heard — no safe action mapped");addLog("No mapped action: "+x);}}
 private void camera(){launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE),"Camera unavailable");} private void dial(){launch(new Intent(Intent.ACTION_DIAL,Uri.parse("tel:")),"Dialer unavailable");} private void sms(){launch(new Intent(Intent.ACTION_SENDTO,Uri.parse("smsto:")),"SMS app unavailable");} private void browser(){launch(new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.google.com")),"Browser unavailable");} private void settings(){launch(new Intent(Settings.ACTION_SETTINGS),"Settings unavailable");}
 private void launch(Intent i,String e){try{startActivity(i);}catch(ActivityNotFoundException ex){Toast.makeText(this,e,Toast.LENGTH_SHORT).show();addLog(e);}}
 private TextView txt(String x,int z,int c){TextView v=new TextView(this);v.setText(x);v.setTextSize(z);v.setTextColor(c);return v;} private Button btn(String x){Button b=new Button(this);b.setText(x);b.setAllCaps(false);b.setTextColor(Color.rgb(5,20,25));b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);GradientDrawable g=new GradientDrawable();g.setColor(Color.rgb(124,238,255));g.setCornerRadius(dp(18));b.setBackground(g);return b;} private Button action(String x,View.OnClickListener l){Button b=new Button(this);b.setText(x);b.setAllCaps(false);b.setTextColor(Color.rgb(230,240,245));b.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);b.setPadding(dp(18),0,dp(18),0);b.setBackground(card());b.setOnClickListener(l);b.setMinHeight(dp(52));return b;} private GradientDrawable card(){GradientDrawable g=new GradientDrawable();g.setColor(Color.rgb(14,18,25));g.setStroke(dp(1),Color.rgb(40,67,78));g.setCornerRadius(dp(16));return g;} private LinearLayout.LayoutParams m(int l,int t,int rr,int b){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(dp(l),dp(t),dp(rr),dp(b));return p;} private int dp(int x){return Math.round(x*getResources().getDisplayMetrics().density);} private void setStatus(String x){if(status!=null)status.setText("Voice status: "+x);} private void addLog(String x){if(log!=null){String o=log.getText().toString();log.setText(o.isEmpty()?"• "+x:o+"\n• "+x);}}
 @Override public void onReadyForSpeech(Bundle p){setStatus("Listening");} @Override public void onBeginningOfSpeech(){setStatus("Voice detected");addLog("VAD: speech started");} @Override public void onRmsChanged(float x){} @Override public void onBufferReceived(byte[] b){} @Override public void onEndOfSpeech(){setStatus("Processing");addLog("VAD: speech ended");} @Override public void onError(int e){setStatus("STT error "+e);addLog("STT error: "+e);} @Override public void onResults(Bundle b){ArrayList<String>a=b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);if(a!=null&&!a.isEmpty()){String x=a.get(0);heard.setText(x);setStatus("Command received");addLog("STT: "+x);route(x);}} @Override public void onPartialResults(Bundle b){ArrayList<String>a=b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);if(a!=null&&!a.isEmpty())heard.setText(a.get(0));} @Override public void onEvent(int t,Bundle p){}
 @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);if(r==REQ_MIC)setStatus(g.length>0&&g[0]==PackageManager.PERMISSION_GRANTED?"READY":"Microphone permission required");}
 @Override protected void onDestroy(){if(sr!=null)sr.destroy();super.onDestroy();}
}
