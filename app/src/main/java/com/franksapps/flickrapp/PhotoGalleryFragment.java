package com.franksapps.flickrapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SearchView;

import java.util.ArrayList;

/**
 * Created by frankbrosnan on 18/11/2015.
 */
public class PhotoGalleryFragment extends VisibleFragment {

    private static final String TAG="PhotGalleryFragment";

    private GridView mGridView;
    ArrayList<GalleryItem> mItems;
    ThumbnailDownloader<ImageView> mThumbnailThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        setHasOptionsMenu(true);
        updateItems();

        //Intent i = new Intent(getActivity(),PollService.class);
        //getActivity().startService(i);

        //PollService.setServiceAlarm(getActivity(),true);

        mThumbnailThread = new ThumbnailDownloader<ImageView>(new Handler());


        mThumbnailThread.setListener(
                new ThumbnailDownloader.Listener<ImageView>(){

                    @Override
                    public void onThumbnailDownloaded(ImageView imageView, Bitmap thumbnail) {
                        if (isVisible()){
                            imageView.setImageBitmap(thumbnail);
                        }
                    }
                }

        );


        mThumbnailThread.start();
        mThumbnailThread.getLooper();
        Log.i(TAG,"Background Thread Started");
    }

    public void updateItems() {
        new FetchItemsTask().execute();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery,container,false);
        mGridView= (GridView)v.findViewById(R.id.gridView);
        setUpAdapter();

        mGridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GalleryItem item = mItems.get(position);
                Uri photoPageUri = Uri.parse(item.getPhotoPage());
                Intent i = new Intent(getActivity(),PhotoPageActivity.class);
                i.setData(photoPageUri);
                startActivity(i);
            }
        });

        return v;
    }

    void setUpAdapter(){
        if ((getActivity() == null) || (mGridView == null)){
            return;
        }
        if (mItems != null){
            //mGridView.setAdapter(new ArrayAdapter<GalleryItem>(getActivity(),
            //                                                  android.R.layout.simple_gallery_item,
            //                                                  mItems
            //        ));

            mGridView.setAdapter(new GalleryItemAdapter(mItems));

        } else {
            mGridView.setAdapter(null);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mThumbnailThread.quit();
        Log.i(TAG,"Background thread destroyed");
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        mThumbnailThread.clearQueue();
    }

    @Override
    @TargetApi(11)
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        super.onCreateOptionsMenu(menu,inflater);
        inflater.inflate(R.menu.menu_photo_gallery,menu);

        //Honeycomb search view
        if ((Build.VERSION.SDK_INT) >= Build.VERSION_CODES.HONEYCOMB){
            //Pull out the search view
            MenuItem searchItem = menu.findItem(R.id.menu_item_search);
            SearchView searchView = (SearchView)searchItem.getActionView();
            SearchManager searchManager = (SearchManager)getActivity().getSystemService(Context.SEARCH_SERVICE);
            ComponentName name = getActivity().getComponentName();
            SearchableInfo searchInfo = searchManager.getSearchableInfo(name);
            searchView.setSearchableInfo(searchInfo);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.menu_item_search:
                getActivity().onSearchRequested();
                return true;
            case R.id.menu_item_clear:
                PreferenceManager.getDefaultSharedPreferences(getActivity())
                                 .edit()
                                 .putString(FlickrFetcher.PREF_SEARCH_QUERY,null)
                                 .commit();
                updateItems();

                return true;
            case R.id.menu_item_toggle_polling:
                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(),shouldStartAlarm);

                //For Honeycomb need to set the action bar to refresh.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
                    getActivity().invalidateOptionsMenu();
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu){
        super.onPrepareOptionsMenu(menu);
        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
        if (PollService.isServiceAlarmOn(getActivity())){
            toggleItem.setTitle(R.string.stop_polling);
        } else {
            toggleItem.setTitle(R.string.start_polling);
        }

    }

    private class FetchItemsTask extends AsyncTask<Void,Void,ArrayList<GalleryItem>> {

        @Override
        protected ArrayList<GalleryItem> doInBackground(Void... params){
//            try{
//                String result = new FlickrFetcher().getString("http://www.google.com");
//                Log.i(TAG,"Fetched Contents of URL:"+result);
//            } catch (IOException e){
//                Log.e(TAG,"Failed to fetch URL",e);
//            }
            Activity activity = getActivity();
            if (activity == null){
                return new ArrayList<GalleryItem>();
            }
            String query = PreferenceManager.getDefaultSharedPreferences(activity)
                    .getString(FlickrFetcher.PREF_SEARCH_QUERY,null);


            if (query != null){
                return (new FlickrFetcher().search(query));
            }
            else {
                return (new FlickrFetcher().fetchItems());
            }
        }

        @Override
        protected void onPostExecute(ArrayList<GalleryItem> items){
            mItems = items;
            setUpAdapter();
        }
    }

    private class GalleryItemAdapter extends ArrayAdapter<GalleryItem>
    {
        public GalleryItemAdapter(ArrayList<GalleryItem> items){
            super(getActivity(),0,items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            if (convertView == null){
                convertView = getActivity().getLayoutInflater().inflate(R.layout.gallery_item,
                        parent, false);
            }
            ImageView imageView = (ImageView)convertView.findViewById(R.id.gallery_item_imageView);
            imageView.setImageResource(R.drawable.bill_up_close);

            GalleryItem galleryItem = getItem(position);
            mThumbnailThread.queueThumbnail(imageView, galleryItem.getUrl());

            return convertView;
        }
    }
}
