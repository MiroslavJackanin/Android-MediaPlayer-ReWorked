package sk.it.android.mp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import sk.it.android.mp.R;
import sk.it.android.mp.listener.OnTrackClickListener;
import sk.it.android.mp.util.Track;

public class TracksRecyclerViewAdapter extends RecyclerView.Adapter<TracksRecyclerViewAdapter.ViewHolder> {

    private final OnTrackClickListener onTrackClickListener;
    private final List<Track> tracks;

    public TracksRecyclerViewAdapter(ArrayList<Track> tracks, OnTrackClickListener onTrackClickListener) {
        this.onTrackClickListener = onTrackClickListener;
        this.tracks = tracks;
    }

    @NonNull
    @Override
    public TracksRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.track_layout, parent, false);
        return new ViewHolder(view, onTrackClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull TracksRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.bind(tracks.get(position));
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        OnTrackClickListener onTrackClickListener;
        private final TextView title, artist, duration;

        public ViewHolder(@NonNull View itemView, OnTrackClickListener onTrackClickListener) {
            super(itemView);
            this.onTrackClickListener = onTrackClickListener;
            itemView.setOnClickListener(this);

            title = itemView.findViewById(R.id.textViewTitle);
            artist = itemView.findViewById(R.id.textViewArtist);
            duration = itemView.findViewById(R.id.textViewDuration);

            title.setSelected(true);
        }

        public void bind(Track track) {
            this.title.setText(track.getTitle());
            this.artist.setText(track.getArtist());
            this.duration.setText(track.getDurationReadable());
        }

        @Override
        public void onClick(View v) {
            onTrackClickListener.onTrackClick(getAdapterPosition());
        }
    }
}
