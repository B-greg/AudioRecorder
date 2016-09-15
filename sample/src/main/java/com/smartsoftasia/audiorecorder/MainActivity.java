package com.smartsoftasia.audiorecorder;

import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.smartsoftasia.library.AudioPlayerView;
import com.smartsoftasia.library.RxAudioRecorder;

import rx.functions.Action1;

public class MainActivity extends AppCompatActivity {

  private static final String AUDIO_FILE_PATH = Environment.getExternalStorageDirectory() + "/recorded_audio.wav";
  private static final int RECORD_AUDIO = 0;

  private AudioPlayerView mAudioPlayerView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mAudioPlayerView = (AudioPlayerView) findViewById(R.id.audioplayerview);

    if (getSupportActionBar() != null) {
      getSupportActionBar().setBackgroundDrawable(
          new ColorDrawable(getResources().getColor(R.color.colorPrimaryDark)));
    }
  }

  public void recordAudio(View v) {
    RxAudioRecorder.with(getApplicationContext()).requestAudio(AUDIO_FILE_PATH, getResources().getColor(R.color.recorder_bg)).subscribe(new Action1<String>() {
      @Override
      public void call(String s) {
        mAudioPlayerView.setMediaUrl(s);
      }
    });

  }
}
