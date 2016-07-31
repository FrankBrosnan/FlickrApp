package com.franksapps.flickrapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by frankbrosnan on 24/11/2015.
 */
public class ThumbnailDownloader<Token> extends HandlerThread {
    private static String TAG="ThumbnailDownloader";
    private final static int MESSAGE_DOWNLOAD = 0;

    Handler mHandler;

    Map<Token,String> requestMap = Collections.synchronizedMap(new HashMap<Token,String>());

    Handler mResponseHandler;
    Listener<Token> mListener;

    public interface Listener<Token> {
        void onThumbnailDownloaded(Token token, Bitmap thumbnail);
    }

    public void setListener(Listener<Token> listener){
        mListener = listener;
    }


    public ThumbnailDownloader(Handler responseHandler){
        super(TAG);
        mResponseHandler = responseHandler;
    }

    @Override
    protected void onLooperPrepared(){
        mHandler = new Handler () {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD){
                    Token token = (Token)msg.obj;
                    handleRequest(token);
                }
            }
        };
    }

    public void queueThumbnail(Token token,String url){
        Log.i(TAG,"Got an URL "+url);
        requestMap.put(token,url);
        mHandler.obtainMessage(MESSAGE_DOWNLOAD,token).sendToTarget();
    }

    private void handleRequest(final Token token){
        try{
            final String url = requestMap.get(token);
            if (url == null){
                return;
            }
            byte[] bitmapBytes = new FlickrFetcher().getBytes(url);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes,0,bitmapBytes.length);
            Log.i(TAG,"Bitmap Created");

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (requestMap.get(token) != url){
                        return;
                    }
                    requestMap.remove(token);
                    mListener.onThumbnailDownloaded(token,bitmap);
                }
            });


        } catch (IOException ioe){
            Log.e(TAG,"Error downloading image ",ioe);
        }
    }

    public void clearQueue(){
        mHandler.removeMessages(MESSAGE_DOWNLOAD);
        requestMap.clear();
    }

}
