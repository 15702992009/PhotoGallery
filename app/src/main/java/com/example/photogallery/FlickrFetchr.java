package com.example.photogallery;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FlickrFetchr {
    private static final String TAG = "FlickrFetchr";
    public static final String API_KEY = "1525f24402b44f82903d5e07303dfea2";

    List<GalleryItem> fetchItems() {
        Log.i(TAG, "fetchItems: execute");
        List<GalleryItem> items = new ArrayList<>();
        try {
            String url = Uri.parse("https://api.flickr.com/services/rest/")
                    .buildUpon()
                    .appendQueryParameter("method", "flickr.photos.getRecent")
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("extras", "url_s")
                    .build().toString();
            String jsonString = getUrlString(url);
            JSONObject jsonBody = new JSONObject(jsonString);
            //todo
            parseItem(items, jsonBody);

        } catch (IOException | JSONException e) {
            Log.e(TAG, "fetchItems: failed to fetch item", e);
            e.printStackTrace();
        }
        return items;
    }

    private void parseItem(List<GalleryItem> items, JSONObject jsonObject) throws IOException, JSONException {
        JSONObject photosJsonObject = jsonObject.getJSONObject("photos");
        JSONArray jsonArray = photosJsonObject.getJSONArray("photo");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject photoJsonObject = jsonArray.getJSONObject(i);
            GalleryItem galleryItem = new GalleryItem();
            galleryItem.setmId(photoJsonObject.getString("id"));
            galleryItem.setmCaption(photoJsonObject.getString("title"));
            if (!photoJsonObject.has("url_s")) {
                continue;
            }
            galleryItem.setmUrl(photoJsonObject.getString("url_s"));
            items.add(galleryItem);
        }

    }

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = urlConnection.getInputStream();
            if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(urlConnection.getResponseMessage() + ": with " + urlSpec);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            urlConnection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }
}
