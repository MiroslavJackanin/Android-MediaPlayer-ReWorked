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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
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

    View bottomSheet;
    View trackLayoutSheet;

    AppCompatImageView sPlayBtn;
    SeekBar timeSeekBar;
    SeekBar volumeSeekBar;
    TextView sTitle;
    TextView sAlbum;
    TextView sArtist;
    TextView elapsedTimeLabel;
    TextView remainingTimeLabel;

    TextView stlTitle;
    TextView stlArtist;
    ImageView stlPlayBtn;

    MediaPlayerService mediaPlayerService;
    BottomSheetBehavior<View> bottomSheetBehavior;
    Handler handler;
    Intent intent;
    Thread scoutProgressThread;

    ArrayList<Track> tracks = new ArrayList<>();
    Track track;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, MediaPlayerService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerService.LocalBinder localBinder = (MediaPlayerService.LocalBinder) service;
            mediaPlayerService = localBinder.getService();

            initActivity();
            initLiveData();
            initViews();

            scoutProgressThread = new Thread(() -> {
                while (true) {
                    handler.post(() -> elapsedTimeLabel.setText(mediaPlayerService.getCurrentPositionReadable()));
                    SystemClock.sleep(1000);
                    if (mediaPlayerService.getCurrentPositionReadable().equals(track.getDurationReadable())) {
                        resetPlayThrough();
                        scoutProgressThread.interrupt();
                    }
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {}
    };

    public void initActivity() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Library");

        ViewPager2 viewPager2 = findViewById(R.id.view_pager);
        ViewPagerFragmentAdapter viewPagerFragmentAdapter = new ViewPagerFragmentAdapter(
                getSupportFragmentManager(),
                getLifecycle(),
                this);
        viewPager2.setAdapter(viewPagerFragmentAdapter);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
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

        handler = new Handler();
    }

    public void initLiveData() {
        ViewModelProvider viewModelProvider = new ViewModelProvider(this);
        DataViewModel dataViewModel = viewModelProvider.get(DataViewModel.class);
        LiveData<ArrayList<Track>> tracksLiveData = dataViewModel.getTracks();
        tracksLiveData.observe(this, tracks -> {
            if (tracks != null) { this.tracks = tracks; }
        });
    }

    public void initViews() {
        trackLayoutSheet = findViewById(R.id.s_layout);
        bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheet.setOnTouchListener((v, event) -> true);
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

        stlPlayBtn = findViewById(R.id.s_tl_play_btn);
        stlTitle = findViewById(R.id.s_tl_title);
        stlArtist = findViewById(R.id.s_tl_artist);
        stlTitle.setSelected(true);

        sPlayBtn = findViewById(R.id.s_play_btn);
        if (mediaPlayerService.isPlaying()) {
            sPlayBtn.setBackgroundResource(R.drawable.ic_pause);
        } else {
            sPlayBtn.setBackgroundResource(R.drawable.ic_play);
        }
        sTitle = findViewById(R.id.s_title);
        sAlbum = findViewById(R.id.s_album);
        sArtist = findViewById(R.id.s_artist);
        sTitle.setSelected(true);

        elapsedTimeLabel = findViewById(R.id.elapsed_time_label);
        remainingTimeLabel = findViewById(R.id.remaining_time_label);

        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        final int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        final int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        volumeSeekBar = findViewById(R.id.volume_seekBar);
        volumeSeekBar.setMax(maxVolume);
        volumeSeekBar.setProgress(currentVolume);
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public void onTrackClick(int position) {
        track = tracks.get(position);

        Intent intent = new Intent(this, MediaPlayerService.class);
        intent.setAction("PLAY");
        intent.putExtra("track", track);
        startService(intent);

        timeSeekBarInit();

        sPlayBtn.setBackgroundResource(R.drawable.ic_pause);
        stlPlayBtn.setBackgroundResource(R.drawable.ic_pause);

        sTitle.setText(track.getTitle());
        sAlbum.setText(track.getAlbum());
        sArtist.setText(track.getArtist());
        remainingTimeLabel.setText(track.getDurationReadable());

        stlTitle.setText(track.getTitle());
        stlArtist.setText(track.getArtist());
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    public void playBtnClick(View view) {
        if (mediaPlayerService.isPlaying()) {
            actionPause();
        } else {
            actionResume();
        }
    }
    @Override
    public void onBackPressed() {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            this.moveTaskToBack(true);
        }
    }
    public void expandSheet(View view) {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    private void timeSeekBarInit() {
        timeSeekBar = findViewById(R.id.time_seekBar);
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
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                actionResume();
            }
        });

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timeSeekBar.setProgress(mediaPlayerService.getCurrentPosition());
            }
        }, 0, 100);

        if (!scoutProgressThread.isAlive() || scoutProgressThread.isInterrupted()) {
            scoutProgressThread.start();
        }
    }

    private void resetPlayThrough() {
        actionSeekTimeTo(0);
        sPlayBtn.setBackgroundResource(R.drawable.ic_play);
        stlPlayBtn.setBackgroundResource(R.drawable.ic_play);
    }

    private void actionPause() {
        intent = new Intent(this, MediaPlayerService.class);
        intent.setAction("PAUSE");
        startService(intent);
        sPlayBtn.setBackgroundResource(R.drawable.ic_play);
        stlPlayBtn.setBackgroundResource(R.drawable.ic_play);
    }
    private void actionResume() {
        intent = new Intent(this, MediaPlayerService.class);
        intent.setAction("RESUME");
        startService(intent);
        sPlayBtn.setBackgroundResource(R.drawable.ic_pause);
        stlPlayBtn.setBackgroundResource(R.drawable.ic_pause);
    }
    private void actionSeekTimeTo(int progress) {
        intent = new Intent(this, MediaPlayerService.class);
        intent.setAction("SEEK_TIME_TO");
        intent.putExtra("progress", progress);
        startService(intent);
    }
}