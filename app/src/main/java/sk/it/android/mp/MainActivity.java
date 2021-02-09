package sk.it.android.mp;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Objects;

import sk.it.android.mp.adapter.ViewPagerFragmentAdapter;
import sk.it.android.mp.data.DataViewModel;
import sk.it.android.mp.service.MediaPlayerService;
import sk.it.android.mp.util.Track;

public class MainActivity extends AppCompatActivity {

    ViewPager2 viewPager2;
    TabLayout tabLayout;
    View bottomSheet;

    ViewPagerFragmentAdapter adapter;
    MediaPlayerService mediaPlayerService;
    BottomSheetBehavior<View> bottomSheetBehavior;

    boolean isMediaPlayerServiceBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, MediaPlayerService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        initToolbar();
        initViewPagerAndTabLayout();

        bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerService.LocalBinder localBinder = (MediaPlayerService.LocalBinder) service;
            mediaPlayerService = localBinder.getService();
            isMediaPlayerServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isMediaPlayerServiceBound = false;
        }
    };

    private void initViewPagerAndTabLayout() {
        viewPager2 = findViewById(R.id.view_pager);
        adapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(), getLifecycle());
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
}