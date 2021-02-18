package sk.it.android.mp.service;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;

import sk.it.android.mp.util.Track;

public class MediaPlayerService extends Service {

    MediaPlayer mediaPlayer;
    Track track;
    Uri uri;

    private final IBinder iBinder = new LocalBinder();
    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }

    @Override
    public void onCreate() {
        mediaPlayer = new MediaPlayer();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (intent.getAction()) {
            case "PLAY":
                actionPlay(intent);
                break;
            case "PAUSE":
                actionPause();
                break;
            case "RESUME":
                actionResume();
                break;
            case "SEEK_TIME_TO":
                actionSeekTimeTo(intent);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void actionPlay(Intent intent) {
        if (track != null) {
            Track newTrack = intent.getParcelableExtra("track");
            if (track.getId() == newTrack.getId()) {
                if (isPlaying()) {
                    return;
                }
            }
        }
        if (mediaPlayer != null && mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            mediaPlayer.reset();
        }
        track = intent.getParcelableExtra("track");
        uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, track.getId());
        mediaPlayer = MediaPlayer.create(this, uri);
        mediaPlayer.start();
    }
    private void actionPause() {
        mediaPlayer.pause();
    }
    private void actionResume() {
        mediaPlayer.start();
    }
    private void actionSeekTimeTo(Intent intent) {
        int progress = intent.getIntExtra("progress", 0);
        mediaPlayer.seekTo(progress);
        if (!isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public boolean isPlaying() {
        if (mediaPlayer != null) {
            return mediaPlayer.isPlaying();
        }
        return false;
    }
    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }
    public String getCurrentPositionReadable() {
        return track.convertToReadable(String.valueOf(getCurrentPosition()));
    }
}