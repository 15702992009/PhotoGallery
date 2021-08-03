package com.example.photogallery;

public class GalleryItem {
    private String mCaption;
    private String mId;
    private String mUrl;

    @Override
    public String toString() {
        return "GalleryItem{" +
                "mCaption='" + mCaption + '\'' +
                ", mId='" + mId + '\'' +
                '}';
    }

    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String caption) {
        mCaption = caption;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public GalleryItem() {
    }

    public GalleryItem(String mCaption, String mId, String mUrl) {
        this.mCaption = mCaption;
        this.mId = mId;
        this.mUrl = mUrl;
    }

}
