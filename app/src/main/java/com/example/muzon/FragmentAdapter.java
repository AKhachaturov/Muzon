package com.example.muzon;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class FragmentAdapter extends FragmentStateAdapter{

    public static final int STATE_SONGS = 1;
    public static final int STATE_PLAYLISTS = 2;

    public FragmentAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return position == STATE_SONGS ? SongsFragment.newInstance() : position == STATE_PLAYLISTS ? new FragmentMoke() : new FragmentMoke();
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
