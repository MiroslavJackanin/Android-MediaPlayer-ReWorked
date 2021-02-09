package sk.it.android.mp.data;

import android.app.Application;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import java.util.ArrayList;

import sk.it.android.mp.adapter.TracksRecyclerViewAdapter;
import sk.it.android.mp.util.Track;

public class DataViewModel extends AndroidViewModel implements OnTracksLoadListener {

    OnTracksLoadListener onTracksLoadParentListener;
    ArrayList<Track> tracks;

    public DataViewModel(@NonNull Application application, OnTracksLoadListener onTracksLoadParentListener) {
        super(application);
        this.onTracksLoadParentListener = onTracksLoadParentListener;
        tracks = new ArrayList<>();
        new LoadTracksTask(this).execute(application);
    }

    public ArrayList<Track> getTracks() {
        return tracks;
    }

    @Override
    public void OnTracksLoaded(ArrayList<Track> tracks) {
        Log.i("DEUBG", "DataViewModel: OnTracksLoaded");
        this.tracks = tracks;
        onTracksLoadParentListener.OnTracksLoaded(tracks);
    }

    public static class LoadTracksTask extends AsyncTask<Application, Void, ArrayList<Track>> {

        private final OnTracksLoadListener listener;

        public LoadTracksTask(OnTracksLoadListener listener) {
            this.listener = listener;
        }

        @Override
        protected ArrayList<Track> doInBackground(Application... applications) {

            Application application = applications[0];
            ArrayList<Track> tracks = new ArrayList<>();

            ContentResolver contentResolver = application.getApplicationContext().getContentResolver();
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

        @Override
        protected void onPostExecute(ArrayList<Track> tracks) {
            super.onPostExecute(tracks);
            listener.OnTracksLoaded(tracks);
        }
    }
}
