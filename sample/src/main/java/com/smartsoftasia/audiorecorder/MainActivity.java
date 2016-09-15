package com.smartsoftasia.audiorecorder;

import android.Manifest;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.smartsoftasia.library.AndroidAudioRecorder;
import com.smartsoftasia.library.Util;

public class MainActivity extends AppCompatActivity {

  private static final String AUDIO_FILE_PATH = Environment.getExternalStorageDirectory() + "/recorded_audio.wav";
  private static final int RECORD_AUDIO = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (getSupportActionBar() != null) {
      getSupportActionBar().setBackgroundDrawable(
          new ColorDrawable(getResources().getColor(R.color.colorPrimaryDark)));
    }
  }

  public void recordAudio(View v) {
    AndroidAudioRecorder.with(this)
        .setFilePath(AUDIO_FILE_PATH)
        .setColor(getResources().getColor(R.color.recorder_bg))
        .setRequestCode(RECORD_AUDIO)
        .record();
  }
}
