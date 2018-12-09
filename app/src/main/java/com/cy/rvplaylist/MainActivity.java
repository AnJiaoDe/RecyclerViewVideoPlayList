package com.cy.rvplaylist;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.cy.cyrvadapter.adapter.RVAdapter;
import com.cy.cyrvadapter.recyclerview.VerticalRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RVAdapter<PlayBean> rvAdapter;

    private int position_play = -1;//播放的位置

    //用户手动点击播放后，自动播放开始，
    // 除非用户手动点击停止，或者视频播放完毕，停止自动播放，
    private boolean isLooper = false;
    private int looperFlag = 0;//0,无自动播放，1.自动播放上一个，2自动播放下一个
    private List<PlayBean> list;

    private VerticalRecyclerView verticalRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        list = new ArrayList<>();
//播放状态   0播放，1停止
        list.add(new PlayBean(1));
        list.add(new PlayBean(1));
        list.add(new PlayBean(1));
        list.add(new PlayBean(1));
        list.add(new PlayBean(1));
        list.add(new PlayBean(1));
        list.add(new PlayBean(1));
        list.add(new PlayBean(1));
        list.add(new PlayBean(1));
        list.add(new PlayBean(1));
        list.add(new PlayBean(1));
        list.add(new PlayBean(1));
        /**
         * 万能适配器，参考https://github.com/AnJiaoDe/RecyclerViewAdapter
         */
        rvAdapter = new RVAdapter<PlayBean>(list) {
            @Override
            public void bindDataToView(RVViewHolder holder, int position, PlayBean bean, boolean isSelected) {

                int state = bean.getState();
                String tv_text = "";
                //播放状态   0播放，1停止
                switch (state) {
                    case 0:
                        tv_text = position + "正在播放";
                        holder.setTextColor(R.id.tv, 0xffff0000);
                        break;
                    case 1:
                        tv_text = position + "停止";
                        holder.setTextColor(R.id.tv, 0xff454545);

                        break;

                }
                holder.setText(R.id.tv, tv_text);
            }

            @Override
            public int getItemLayoutID(int position, PlayBean bean) {
                return R.layout.item;
            }

            @Override
            public void onItemClick(int position, PlayBean bean) {
                //播放状态   0播放，1停止
                int state = bean.getState();
                switch (state) {
                    case 0:
                        bean.setState(1);
                        isLooper = false;//自动播放停止
                        break;
                    case 1:
                        if (position_play != -1)
                            list.get(position_play).setState(1);//上次播放的要停止
                        position_play = position;
                        bean.setState(0);
                        isLooper = true;//自动播放开始
                        break;
                }
                notifyDataSetChanged();
            }
        };

        verticalRecyclerView = ((VerticalRecyclerView) findViewById(R.id.vrv));
        verticalRecyclerView.setAdapter(rvAdapter);


        verticalRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //滑动停止后，
                if (newState == RecyclerView.SCROLL_STATE_IDLE && isLooper && looperFlag != 0) {
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                    switch (looperFlag) {

                        case 1:
                            int position_lastVisible=layoutManager.findLastVisibleItemPosition();
                            if (position_lastVisible==position_play){
                                //自动播放上一个
                                position_play-=1;
                            }else {
                                //最后一个可见的item和滑出去的上次播放的view隔了N(N>=1)个Item,所以自动播放倒数第2个可见的item
                                position_play=position_lastVisible-1;
                            }

                            break;
                        case 2:
                            int position_firstVisible=layoutManager.findFirstVisibleItemPosition();
                            if (position_firstVisible==position_play){
                                //自动播放下一个

                                position_play+=1;
                            }else {
                                //第一个可见的item和滑出去的上次播放的view隔了N(N>=1)个Item,所以自动播放第2个可见的item
                                position_play=position_firstVisible+1;

                            }

                            break;
                    }
                    list.get(position_play).setState(0);

                    rvAdapter.notifyItemChanged(position_play);

                    //注意
                    looperFlag=0;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!isLooper) return;
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                View view = layoutManager.findViewByPosition(position_play);
                //说明播放的view还未完全消失
                if (view != null) {

                    int y_t_rv = ScreenUtils.getViewScreenLocation(verticalRecyclerView)[1];//RV顶部Y坐标
                    int y_b_rv = y_t_rv + verticalRecyclerView.getHeight();//RV底部Y坐标

                    int y_t_view = ScreenUtils.getViewScreenLocation(view)[1];//播放的View顶部Y坐标
                    int height_view = view.getHeight();
                    int y_b_view = y_t_view + height_view;//播放的View底部Y坐标

                    //上滑
                    if (dy > 0) {
                        //播放的View上滑，消失了一半了,停止播放，
                        if ((y_t_rv > y_t_view) && ((y_t_rv - y_t_view) > height_view * 1f / 2)) {

                            list.get(position_play).setState(1);
                            rvAdapter.notifyItemChanged(position_play);
                            looperFlag = 2;//自动播放下一个
                        }

                    } else if (dy < 0) {
                        //下滑

//                        LogUtils.log("y_t_rv", y_t_rv);
//                        LogUtils.log("y_b_rv", y_b_rv);
                        //播放的View下滑，消失了一半了,停止播放
                        if ((y_b_view > y_b_rv) && ((y_b_view - y_b_rv) > height_view * 1f / 2)) {

                            list.get(position_play).setState(1);
                            rvAdapter.notifyItemChanged(position_play);
                            looperFlag = 1;//自动播放上一个


                        }
                    }
                }


            }
        });

    }
}
