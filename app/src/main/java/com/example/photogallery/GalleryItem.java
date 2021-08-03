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

    public GalleryItem() {
    }

    public GalleryItem(String mCaption, String mId, String mUrl) {
        this.mCaption = mCaption;
        this.mId = mId;
        this.mUrl = mUrl;
    }

    public String getmCaption() {

        return mCaption;
    }

    public void setmCaption(String mCaption) {
        this.mCaption = mCaption;
    }

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public String getmUrl() {
        return mUrl;
    }

    public void setmUrl(String mUrl) {
        this.mUrl = mUrl;
    }
}
