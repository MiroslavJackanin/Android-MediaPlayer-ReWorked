package sk.it.android.mp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import sk.it.android.mp.adapter.ViewPagerFragmentAdapter;
import sk.it.android.mp.data.DataViewModel;
import sk.it.android.mp.listener.OnTrackClickListener;
import sk.it.android.mp.service.MediaPlayerService;
import sk.it.android.mp.util.Track;

public class MainActivity extends AppCompatActivity implements OnTrackClickListener {

    ViewPager2 viewPager2;
    TabLayout tabLayout;
    View bottomSheet;
    View trackLayoutSheet;

    AppCompatImageView playBtn;
    SeekBar timeSeekBar;
    SeekBar volumeSeekBar;

    TextView titleTextView;
    TextView albumTextView;
    TextView artistTextView;

    TextView elapsedTimeLabel;
    TextView remainingTimeLabel;

    ViewPagerFragmentAdapter adapter;
    MediaPlayerService mediaPlayerService;
    BottomSheetBehavior<View> bottomSheetBehavior;
    AudioManager audioManager;
    Handler handler;
    Intent intent;

    ArrayList<Track> tracks = new ArrayList<>();
    Track track;
    boolean isMediaPlayerServiceBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, MediaPlayerService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        initToolbar();
        initViewPagerAndTabLayout();

        trackLayoutSheet = findViewById(R.id.track_layout_sheet);
        bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {}

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                trackLayoutSheet.setAlpha((1-slideOffset));
            }
        });

        ViewModelProvider viewModelProvider = new ViewModelProvider(this);
        DataViewModel dataViewModel = viewModelProvider.get(DataViewModel.class);

        LiveData<ArrayList<Track>> tracksLiveData = dataViewModel.getTracks();
        tracksLiveData.observe(this, tracks -> {
            if (tracks != null) {
                this.tracks = tracks;
            }
        });
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerService.LocalBinder localBinder = (MediaPlayerService.LocalBinder) service;
            mediaPlayerService = localBinder.getService();
            isMediaPlayerServiceBound = true;

            handler = new Handler();

            playBtnInit();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isMediaPlayerServiceBound = false;
        }
    };

    Thread scoutProgress = new Thread(new Runnable() {
        public void run() {
            while (true) {
                handler.post(() -> elapsedTimeLabel.setText(mediaPlayerService.getCurrentPositionReadable()));
                SystemClock.sleep(1000);
            }
        }
    });

    private void initViewPagerAndTabLayout() {
        viewPager2 = findViewById(R.id.view_pager);
        adapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(), getLifecycle(), this);
        viewPager2.setAdapter(adapter);

        tabLayout = findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("TRACKS");
                    break;
                case 1:
                    tab.setText("ALBUMS");
                    break;
            }
        }).attach();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Library");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            finish();
        }
    }

    @Override
    public void onTrackClick(int position) {
        track = tracks.get(position);

        Intent intent = new Intent(this, MediaPlayerService.class);
        intent.setAction("PLAY");
        intent.putExtra("track", track);
        startService(intent);

        playBtn.setBackgroundResource(R.drawable.ic_pause);
        initBottomSheetTrackLayout();
        initBottomSheet();

        timeSeekBarInit();
        volumeSeekBarInit();

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timeSeekBar.setProgress(mediaPlayerService.getCurrentPosition());
            }
        }, 0, 100);

        if (!scoutProgress.isAlive()) {
            scoutProgress.start();
        }
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void initBottomSheetTrackLayout() {
        TextView bottomSheetTrackLayoutTitle = findViewById(R.id.textViewTitleSheet);
        TextView bottomSheetTrackLayoutArtist = findViewById(R.id.textViewArtistSheet);
        bottomSheetTrackLayoutTitle.setSelected(true);

        bottomSheetTrackLayoutTitle.setText(track.getTitle());
        bottomSheetTrackLayoutArtist.setText(track.getArtist());
    }

    private void initBottomSheet() {
        titleTextView = findViewById(R.id.title);
        albumTextView = findViewById(R.id.album);
        artistTextView = findViewById(R.id.artist);

        titleTextView.setSelected(true);

        titleTextView.setText(track.getTitle());
        albumTextView.setText(track.getAlbum());
        artistTextView.setText(track.getArtist());

        elapsedTimeLabel = findViewById(R.id.elapsedTimeLabel);
        remainingTimeLabel = findViewById(R.id.remainingTimeLabel);
        remainingTimeLabel.setText(track.getDurationReadable());
    }

    private void playBtnInit() {
        playBtn = findViewById(R.id.play_btn);
        if (mediaPlayerService.isPlaying()) {
            playBtn.setBackgroundResource(R.drawable.ic_pause);
        } else {
            playBtn.setBackgroundResource(R.drawable.ic_play);
        }
    }
    public void playBtnClick(View view) {
        if (mediaPlayerService.isPlaying()) {
            playBtn.setBackgroundResource(R.drawable.ic_play);
            actionPause();
        } else {
            playBtn.setBackgroundResource(R.drawable.ic_pause);
            actionResume();
        }
    }

    private void timeSeekBarInit() {
        timeSeekBar = findViewById(R.id.seek_bar);
        timeSeekBar.setMax(Integer.parseInt(track.getDuration()));
        timeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) actionSeekTimeTo(progress);
                timeSeekBar.setProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                actionPause();
                playBtn.setBackgroundResource(R.drawable.ic_play);
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                actionResume();
                playBtn.setBackgroundResource(R.drawable.ic_pause);
            }
        });
    }

    private void volumeSeekBarInit() {
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        final int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        final int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        volumeSeekBar = findViewById(R.id.volume_seek_bar);
        volumeSeekBar.setMax(maxVolume);
        volumeSeekBar.setProgress(currentVolume);
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void actionPause() {
        intent = new Intent(this, MediaPlayerService.class);
        intent.setAction("PAUSE");
        startService(intent);

        playBtn.setBackgroundResource(R.drawable.ic_play);
    }
    private void actionResume() {
        intent = new Intent(this, MediaPlayerService.class);
        intent.setAction("RESUME");
        startService(intent);

        playBtn.setBackgroundResource(R.drawable.ic_pause);
    }
    private void actionSeekTimeTo(int progress) {
        intent = new Intent(this, MediaPlayerService.class);
        intent.setAction("SEEK_TIME_TO");
        intent.putExtra("progress", progress);
        startService(intent);
    }
}