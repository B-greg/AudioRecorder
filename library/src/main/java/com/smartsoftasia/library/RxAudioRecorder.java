package com.smartsoftasia.library;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.IntegerRes;
import android.support.annotation.Nullable;

import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by androiddev01 on 9/15/2016 AD.
 */
public class RxAudioRecorder {

  public static final String EXTRA_FILE_PATH = "filePath";
  public static final String EXTRA_COLOR = "color";

  private static RxAudioRecorder instance;

  private String dirName = "audio";
  private int color = Color.parseColor("#546E7A");

  public static synchronized RxAudioRecorder with(Context context) {
    if (instance == null) {
      instance = new RxAudioRecorder(context.getApplicationContext());
    }
    return instance;
  }

  private Context context;
  private PublishSubject<String> publishSubject;

  private RxAudioRecorder(Context context) {
    this.context = context;
  }

  public Observable<String> getActiveSubscription() {
    return publishSubject;
  }

  public Observable<String> requestAudio(@Nullable  String dirName, @Nullable Integer color ) {
    if (dirName!= null){
      this.dirName = dirName;
    }
    if ( color != null){
      this.color = color;
    }
    publishSubject = PublishSubject.create();
    startAudioRecordPickHiddenActivity();
    return publishSubject;
  }



  void onAudioPicked(String filePath) {
    if (publishSubject != null) {
      publishSubject.onNext(filePath);
      publishSubject.onCompleted();
    }
  }

  private void startAudioRecordPickHiddenActivity() {
    Intent intent = new Intent(context, AudioRecorderActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.putExtra(EXTRA_FILE_PATH, dirName);
    intent.putExtra(EXTRA_COLOR, color);
    context.startActivity(intent);
  }



}


