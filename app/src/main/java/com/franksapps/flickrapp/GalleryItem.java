package com.franksapps.flickrapp;

/**
 * Created by frankbrosnan on 20/11/2015.
 */
public class GalleryItem {
    private String mCaption;
    private String mId;

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    private String mUrl;



    public String getId() {
        return mId;
    }

    public void setId(String mId) {
        this.mId = mId;
    }


    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String mCaption) {
        this.mCaption = mCaption;
    }

    public String getOwner() {
        return mOwner;
    }

    public void setOwner(String mOwner) {
        this.mOwner = mOwner;
    }

    private String mOwner;



    public String toString() {
        return mCaption;
    }

    public String getPhotoPage(){return "http://www.flickr.com/photos/"+mOwner+"/"+mId;}

}
