package com.syp4.noctua.utilidades;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class AdaptadorFragmentos extends FragmentPagerAdapter
{
    List<Fragment> fragments;

    public AdaptadorFragmentos(FragmentManager fm) {
        super(fm);
        this.fragments = new ArrayList<Fragment>();
    }

    public AdaptadorFragmentos(FragmentManager fm, List<Fragment> fragmentos) {
        super(fm);
        this.fragments = fragmentos;
    }

    public void addFragment(Fragment fragment) {
        this.fragments.add(fragment);
    }

    @Override
    public Fragment getItem(int i) {
        return fragments.get(i);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
