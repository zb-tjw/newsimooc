package com.qust.newsimooc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Lenovo_PC on 2016/11/18.
 */

public class NewsAdapter extends BaseAdapter implements AbsListView.OnScrollListener{

    //需要映射的数组
    private List<NewsBean> mList;
    //转化一个布局作为一个item
    private LayoutInflater mInflater;
    private ImageLoader mImageLoader;
    private int mStart,mEnd;
    //用来保存当前获取到的所有URL地址
    public static String[] URLs;
    private boolean mFirstIn;

    public NewsAdapter(Context context, List<NewsBean> data, ListView listView){
        mList = data;
        //初始化mInflater对象
        mInflater = LayoutInflater.from(context);
        //这么做是为了保证只有一个LruCache
        mImageLoader = new ImageLoader(listView);
        URLs = new String[data.size()];
        for (int i = 0; i< data.size() ; i++){
            URLs[i] = data.get(i).newsIconUrl;
        }
        mFirstIn = true;
        listView.setOnScrollListener(this);
    }
    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null){
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_layout,null);
            viewHolder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_icon);
            String url = mList.get(position).newsIconUrl;
            viewHolder.ivIcon.setTag(url);
//            new ImageLoader().showImageByThread(viewHolder.ivIcon, url);
//            new ImageLoader().showImageByAsyncTask(viewHolder.ivIcon, url);
            mImageLoader.showImageByAsyncTask(viewHolder.ivIcon, url);
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
            viewHolder.tvContent = (TextView) convertView.findViewById(R.id.tv_content);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
//        viewHolder.ivIcon.setImageResource(R.mipmap.ic_launcher);
        viewHolder.tvTitle.setText(mList.get(position).newsTitle);
        viewHolder.tvContent.setText(mList.get(position).newsContent);
        return convertView;
    }

    /**
     * 在listView滑动的状态切换的时候才去调用
     * @param view
     * @param scrollState
     */
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        //判断listView的滚动状态，滚动的时候取消掉所有的下载任务，滚动结束的时候加载所有的可见项
        if(scrollState == SCROLL_STATE_IDLE){
            //加载可见项
            mImageLoader.loadImages(mStart, mEnd);
        }else{
            //停止任务
            mImageLoader.cancelAllTasks();
        }

    }

    /**
     * 在整个滑动的过程中都会去调用
     * @param view
     * @param firstVisibleItem:第一个可见元素
     * @param visibleItemCount：可见元素长度
     * @param totalItemCount
     */
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mStart = firstVisibleItem;
        mEnd = firstVisibleItem + visibleItemCount;
        //手动加载第一屏
        if (mFirstIn && visibleItemCount > 0){
            mImageLoader.loadImages(mStart, mEnd);
            mFirstIn = false;
        }
    }

    class ViewHolder{
        public TextView tvTitle,tvContent;
        public ImageView ivIcon;
    }
}
