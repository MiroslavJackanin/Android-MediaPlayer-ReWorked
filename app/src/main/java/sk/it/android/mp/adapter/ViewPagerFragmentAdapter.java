package sk.it.android.mp.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

import sk.it.android.mp.fragment.AlbumsFragment;
import sk.it.android.mp.fragment.TracksFragment;
import sk.it.android.mp.util.Track;

public class ViewPagerFragmentAdapter extends FragmentStateAdapter {

    private final ArrayList<Fragment> fragments;

    public ViewPagerFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
        fragments = new ArrayList<>();
        fragments.add(new TracksFragment());
        fragments.add(new AlbumsFragment());
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }
}
