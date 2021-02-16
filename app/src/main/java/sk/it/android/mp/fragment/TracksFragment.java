package sk.it.android.mp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import sk.it.android.mp.R;
import sk.it.android.mp.adapter.TracksRecyclerViewAdapter;
import sk.it.android.mp.data.DataViewModel;
import sk.it.android.mp.listener.OnTrackClickListener;
import sk.it.android.mp.util.Track;

public class TracksFragment extends Fragment {

    RecyclerView tracksRecyclerView;
    ViewModelProvider viewModelProvider;
    DataViewModel dataViewModel;

    LiveData<ArrayList<Track>> tracksLiveData;
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
        adapter = new TracksRecyclerViewAdapter(onTrackClickListener);
        tracksRecyclerView.setAdapter(adapter);
        tracksRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        viewModelProvider = new ViewModelProvider(this);
        dataViewModel = viewModelProvider.get(DataViewModel.class);

        tracksLiveData = dataViewModel.getTracks();
        tracksLiveData.observe(getViewLifecycleOwner(), tracks -> {
            if (tracks != null) {
                adapter.setTracks(tracks);
            }
        });
    }
}