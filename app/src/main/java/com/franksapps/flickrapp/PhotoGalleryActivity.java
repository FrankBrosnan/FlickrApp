package com.franksapps.flickrapp;

import android.app.SearchManager;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;

public class PhotoGalleryActivity extends SingleFragmentActivity {

    private static final String TAG = "PhotoGalleryActivity";

    @Override
    protected Fragment createFragment() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onNewIntent(Intent intent){
        PhotoGalleryFragment fragment =
                (PhotoGalleryFragment)getSupportFragmentManager()
                                      .findFragmentById(R.id.fragment_container);

        if (Intent.ACTION_SEARCH.equals(intent.getAction())){
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.i(TAG,"processing search for query: "+query);
            fragment.updateItems();

            PreferenceManager.getDefaultSharedPreferences(this)
                             .edit()
                             .putString(FlickrFetcher.PREF_SEARCH_QUERY,query)
                             .commit();

        }

    }
}
