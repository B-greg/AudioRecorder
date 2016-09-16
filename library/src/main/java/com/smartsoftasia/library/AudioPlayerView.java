package com.smartsoftasia.library;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;

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
  private Drawable mPlayButtonResource;
  private Drawable mPauseButtonResource;
  private Handler handler = new Handler();


  public AudioPlayerView(Context context) {
    super(context);
    init(context, null);
  }

  public AudioPlayerView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public AudioPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public AudioPlayerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attributeSet) {
    Integer textColor = null;
    Drawable progressDrawable = null;
    mPlayButtonResource = ContextCompat.getDrawable(getContext(), R.drawable.aar_ic_play);
    mPauseButtonResource = ContextCompat.getDrawable(getContext(), R.drawable.aar_ic_pause);

    if (attributeSet != null) {
      TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.AudioPlayer);
      textColor = a.getColor(R.styleable.AudioPlayer_textColor, 0);
      progressDrawable = a.getDrawable(R.styleable.AudioPlayer_progressbar);
      Drawable drawablePlay = a.getDrawable(R.styleable.AudioPlayer_playButton);
      Drawable drawablePause = a.getDrawable(R.styleable.AudioPlayer_pauseButton);
      if (drawablePlay != null) {
        mPlayButtonResource = drawablePlay;
      }
      if (drawablePause != null) {
        mPauseButtonResource = drawablePause;
      }
    }


    mInflater = LayoutInflater.from(context);
    View v = mInflater.inflate(R.layout.layout_audio_player_view, this, true);
    mPLayButton = (ImageButton) v.findViewById(R.id.button_play);
    mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar);
    mTimeTextView = (TextView) v.findViewById(R.id.textView);

    if (textColor != null && textColor != 0) {
      mTimeTextView.setTextColor(textColor);
    }

    if (progressDrawable != null) {
      mProgressBar.setProgressDrawable(progressDrawable);
    }

    mPLayButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        onPlayButtonClick();
      }
    });


  }


  public void setTextColor(@ColorRes int color) {
    mTimeTextView.setTextColor(ContextCompat.getColor(getContext(), color));
  }

  public void setProgressDrawable(@DrawableRes int drawable) {
    mProgressBar.setProgressDrawable(ContextCompat.getDrawable(getContext(), drawable));
  }

  public void setPauseButtonResource(Drawable pauseButtonResource) {
    mPauseButtonResource = pauseButtonResource;
    updateButton();
  }

  public void setPlayButtonResource(Drawable playButtonResource) {
    mPlayButtonResource = playButtonResource;
    updateButton();
  }

  public void setMediaUrl(String mediaUrl) {
    mMediaUrl = mediaUrl;
    try {
      prepareMediaFile();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void updateButton(){
    if (mPlayer.isPlaying()){
      mPLayButton.setImageDrawable(mPauseButtonResource);
    }else{
      mPLayButton.setImageDrawable(mPlayButtonResource);

    }
  }

  private void prepareMediaFile() throws IOException {
    if (mPlayer == null) {
      initPlayer();
    } else {
      mPlayer.stop();
      mPlayer.reset();
    }
    mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    mPlayer.setDataSource(mMediaUrl);
    mPlayer.prepareAsync();
  }

  private void initPlayer() {
    mPlayer = new MediaPlayer();
    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
      @Override
      public void onCompletion(MediaPlayer mp) {
        mPLayButton.setImageDrawable(mPlayButtonResource);
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
      mPLayButton.setImageDrawable(mPlayButtonResource);
    } else {
      mPlayer.start();
      mPLayButton.setImageDrawable(mPauseButtonResource);
      setupProgressbar();
    }
  }

  private void setupProgressbar() {
    final int duration = mPlayer.getDuration();
    final int UPDATE_INTERVAL = 41;
    final Runnable r = new Runnable() {
      public void run() {
        int currentPosition = mPlayer.getCurrentPosition();

        if (currentPosition < duration) {
          int position = Math.round((currentPosition * 100) / duration);
          mProgressBar.setProgress(position);
          mTimeTextView.setText(Util.formatMiliSeconds(currentPosition));
          if (mPlayer.isPlaying()) {
            handler.postDelayed(this, UPDATE_INTERVAL);
          }
        }
      }
    };
    if (duration > 0) {
      handler.postDelayed(r, UPDATE_INTERVAL);
    }

  }
}
