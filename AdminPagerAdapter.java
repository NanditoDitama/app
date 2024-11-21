package com.example.laporan2;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class AdminPagerAdapter extends FragmentStateAdapter {
    public AdminPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new ProcessedDataFragment();
            case 1:
                return new NewDataFragment();
            case 2:
                return new EditedDataFragment();
            default:
                return new ProcessedDataFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Tiga fragment: Data Baru, Data Edit, Data Diproses
    }
}