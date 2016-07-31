package com.franksapps.flickrapp;

import android.support.v4.app.Fragment;

/**
 * Created by frankbrosnan on 16/07/2016.
 */
public class PhotoPageActivity extends SingleFragmentActivity {

    @Override
    public Fragment createFragment(){
        return new PhotoPageFragment();
    }
}
