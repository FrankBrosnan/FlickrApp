package com.franksapps.flickrapp;

import android.net.Uri;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by frankbrosnan on 18/11/2015.
 */
public class FlickrFetcher {

    public static final String TAG="FlickrFetcher";

    private static final String ENDPOINT="https://api.flickr.com/services/rest";
    private static final String API_KEY="7b2f808d8228f0d9e0b9b03847a49201";

    private static final String METHOD_GET_RECENT="flickr.photos.getRecent";
    private static final String METHOD_SEARCH = "flickr.photos.search";
    private static final String PARAM_EXTRAS="extras";
    private static final String PARAM_TEXT="text";
    private static final String EXTRA_SMALL_URI="url_s";

    private static final String XML_PHOTO="photo";
    public static final String PREF_SEARCH_QUERY="searchQuery";
    public static final String PREF_LAST_RESULT_ID="lastResultId";

    byte[] getBytes(String urlSpec)
        throws IOException
    {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        try{
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                return null;
            }
            int bytesRead = 0;
            byte[] byteBuffer = new byte[1024];
            while ((bytesRead = in.read(byteBuffer)) > 0){
                out.write(byteBuffer,0,bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getURL(String urlSpec)
        throws IOException
    {
        return new String(getBytes(urlSpec));
    }

    public ArrayList<GalleryItem> downloadGalleryItems(String url){
        ArrayList<GalleryItem> items = new ArrayList<GalleryItem>();
        try{

            String xmlString = getURL(url);
            Log.i(TAG,"Receieved XML: "+xmlString);

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xmlString));
            parseItem(items,parser);



        }catch (IOException ioe) {
            Log.e(TAG,"Failed to fetch recent photos",ioe);
        }
        catch (XmlPullParserException xppe){
            Log.e(TAG,"Failed to parse recent photos xml",xppe);
        }
        return items;
    }

    //REST Request Methods.
    public ArrayList<GalleryItem> fetchItems(){
        String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method",METHOD_GET_RECENT)
                .appendQueryParameter("api_key",API_KEY)
                .appendQueryParameter(PARAM_EXTRAS,EXTRA_SMALL_URI)
                .build().toString();
        return downloadGalleryItems(url);
    }

    public ArrayList<GalleryItem> search(String query){
        String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method",METHOD_SEARCH)
                .appendQueryParameter("api_key",API_KEY)
                .appendQueryParameter(PARAM_EXTRAS,EXTRA_SMALL_URI)
                .appendQueryParameter(PARAM_TEXT,query)
                .build().toString();
        return downloadGalleryItems(url);


    }

    void parseItem(ArrayList<GalleryItem> items, XmlPullParser parser)
        throws XmlPullParserException, IOException
    {
        int eventType = parser.next();
        while (eventType != XmlPullParser.END_DOCUMENT){
            if (eventType == XmlPullParser.START_TAG && XML_PHOTO.equals(parser.getName())){
                String id = parser.getAttributeValue(null,"id");
                String caption = parser.getAttributeValue(null,"title");
                String smallUrl= parser.getAttributeValue(null,EXTRA_SMALL_URI);
                String owner = parser.getAttributeValue(null,"owner");

                GalleryItem item = new GalleryItem();
                item.setCaption(caption);
                item.setId(id);
                item.setUrl(smallUrl);
                item.setOwner(owner);
                items.add(item);
            }
            eventType = parser.next();
        }

    }

}
