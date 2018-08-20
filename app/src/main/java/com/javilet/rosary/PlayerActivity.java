package com.javilet.rosary;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class PlayerActivity extends AppCompatActivity {
    private final static String TAG = PlayerActivity.class.getSimpleName();

    ImageView mysteryImage = null;
    TextView titleTextView = null;
    TextView playingRecordingTitleTextView = null;

    private PlayerService playerService = null;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i(TAG, "Binded to Service");
            PlayerService.PlayerBinder binder = (PlayerService.PlayerBinder) iBinder;
            playerService = binder.getService();
            try {
                playerService.setData(getIntent().getStringExtra("mystery"));
                playerService.play();
                playerService.setOnPlayerChangeListener(new OnPlayerChangeListener() {
                    @Override
                    public void onChange(JSONObject json) {
                        try {
                            Log.i(TAG, "Changing Picture");
                            mysteryImage.setImageResource(getResources().getIdentifier(json.getString("picture"), "drawable", PlayerActivity.this.getPackageName()));
                            Log.i(TAG, "Changing Titles");
                            titleTextView.setText(playerService.getName());
                            playingRecordingTitleTextView.setText(playerService.getPlayingName());
                        } catch (JSONException e) {
                            Toast.makeText(PlayerActivity.this, "Reading JSON: JSONException", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                });
                playerService.setOnPlayerFinishedListener(new OnPlayerFinishedListener() {
                    @Override
                    public void onFinished() {
                        goBack(titleTextView);
                    }
                });
                playerService.setOnPlayerRepeatListener(new OnPlayerRepeatListener() {
                    @Override
                    public void onRepeat(String playingName) {
                        playingRecordingTitleTextView.setText(playingName);
                    }
                });
            } catch (IOException e) {
                Toast.makeText(PlayerActivity.this, "Reading JSON: IOException", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (JSONException e) {
                Toast.makeText(PlayerActivity.this, "Reading JSON: JSONException", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // do nothing
        }
    };

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        mysteryImage = findViewById(R.id.mystery_image);
        titleTextView = findViewById(R.id.title);
        playingRecordingTitleTextView = findViewById(R.id.playing_recording_title);

        // bind to activity
        Intent serviceIntent = new Intent(this, PlayerService.class);
        startService(serviceIntent); // if it is not running already
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        final FloatingActionButton pausePlayButton = findViewById(R.id.pausePlayButton);
        ImageButton previousButton = findViewById(R.id.previousButton);
        ImageButton nextButton = findViewById(R.id.nextButton);
        pausePlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playerService.isPlaying()) {
                    playerService.pause();
                    pausePlayButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                } else {
                    playerService.play();
                    pausePlayButton.setImageResource(R.drawable.ic_pause_black_24dp);
                }
            }
        });

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playerService.previousTopic();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playerService.nextTopic();
            }
        });

        mysteryImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playerService.next();
            }
        });
    }

    private void unbind() {
        Log.i(TAG, "Unbinding from service");
        playerService.reset();
        try {
            unbindService(serviceConnection);
        } catch (IllegalArgumentException ignored) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbind();
    }

    public void goBack(View view) {
        unbind();
        NavUtils.navigateUpFromSameTask(this);
    }
}
