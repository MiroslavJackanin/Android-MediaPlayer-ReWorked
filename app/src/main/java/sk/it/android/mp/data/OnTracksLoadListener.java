package sk.it.android.mp.data;

import java.util.ArrayList;

import sk.it.android.mp.util.Track;

public interface OnTracksLoadListener {
    void OnTracksLoaded(ArrayList<Track> tracks);
}
