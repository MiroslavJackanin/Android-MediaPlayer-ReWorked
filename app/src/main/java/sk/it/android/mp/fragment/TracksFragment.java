package sk.it.android.mp.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Objects;

import sk.it.android.mp.R;
import sk.it.android.mp.listener.OnTrackClickListener;
import sk.it.android.mp.adapter.TracksRecyclerViewAdapter;
import sk.it.android.mp.data.DataViewModel;
import sk.it.android.mp.data.DataViewModelFactory;
import sk.it.android.mp.listener.OnTracksLoadListener;
import sk.it.android.mp.util.Track;

public class TracksFragment extends Fragment implements OnTracksLoadListener {

    RecyclerView tracksRecyclerView;
    DataViewModel dataViewModel;

    ArrayList<Track> tracks;
    TracksRecyclerViewAdapter adapter;

    OnTrackClickListener onTrackClickListener;

    public TracksFragment(OnTrackClickListener onTrackClickListener) {
        this.onTrackClickListener = onTrackClickListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tracks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tracksRecyclerView = view.findViewById(R.id.tracksRecyclerView);
        tracksRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        dataViewModel = new ViewModelProvider(this, new DataViewModelFactory(Objects.requireNonNull(getActivity()).getApplication(), this)).get(DataViewModel.class);
        tracks = dataViewModel.getTracks();

        adapter = new TracksRecyclerViewAdapter(tracks, onTrackClickListener);
        tracksRecyclerView.setAdapter(adapter);
    }

    @Override
    public void OnTracksLoaded(ArrayList<Track> tracks) {
        this.tracks = tracks;
        adapter = new TracksRecyclerViewAdapter(tracks, onTrackClickListener);
        tracksRecyclerView.setAdapter(adapter);
    }
}