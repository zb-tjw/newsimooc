package com.qust.newsimooc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Lenovo_PC on 2016/11/18.
 */

public class ImageLoader {

    private ImageView mImageView;
    private String mURL;
    //创建Cache
    private LruCache<String, Bitmap> mCaches;
    private ListView mListview;
    private Set<NewsAsyncTask> mTask;

    public ImageLoader(ListView listView){
        mListview = listView;
        mTask = new HashSet<>();
        //获取最大可用内存
        int MaxMemory = (int) Runtime.getRuntime().maxMemory();
        int CacheSize = MaxMemory / 4;
        mCaches = new LruCache<String, Bitmap>(CacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                //在每次存入缓存的时候去调用
                return value.getByteCount();
            }
        };
    }

    //增加到缓存
    public void addBitmapToCache(String url, Bitmap bitmap){
        //校验缓存是否存在
        if(getBitMapFromCache(url) == null){
            mCaches.put(url,bitmap);
        }
    }

    //从缓存中获取数据
    public Bitmap getBitMapFromCache(String url){
        return mCaches.get(url);
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mImageView.getTag().equals(mURL)){
                mImageView.setImageBitmap((Bitmap) msg.obj);
            }
        }
    };

    public void showImageByThread(ImageView imageView, final String url){
        mImageView = imageView;
        mURL = url;
        new Thread(){
            @Override
            public void run() {
                super.run();
                Bitmap bitmap = getBitmapFromURL(url);
                Message message = Message.obtain();
                message.obj = bitmap;
                mHandler.sendMessage(message);
            }
        }.start();
    }

    public Bitmap getBitmapFromURL(String urlString){
        Bitmap bitmap;
        InputStream is = null;
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            is = new BufferedInputStream(connection.getInputStream());
            bitmap = BitmapFactory.decodeStream(is);
            connection.disconnect();
//            Thread.sleep(1000);
            return bitmap;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ;
        }
        return  null;
    }


    public void showImageByAsyncTask(ImageView imageView, String url){
        //从缓存中取出对应的图片
        Bitmap bitmap = getBitMapFromCache(url);
        //如果缓存中有就不必去网络下载，直接使用内存中已存在的图片就可以
        if (bitmap != null){
            imageView.setImageBitmap(bitmap);
        }else{
            imageView.setImageResource(R.mipmap.ic_launcher);
        }
    }

    public void cancelAllTasks(){
        if (mTask != null){
            for (NewsAsyncTask task : mTask){
                //把任务取消掉
                //true表示如果进程正在执行应该被打断停止；否则的话，正在执行的进程允许执行完
                task.cancel(false);
            }
        }
    }

    //加载指定的从start到end这个系列在中的图片
    public void loadImages(int start, int end){
        for (int i = start; i < end ; i++){
            String url = NewsAdapter.URLs[i];
            //从缓存中取出对应的图片
            Bitmap bitmap = getBitMapFromCache(url);
            //如果缓存中有就不必去网络下载，直接使用内存中已存在的图片就可以
            if (bitmap != null){
                ImageView imageView = (ImageView) mListview.findViewWithTag(url);
                if (imageView == null){
                    Log.i("tag","iamgeview is null");
                }
                if(bitmap == null){
                    Log.i("tag","bitmap is null");
                }
                if (imageView!=null){//改if为运行时空指针所以自己添加
                    imageView.setImageBitmap(bitmap);
                }
//                imageView.setImageBitmap(bitmap);
            }else{
                NewsAsyncTask task = new NewsAsyncTask(url);
                task.execute(url);
                mTask.add(task);
            }
        }
    }

    private class NewsAsyncTask extends AsyncTask<String, Void, Bitmap>{

//        private ImageView mImageView;
        private String mURL;

        public NewsAsyncTask(String url){
//            mImageView = imageView;
            mURL = url;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String url = params[0];
            Bitmap bitmap = getBitmapFromURL(url);
            if (bitmap != null){
                addBitmapToCache(url,bitmap);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
//            if (mImageView.getTag().equals(mURL)){
//                mImageView.setImageBitmap(bitmap);
//            }
            ImageView imageView = (ImageView) mListview.findViewWithTag(mURL);
            if (imageView != null && bitmap != null){
                imageView.setImageBitmap(bitmap);
            }
            mTask.remove(this);
        }
    }
}
