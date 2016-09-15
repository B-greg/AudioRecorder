package com.smartsoftasia.library;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.cleveroad.audiovisualization.DbmHandler;
import com.cleveroad.audiovisualization.GLAudioVisualizationView;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import omrecorder.AudioChunk;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.Recorder;

public class AudioRecorderActivity extends AppCompatActivity
    implements PullTransport.OnAudioChunkPulledListener, MediaPlayer.OnCompletionListener {

  private MediaPlayer player;
  private Recorder recorder;
  private VisualizerHandler visualizerHandler;

  private String filePath;
  private int color;

  private Timer timer;
  private MenuItem saveMenuItem;
  private int recorderSecondsElapsed;
  private int playerSecondsElapsed;
  private boolean isRecording;

  private RelativeLayout contentLayout;
  private GLAudioVisualizationView visualizerView;
  private TextView statusView;
  private TextView timerView;
  private ImageButton restartView;
  private ImageButton recordView;
  private ImageButton playView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.aar_activity_audio_recorder);


    if (savedInstanceState != null) {
      filePath = savedInstanceState.getString(RxAudioRecorder.EXTRA_FILE_PATH);
      color = savedInstanceState.getInt(RxAudioRecorder.EXTRA_COLOR);
    } else {
      handleIntent(getIntent());
      filePath = getIntent().getStringExtra(RxAudioRecorder.EXTRA_FILE_PATH);
      color = getIntent().getIntExtra(RxAudioRecorder.EXTRA_COLOR, Color.BLACK);
    }



    if (getSupportActionBar() != null) {
      getSupportActionBar().setHomeButtonEnabled(true);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setDisplayShowTitleEnabled(false);
      getSupportActionBar().setElevation(0);
      getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Util.getDarkerColor(color)));
      getSupportActionBar().setHomeAsUpIndicator(ContextCompat.getDrawable(this, R.drawable.aar_ic_clear));
    }

    visualizerView = new GLAudioVisualizationView.Builder(this).setLayersCount(1)
        .setWavesCount(6)
        .setWavesHeight(R.dimen.aar_wave_height)
        .setWavesFooterHeight(R.dimen.aar_footer_height)
        .setBubblesPerLayer(20)
        .setBubblesSize(R.dimen.aar_bubble_size)
        .setBubblesRandomizeSize(true)
        .setBackgroundColor(Util.getDarkerColor(color))
        .setLayerColors(new int[] { color })
        .build();

    contentLayout = (RelativeLayout) findViewById(R.id.content);
    statusView = (TextView) findViewById(R.id.status);
    timerView = (TextView) findViewById(R.id.timer);
    restartView = (ImageButton) findViewById(R.id.restart);
    recordView = (ImageButton) findViewById(R.id.record);
    playView = (ImageButton) findViewById(R.id.play);

    contentLayout.setBackgroundColor(Util.getDarkerColor(color));
    contentLayout.addView(visualizerView, 0);
    restartView.setVisibility(View.INVISIBLE);
    playView.setVisibility(View.INVISIBLE);

    if (Util.isBrightColor(color)) {
      ContextCompat.getDrawable(this, R.drawable.aar_ic_clear)
          .setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
      ContextCompat.getDrawable(this, R.drawable.aar_ic_check)
          .setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
      statusView.setTextColor(Color.BLACK);
      timerView.setTextColor(Color.BLACK);
      restartView.setColorFilter(Color.BLACK);
      recordView.setColorFilter(Color.BLACK);
      playView.setColorFilter(Color.BLACK);
    }

  }


  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                         int[] grantResults) {
    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      handleIntent(getIntent());
    } else {
      finish();
    }
  }


  @Override
  protected void onNewIntent(Intent intent) {
    handleIntent(intent);
  }

  @Override
  public void onResume() {
    super.onResume();
    try {
      visualizerView.onResume();
    } catch (Exception e) {
    }
  }

  @Override
  protected void onPause() {
    restartRecording(null);
    try {
      visualizerView.onPause();
    } catch (Exception e) {
    }
    super.onPause();
  }

  @Override
  protected void onDestroy() {
    restartRecording(null);
    setResult(RESULT_CANCELED);
    try {
      visualizerView.release();
    } catch (Exception e) {
    }
    super.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    outState.putString(RxAudioRecorder.EXTRA_FILE_PATH, filePath);
    outState.putInt(RxAudioRecorder.EXTRA_COLOR, color);
    super.onSaveInstanceState(outState);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.aar_audio_recorder, menu);
    saveMenuItem = menu.findItem(R.id.action_save);
    saveMenuItem.setIcon(getResources().getDrawable(R.drawable.aar_ic_check));
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int i = item.getItemId();
    if (i == android.R.id.home) {
      finish();
    } else if (i == R.id.action_save) {
      selectAudio();
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onAudioChunkPulled(AudioChunk audioChunk) {
    float amplitude = isRecording ? (float) audioChunk.maxAmplitude() : 0f;
    visualizerHandler.onDataReceived(amplitude);
  }

  @Override
  public void onCompletion(MediaPlayer mediaPlayer) {
    stopPlaying();
  }

  private void selectAudio() {
    stopRecording();
    RxAudioRecorder.with(this).onAudioPicked(filePath);
    finish();
  }

  public void toggleRecording(View v) {
    stopPlaying();
    Util.wait(100, new Runnable() {
      @Override
      public void run() {
        if (isRecording) {
          pauseRecording();
        } else {
          resumeRecording();
        }
      }
    });
  }

  public void togglePlaying(View v) {
    pauseRecording();
    Util.wait(100, new Runnable() {
      @Override
      public void run() {
        if (isPlaying()) {
          stopPlaying();
        } else {
          startPlaying();
        }
      }
    });
  }

  public void restartRecording(View v) {
    if (isRecording) {
      stopRecording();
    } else if (isPlaying()) {
      stopPlaying();
    } else {
      visualizerHandler = new VisualizerHandler();
      visualizerView.linkTo(visualizerHandler);
      visualizerView.release();
      if (visualizerHandler != null) {
        visualizerHandler.stop();
      }
    }
    if(saveMenuItem != null){
      saveMenuItem.setVisible(false);
    }
    if(statusView != null){
      statusView.setVisibility(View.INVISIBLE);
    }
    if(restartView != null){
      restartView.setVisibility(View.INVISIBLE);
    }
    if(playView != null){
      playView.setVisibility(View.INVISIBLE);
    }
    if(recordView != null){
      recordView.setImageResource(R.drawable.aar_ic_rec);
    }
    if(timerView != null){
      timerView.setText("00:00:00");
    }
    recorderSecondsElapsed = 0;
    playerSecondsElapsed = 0;
  }

  private void resumeRecording() {
    isRecording = true;
    saveMenuItem.setVisible(false);
    statusView.setText(R.string.aar_recording);
    statusView.setVisibility(View.VISIBLE);
    restartView.setVisibility(View.INVISIBLE);
    playView.setVisibility(View.INVISIBLE);
    recordView.setImageResource(R.drawable.aar_ic_pause);
    playView.setImageResource(R.drawable.aar_ic_play);

    visualizerHandler = new VisualizerHandler();
    visualizerView.linkTo(visualizerHandler);

    if (recorder == null) {
      timerView.setText("00:00:00");

      recorder = OmRecorder.wav(
          new PullTransport.Default(Util.getMic(), AudioRecorderActivity.this), new File(filePath));
    }
    recorder.resumeRecording();

    startTimer();
  }

  private void pauseRecording() {
    isRecording = false;
    if (!isFinishing()) {
      saveMenuItem.setVisible(true);
    }
    statusView.setText(R.string.aar_paused);
    statusView.setVisibility(View.VISIBLE);
    restartView.setVisibility(View.VISIBLE);
    playView.setVisibility(View.VISIBLE);
    recordView.setImageResource(R.drawable.aar_ic_rec);
    playView.setImageResource(R.drawable.aar_ic_play);

    visualizerView.release();
    if (visualizerHandler != null) {
      visualizerHandler.stop();
    }

    if (recorder != null) {
      recorder.pauseRecording();
    }

    stopTimer();
  }

  private void stopRecording() {
    visualizerView.release();
    if (visualizerHandler != null) {
      visualizerHandler.stop();
    }

    recorderSecondsElapsed = 0;
    if (recorder != null) {
      recorder.stopRecording();
      recorder = null;
    }

    stopTimer();
  }

  private void startPlaying() {
    try {
      stopRecording();
      player = new MediaPlayer();
      player.setDataSource(filePath);
      player.prepare();
      player.start();

      visualizerView.linkTo(DbmHandler.Factory.newVisualizerHandler(this, player));
      visualizerView.post(new Runnable() {
        @Override
        public void run() {
          player.setOnCompletionListener(AudioRecorderActivity.this);
        }
      });

      timerView.setText("00:00:00");
      statusView.setText(R.string.aar_playing);
      statusView.setVisibility(View.VISIBLE);
      playView.setImageResource(R.drawable.aar_ic_stop);

      playerSecondsElapsed = 0;
      startTimer();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void stopPlaying() {
    statusView.setText("");
    statusView.setVisibility(View.INVISIBLE);
    playView.setImageResource(R.drawable.aar_ic_play);

    visualizerView.release();
    if (visualizerHandler != null) {
      visualizerHandler.stop();
    }

    if (player != null) {
      try {
        player.stop();
        player.reset();
      } catch (Exception e) {
      }
    }

    stopTimer();
  }

  private boolean isPlaying() {
    try {
      return player != null && player.isPlaying() && !isRecording;
    } catch (Exception e) {
      return false;
    }
  }

  private void startTimer() {
    stopTimer();
    timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        updateTimer();
      }
    }, 0, 1000);
  }

  private void stopTimer() {
    if (timer != null) {
      timer.cancel();
      timer.purge();
      timer = null;
    }
  }

  private void updateTimer() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (isRecording) {
          recorderSecondsElapsed++;
          timerView.setText(Util.formatSeconds(recorderSecondsElapsed));
        } else if (isPlaying()) {
          playerSecondsElapsed++;
          timerView.setText(Util.formatSeconds(playerSecondsElapsed));
        }
      }
    });
  }

  private void handleIntent(Intent intent) {
    if (!checkPermission()) {
      return;
    }



  }

  private boolean checkPermission() {
    if (ContextCompat.checkSelfPermission(AudioRecorderActivity.this,
        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(AudioRecorderActivity.this,
          new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
      return false;
    } else if (ContextCompat.checkSelfPermission(AudioRecorderActivity.this,
        Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(AudioRecorderActivity.this,
          new String[] { Manifest.permission.RECORD_AUDIO }, 0);
      return false;
    } else {
      return true;
    }
  }
}
