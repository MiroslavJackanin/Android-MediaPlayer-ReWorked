package sk.it.android.mp.listener;

import java.util.ArrayList;

import sk.it.android.mp.util.Track;

public interface OnTracksLoadListener {
    void OnTracksLoaded(ArrayList<Track> tracks);
}
