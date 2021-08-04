package com.example.photogallery;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * todo 利用Gson解析json数据绑定数据，OKHttp的使用
 * 25.10 挑战练习:Gson
 * 25.11 挑战练习:分页
 * 25.12 挑战练习:动态调整网格列
 */
public class FlickrFetchr {
    private static final String TAG = "FlickrFetchr";
    public static final String API_KEY = "1525f24402b44f82903d5e07303dfea2";
    public static final String FETCH_RECENTS_METHOD = "flickr.photos.getRecent";
    public static final String SEARCH_METHOD = "flickr.photos.search";
    public static final Uri ENDPOINT = Uri
            .parse("https://api.flickr.com/services/rest/")
            .buildUpon()
            .appendQueryParameter("api_key", API_KEY)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", "url_s")
            .build();

    private String buildUrl(String method, String query) {
        Uri.Builder uriBuilder = ENDPOINT.buildUpon().appendQueryParameter("method", method);
        if (method.equals(SEARCH_METHOD)) {
            uriBuilder.appendQueryParameter("text", query);
        }
        return uriBuilder.build().toString();
    }

     List<GalleryItem> fetchRecentPhotos() {
        String url = buildUrl(FETCH_RECENTS_METHOD, null);
        return downloadGalleryItems(url);
    }

     List<GalleryItem> searchPhotos(String query) {
        String url = buildUrl(SEARCH_METHOD, query);
         Log.i(TAG, "searchPhotos: +"+url);
        return downloadGalleryItems(url);
    }

    //    List<GalleryItem> fetchItems() {
    private List<GalleryItem> downloadGalleryItems(String url) {
        List<GalleryItem> items = new ArrayList<>();
        try {
/*            String url = Uri.parse("https://api.flickr.com/services/rest/")
                    .buildUpon()
                    .appendQueryParameter("method", "flickr.photos.getRecent")
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("extras", "url_s")
                    .build().toString();*/
            String jsonString = getUrlString(url);
            /*Gson gson = new Gson();
            List<GalleryItem> fromJson = gson.fromJson(jsonString, new TypeToken<List<GalleryItem>>() {
            }.getType());
            for (GalleryItem galleryItem : fromJson) {
                Log.d("GalleryItem", "fetchItems: "+galleryItem.toString());
            }*/
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
            galleryItem.setId(photoJsonObject.getString("id"));
            galleryItem.setCaption(photoJsonObject.getString("title"));
            if (!photoJsonObject.has("url_s")) {
                continue;
            }
            galleryItem.setUrl(photoJsonObject.getString("url_s"));
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
