package com.example.photogallery;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * 写错了，小了， 格局小了
 * fragment`s bind view through fragmentManager.add method
 * e.g
 * fm.beginTransaction()
 * .add(R.id.fragment_container, fragment)
 * .commit();
 * todo 25.8
 */
public class PhotoGalleryFragment extends Fragment {
    private static final String TAG = "PhotoGalleryFragment";
    private RecyclerView recyclerView;

    private List<GalleryItem> mItem = new ArrayList<>();

    ViewAdapter viewAdapter;
    ThumbnailDownloader<ViewAdapter.ViewHolder> mThumbnailDownloader;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: execute");
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new FetchItemsTask().execute();
        /**
         * 前面说过, Handler 默认与当前线程的 Looper 相关联。这个 Handler 是在 onCreate(...) 方
         * 法中创建的,所以它会与主线程的 Looper 相关联。
         */

        Handler handler = new Handler();
//        Log.i(TAG, "onCreate: handler  "+handler);
        mThumbnailDownloader = new ThumbnailDownloader<>(handler);
        /**
         * standard callback interface ,newbee!!!
         */
        mThumbnailDownloader.setmThumbnailDownloadListener(new ThumbnailDownloader.ThumbnailDownloadListener<ViewAdapter.ViewHolder>() {
            @Override
            public void onThumbnailDownloaded(ViewAdapter.ViewHolder viewHolder, Bitmap bitmap) {
                Drawable drawable= new BitmapDrawable(getResources(),bitmap);
                viewHolder.bindDrawable(drawable);
            }
        });
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG, "Background thread started");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
        Log.i(TAG, "onDestroy: Background thread destroyed");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.photo_recycler, container, false);
        recyclerView = view.findViewById(R.id.recycler_id);
        viewAdapter = new ViewAdapter(mItem);
        recyclerView.setAdapter(viewAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        return view;
    }



/*
    void dataChangedNotify() {
        Log.i(TAG, "dataChangedNotify: execute" + viewAdapter);
        viewAdapter.notifyDataSetChanged();
    }
*/


    /**
     * order to execute task asynchronously
     * 即使fragment/activity已销毁了(或者视图已看不到了),也可以不撤销 AsyncTask ,让它运
     * 行至结束把事情做完。不过,这可能会引发内存泄漏(比如,没用的Activity实例本应销毁,但
     * 一直还在内存里)
     * ,也可能会出现UI更新问题(因为UI已失效)。如果不管用户怎么操作,要确保
     * 重要工作能完成,那最好考虑其他解决方案,比如使用 Service (详见第28章)。
     */
    private class FetchItemsTask extends AsyncTask<Void, Integer, List<GalleryItem>> {

        /**
         * 我们让 doInBackground(...) 方法返回了 GalleryItem 对象 List 。这样既修正了代
         * 码编译错误,还将 GalleryItem 对象 List 传递给了 onPostExecute(...) 方法
         *
         * @param params
         * @return
         */
        @Override
        protected List<GalleryItem> doInBackground(Void... params) {
            Log.i(TAG, "doInBackground:");
            return new FlickrFetchr().fetchItems();
        }

        /**
         * 最后,我们添加了 onPostExecute(...) 方法实现代码。该方法接收 doInBackground(...) 方
         * 法 返 回 的 GalleryItem 数 据 , 并 放 入 mItems 变 量 , 然 后 调 用 setupAdapter() 方 法 更 新
         * RecyclerView视图的adapter。
         *
         * @param items
         */
        @Override
        protected void onPostExecute(List<GalleryItem> items) {
            Log.i(TAG, "onPostExecute: " + items.size());
            PhotoGalleryFragment.this.mItem = items;
//            viewAdapter = new ViewAdapter(PhotoGalleryFragment.this.mItem);
            viewAdapter.galleryItems = items;
            recyclerView.setAdapter(viewAdapter);
            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        }
    }


    /**
     *
     */
    private class ViewAdapter extends RecyclerView.Adapter<ViewAdapter.ViewHolder> {
        private static final String TAG = "ViewAdapter";
        private List<GalleryItem> galleryItems;

        class ViewHolder extends RecyclerView.ViewHolder {

            //            private TextView mTitleTextView;
            private ImageView mImageView;


            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                mImageView = (ImageView) itemView.findViewById(R.id.item_image);
            }

            /*            public void bindGalleryItem(GalleryItem item) {
                            Log.i(TAG, "bindGalleryItem: execute" + item.toString());
                            mTitleTextView.setText(item.toString());
                        }*/
            public void bindDrawable(Drawable drawable) {
                mImageView.setImageDrawable(drawable);
            }
        }

        public ViewAdapter(List<GalleryItem> galleryItems) {
            Log.i(TAG, "ViewAdapter: galleryItems" + galleryItems.size());
            this.galleryItems = galleryItems;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Log.i(TAG, "onCreateViewHolder: execute");
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            GalleryItem galleryItem = galleryItems.get(position);
            Drawable drawable = getResources().getDrawable(R.drawable.bill_up_close);
            holder.bindDrawable(drawable);
            mThumbnailDownloader.queueThumbnail(holder, galleryItem.getmUrl());
//            holder.bindGalleryItem(galleryItem);

        }

        @Override
        public int getItemCount() {
            return galleryItems.size();
        }
    }
}
