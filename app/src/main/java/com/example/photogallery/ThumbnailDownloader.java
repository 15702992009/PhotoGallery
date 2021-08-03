package com.example.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * todo
 * @param <T> 注意, ThumbnailDownloader 类使用了 <T> 泛型参数。 ThumbnailDownloader 类的使用者(这
 *            里指 PhotoGalleryFragment), 需要使用某些对象来识别每次下载,并确定该使用已下载图片
 *            更新哪个UI元素。有了泛型参数,实施起来方便了很多。
 */
public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";
    private Boolean mHasQuit = false;
    private static final int MESSAGE_DOWNLOAD = 0;
    private Handler mRequestHandler;
    private Handler mResponseHandler;
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;;

    public void setmThumbnailDownloadListener(ThumbnailDownloadListener<T> mThumbnailDownloadListener) {
        this.mThumbnailDownloadListener = mThumbnailDownloadListener;
    }

    public interface ThumbnailDownloadListener<T> {
        void onThumbnailDownloaded(T target,Bitmap thumbnail);
    }

    private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();

    public ThumbnailDownloader(Handler mResponseHandler) {
        super(TAG);
        this.mResponseHandler = mResponseHandler;
        Log.i(TAG, "ThumbnailDownloader: construct mRequestHandler  "+this.mResponseHandler);
    }

    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }

    /**
     * queueThumbnail() 方法需要一个 T 类型对象(标识具体哪次下载)和一个 String 参数(URL
     * 下载链接) 同时,它也是 PhotoAdapter 在其 onBindViewHolder(...) 实现方法中要调用的方法。
     * ?
     *
     * @param target
     * @param url
     */
    public void queueThumbnail(T target, String url) {
        Log.i(TAG, "queueThumbnail:  got a URL: " + url);
        if (url == null) {
            mRequestMap.remove(target);
        } else {
            mRequestMap.put(target, url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target)
                    .sendToTarget();
        }
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    T target = (T) msg.obj;
                    Log.i(TAG, "handleMessage: got a request for URL " + mRequestMap.get(target));
                    handleRequest(target);
                }
            }
        };
    }

    /**
     * handleRequest() 方 法 是 下 载 执 行 的 地 方 。
     *
     * @param target
     */
    private void handleRequest(final T target) {
        try {
            final String url = mRequestMap.get(target);
            if (url == null) {
                return;
            }
            byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
            Log.i(TAG, "handleRequest: bitmap generated");
            /**
             * todo
             * 那么上述代码有什么作用呢?首先,它再次检查 requestMap 。这很有必要,因为 RecyclerView
             * 会循环使用其视图。在 ThumbnailDownloader 下载完成 Bitmap 之后, RecyclerView 可能循环使
             * 用了 PhotoHolder 并相应请求了一个不同的URL。该检查可保证每个 PhotoHolder 都能获取到正
             * 确的图片,即使中间发生了其他请求也无妨。
             */
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!Objects.equals(mRequestMap.get(target), url) ||mHasQuit){
                        return;
                    }
                    mRequestMap.remove(target);
                    mThumbnailDownloadListener.onThumbnailDownloaded(target,bitmap);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void clearQueue() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
        mRequestMap.clear();
    }

}
