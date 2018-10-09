package com.example.administrator.douyinviewpager.activity;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.dingmouren.layoutmanagergroup.viewpager.OnViewPagerListener;
import com.dingmouren.layoutmanagergroup.viewpager.ViewPagerLayoutManager;
import com.example.administrator.douyinviewpager.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ViewPagerActivity";
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    private ViewPagerLayoutManager mLayoutManager;
    private String recordUrl="http://www.kuwoshipin.com/ssm/uservideopagej.do?pageIndex=1";
    private OkHttpClient okHttpClient;
    private String bodyMessage;
    private List<String> videoList;//视频集合
    private List<String> imageList;//封面集合
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initListener();
    }

    private void initView() {
        mRecyclerView = findViewById(R.id.recycler);
        videoList=new ArrayList<>();
        imageList=new ArrayList<>();
        okHttpClient=new OkHttpClient();
        Request request=new Request.Builder()
                .url(recordUrl)
                .build();
        okHttpClient.newCall(request).enqueue(new MyClient());
        mLayoutManager = new ViewPagerLayoutManager(this, OrientationHelper.VERTICAL);
    }
    private class MyClient implements Callback {
        @Override
        public void onFailure(Call call, IOException e) {
            return;
        }

        @Override
        public void onResponse(Call call, Response response) {
            try {
                bodyMessage=response.body().string();
                Log.e("url",bodyMessage);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initJson(bodyMessage);
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //解析服务器返回的数据
    private void initJson(final String msg){
        try {
            JSONObject object=new JSONObject(msg);
            JSONArray array=object.getJSONArray("videolist");
            for(int i=0;i<array.length();i++){
                JSONObject jsonObject=array.getJSONObject(i);
                if(jsonObject!=null&&!jsonObject.equals("")&&jsonObject.length()>0){
                    String url="http://www.kuwoshipin.com/ssm/m3u8/"+jsonObject.getString("url");
                    String imageUrl="http://www.kuwoshipin.com/ssm/pic/"+jsonObject.getString("attribute1");
                    videoList.add(url);
                    imageList.add(imageUrl);
                }
            }
            mAdapter = new MyAdapter(this);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(mAdapter);
        }catch (Exception e){

        }
    }
    private void initListener(){
        mLayoutManager.setOnViewPagerListener(new OnViewPagerListener() {
            @Override
            public void onInitComplete() {

            }

            @Override
            public void onPageRelease(boolean isNext,int position) {
                Log.e(TAG,"释放位置:"+position +" 下一页:"+isNext);
                int index = 0;
                if (isNext){
                    index = 0;
                }else {
                    index = 1;
                }
                releaseVideo(index);
            }

            @Override
            public void onPageSelected(int position,boolean isBottom) {
                Log.e(TAG,"选中位置:"+position+"  是否是滑动到底部:"+isBottom);
                playVideo(0);
            }


            public void onLayoutComplete() {
                playVideo(0);
            }

        });
    }

    private void playVideo(int position) {
        View itemView = mRecyclerView.getChildAt(0);
        final VideoView videoView = itemView.findViewById(R.id.video_view);
        final ImageView imgPlay = itemView.findViewById(R.id.img_play);
        final ImageView imgThumb = itemView.findViewById(R.id.img_thumb);
        final RelativeLayout rootView = itemView.findViewById(R.id.root_view);
        final MediaPlayer[] mediaPlayer = new MediaPlayer[1];
        videoView.start();
        videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                mediaPlayer[0] = mp;
                Log.e(TAG,"onInfo");
                mp.setLooping(true);
                imgThumb.animate().alpha(0).setDuration(200).start();
                return false;
            }
        });
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.e(TAG,"onPrepared");

            }
        });


        imgPlay.setOnClickListener(new View.OnClickListener() {
            boolean isPlaying = true;
            @Override
            public void onClick(View v) {
                if (videoView.isPlaying()){
                    Log.e(TAG,"isPlaying:"+videoView.isPlaying());
                    imgPlay.animate().alpha(1f).start();
                    videoView.pause();
                    isPlaying = false;
                }else {
                    Log.e(TAG,"isPlaying:"+videoView.isPlaying());
                    imgPlay.animate().alpha(0f).start();
                    videoView.start();
                    isPlaying = true;
                }
            }
        });
    }

    private void releaseVideo(int index){
        View itemView = mRecyclerView.getChildAt(index);
        final VideoView videoView = itemView.findViewById(R.id.video_view);
        final ImageView imgThumb = itemView.findViewById(R.id.img_thumb);
        final ImageView imgPlay = itemView.findViewById(R.id.img_play);
        videoView.stopPlayback();
        imgThumb.animate().alpha(1).start();
        imgPlay.animate().alpha(0f).start();
    }


    class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{
//        private int[] imgs = {R.mipmap.video11,R.mipmap.video12,R.mipmap.video13,R.mipmap.video14,R.mipmap.img_video_2};
//        private int[] videos = {R.raw.video11,R.raw.video12,R.raw.video13,R.raw.video14,R.raw.video_2};
        private Context context;
        public MyAdapter(Context context){
            this.context=context;
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view_pager,parent,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
//            holder.img_thumb.setImageResource(imgs[position%5]);
            Glide.with(context).load(imageList.get(position)).into(holder.img_thumb);
            holder.videoView.setVideoURI(Uri.parse(videoList.get(position)));
        }

        @Override
        public int getItemCount() {
            return 50;
        }

        public class ViewHolder extends RecyclerView.ViewHolder{
            ImageView img_thumb;
            VideoView videoView;
            ImageView img_play;
            RelativeLayout rootView;
            public ViewHolder(View itemView) {
                super(itemView);
                img_thumb = itemView.findViewById(R.id.img_thumb);
                videoView = itemView.findViewById(R.id.video_view);
                img_play = itemView.findViewById(R.id.img_play);
                rootView = itemView.findViewById(R.id.root_view);
            }
        }
    }
}
