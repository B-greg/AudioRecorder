package com.smartsoftasia.library;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by androiddev01 on 9/15/2016 AD.
 */
public class AudioPlayerView extends LinearLayout {

  private LayoutInflater mInflater;
  private MediaPlayer mPlayer;
  private ImageButton mPLayButton;
  private TextView mTimeTextView;
  private String mMediaUrl;
  private ProgressBar mProgressBar;
  private Handler handler = new Handler();


  public AudioPlayerView(Context context) {
    super(context);
    init(context);
  }

  public AudioPlayerView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public AudioPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public AudioPlayerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init(context);
  }

  private void init(Context context) {
    mInflater = LayoutInflater.from(context);
    View v = mInflater.inflate(R.layout.layout_audio_player_view, this, true);
    mPLayButton = (ImageButton) v.findViewById(R.id.button_play);
    mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar);
    mTimeTextView = (TextView) v.findViewById(R.id.textView);
    mPLayButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        onPlayButtonClick();
      }
    });


  }

  public void setMediaUrl(String mediaUrl) {
    mMediaUrl = mediaUrl;
    try {
      prepareMediaFile();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void prepareMediaFile() throws IOException {
    if (mPlayer == null){
      initPlayer();
    }else{
      mPlayer.stop();
      mPlayer.reset();
    }
    mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    mPlayer.setDataSource(mMediaUrl);
    mPlayer.prepareAsync();
  }

  private void initPlayer(){
    mPlayer = new MediaPlayer();
    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
      @Override
      public void onCompletion(MediaPlayer mp) {
        mPLayButton.setImageResource(R.drawable.aar_ic_play);
      }
    });
    mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
      @Override
      public void onPrepared(MediaPlayer mp) {
        setupProgressbar();
      }
    });
  }

  private void onPlayButtonClick() {
    if (mPlayer.isPlaying()) {
      mPlayer.pause();
      mPLayButton.setImageResource(R.drawable.aar_ic_play);
    } else {
      mPlayer.start();
      mPLayButton.setImageResource(R.drawable.aar_ic_pause);
      setupProgressbar();
    }
  }

  private void setupProgressbar(){
    final int duration = mPlayer.getDuration();
    final int UPDATE_INTERVAL = 41;
    final Runnable r = new Runnable() {
      public void run() {
        int currentPosition = mPlayer.getCurrentPosition();

        if (currentPosition < duration) {
          int position = Math.round((currentPosition*100)/duration);
          mProgressBar.setProgress(position);
          mTimeTextView.setText(Util.formatMiliSeconds(currentPosition));
          if (mPlayer.isPlaying()){
            handler.postDelayed(this, UPDATE_INTERVAL);
          }
        }
      }
    };
    if (duration > 0){
      handler.postDelayed(r, UPDATE_INTERVAL);
    }

  }
}
