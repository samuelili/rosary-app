package com.javilet.samuel.rosary;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

public class PlayerService extends Service implements MediaPlayer.OnCompletionListener {
    private final static String TAG = PlayerService.class.getSimpleName();

    private final PlayerService playerService = this;
    private final IBinder binder = new PlayerBinder();
    private MediaPlayer mediaPlayer = null;
    private OnPlayerChangeListener onPlayerChangeListener = null;
    private OnPlayerFinishedListener onPlayerFinishedListener = null;

    private String mysteryName = "";
    private String name = "";
    private String playingName = "";
    private JSONArray recordings = null;

    private int index = 0;
    private int played = 0;

    public PlayerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setData(String mysteryName) throws IOException, JSONException {
        this.mysteryName = mysteryName;
        // reset mediaPlayer
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        int mysteryFile = R.raw.joyful_mysteries; // default

        switch (mysteryName) { // determines resource id based on mystery name
            case "joyful":
                mysteryFile = R.raw.joyful_mysteries;
                break;
            case "sorrowful":
                mysteryFile = R.raw.sorrowful_mysteries;
                break;
            case "glorious":
                mysteryFile = R.raw.glorious_mysteries;
                break;
            case "luminous":
                mysteryFile = R.raw.luminous_mysteries;
                break;
        }

        InputStream is = getResources().openRawResource(mysteryFile);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        int n;
        while ((n = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, n);
        }

        is.close();

        JSONObject json = new JSONObject(writer.toString());

        name = json.getString("name");
        recordings = json.getJSONArray("recordings");
    }

    public void play() {
        // if mediaPlayer has not started
        if (mediaPlayer == null) {
            try {
                final JSONObject jsonObject = recordings.getJSONObject(index);
                playingName = jsonObject.getString("name");
                int audioResourceId = getResources().getIdentifier(jsonObject.getString("audio"), "raw", this.getPackageName());
                mediaPlayer = MediaPlayer.create(this, audioResourceId);
                Log.i(TAG, "Preparing");
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        Log.i(TAG, "Play");
                        mediaPlayer.start();
                        mediaPlayer.setOnCompletionListener(playerService);
                        if (onPlayerChangeListener != null) {
                            Log.i(TAG, "On Change");
                            onPlayerChangeListener.onChange(jsonObject);
                        }
                    }
                });
            } catch (JSONException e) {
                Toast.makeText(this, "Reading JSON: JSONException", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Play");
            mediaPlayer.start();
        }
    }

    public void pause() {
        if (mediaPlayer != null) {
            Log.i(TAG, "Pause");
            mediaPlayer.pause();
        }
    }

    public void previous() {
        if (index - 1 >= 0) { // check if index doesn't go below
            index--;
            played = 0;
            stop();
            Log.i(TAG, "Previous");
            play();
        } else {
            previousTopic();
        }
    }

    public void previousTopic() {
        if (index > 0) {
            index = 0;
            played = 0;
            stop();
            Log.i(TAG, "Beginning");
            play();
        } else {
            Log.i(TAG, "Previous Topic");
            String previousMysteryName = "stop";
            switch (mysteryName) { // determines resource id based on mystery name
                case "joyful":
                    previousMysteryName = "stop";
                    break;
                case "sorrowful":
                    previousMysteryName = "joyful";
                    break;
                case "glorious":
                    previousMysteryName = "sorrowful";
                    break;
                case "luminous":
                    previousMysteryName = "glorious";
                    break;
            }

            if (previousMysteryName.equals("stop")) {
                onPlayerFinishedListener.onFinished();
            } else {
                try {
                    setData(previousMysteryName);
                    index = 0;
                    played = 0;
                    play();
                } catch (IOException e) {
                    Toast.makeText(this, "Reading JSON: IOException", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                } catch (JSONException e) {
                    Toast.makeText(this, "Reading JSON: JSONException", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            Log.i(TAG, "Released");
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void next() {
        if (index + 1 < recordings.length()) { // check if index doesn't go over
            stop();
            played = 0;
            index++;
            Log.i(TAG, "Next");
            play();
        } else {
            nextTopic();
        }
    }

    public void nextTopic() {
        Log.i(TAG, "Next Topic");
        String nextMysteryName = "stop";
        switch (mysteryName) { // determines resource id based on mystery name
            case "joyful":
                nextMysteryName = "sorrowful";
                break;
            case "sorrowful":
                nextMysteryName = "glorious";
                break;
            case "glorious":
                nextMysteryName = "luminous";
                break;
            case "luminous":
                nextMysteryName = "stop";
                break;
        }

        if (nextMysteryName.equals("stop")) {
            onPlayerFinishedListener.onFinished();
        } else {
            try {
                setData(nextMysteryName);
                index = 0;
                played = 0;
                play();
            } catch (IOException e) {
                Toast.makeText(this, "Reading JSON: IOException", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (JSONException e) {
                Toast.makeText(this, "Reading JSON: JSONException", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private void repeat() {
        Log.i(TAG, "Repeat");
        mediaPlayer.seekTo(0);
        play();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.i(TAG, "Completed");
        played++; // add an iteration
        try {
            if (played >= recordings.getJSONObject(index).getInt("repeat")) { // if played required amount of times
                next();
                played = 0;
            } else { // otherwise
                repeat();
            }
        } catch (JSONException e) {
            Toast.makeText(this, "Reading JSON: JSONException", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void reset() {
        index = 0;
        played = 0;
        stop();
    }

    public boolean isPlaying() {
        Log.i(TAG, "Is Playing: " + mediaPlayer.isPlaying());
        return mediaPlayer.isPlaying();
    }

    public String getName() {
        Log.i(TAG, "Name: " + name);
        return name;
    }

    public String getPlayingName() {
        Log.i(TAG, "Playing Name: " + playingName);
        return playingName;
    }

    public void setOnPlayerChangeListener(OnPlayerChangeListener listener) {
        onPlayerChangeListener = listener;
    }

    public void setOnPlayerFinishedListener(OnPlayerFinishedListener listener) {
        onPlayerFinishedListener = listener;
    }

    public class PlayerBinder extends Binder {
        PlayerService getService() {
            Log.i(TAG, "Binding");
            return PlayerService.this;
        }
    }
}