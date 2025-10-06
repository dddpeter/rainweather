package com.dddpeter.app.rainweather.view;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TabsAdapter extends FragmentStateAdapter {
    int mNumOfTabs;

    private TabsAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    public TabsAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, int noofTabs) {
        this(fragmentManager, lifecycle);
        mNumOfTabs = noofTabs;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:
                return new H24Fragment();
            case 2:
                return new MainFragment();
            case 3:
                return new AboutFragment();
            case 0:
            default:
                return new TodayFragment();
        }
    }

    @Override
    public int getItemCount() {
        return mNumOfTabs;
    }
}
