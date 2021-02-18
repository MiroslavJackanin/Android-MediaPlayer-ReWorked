package sk.it.android.mp.data;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import sk.it.android.mp.util.Track;

public class DataRepository {

    private final Context context;

    public DataRepository(Context context) {
        this.context = context;
    }

    public LiveData<ArrayList<Track>> getLiveData() {
        return new LiveData<ArrayList<Track>>() {
            private ContentObserver observer;

            @Override
            protected void onActive() {
                observer = new ContentObserver(null) {

                    @Override
                    public void onChange(boolean selfChange) {
                        postValue(loadData());
                    }
                };
                context.getContentResolver().registerContentObserver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, observer);
                observer.onChange(true);
            }

            @Override
            protected void onInactive() {
                context.getContentResolver().unregisterContentObserver(observer);
            }
        };
    }

    public ArrayList<Track> loadData() {
        ArrayList<Track> tracks = new ArrayList<>();

        ContentResolver contentResolver = context.getApplicationContext().getContentResolver();
        Uri trackUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor trackCursor = contentResolver.query(trackUri, null, null, null, null);

        long id;
        String title, album, artist, duration;
        Track track;

        if (trackCursor != null && trackCursor.moveToFirst()) {
            do {
                id = trackCursor.getLong(trackCursor.getColumnIndex(MediaStore.Audio.Media._ID));
                title = trackCursor.getString(trackCursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                album = trackCursor.getString(trackCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                artist = trackCursor.getString(trackCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                duration = trackCursor.getString(trackCursor.getColumnIndex(MediaStore.Audio.Media.DURATION));

                track = new Track(id, title, album, artist, duration);
                tracks.add(track);
            } while (trackCursor.moveToNext());
            trackCursor.close();
        }
        return tracks;
    }
}