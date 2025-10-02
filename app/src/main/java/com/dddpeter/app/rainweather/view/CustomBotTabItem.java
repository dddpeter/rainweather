package com.dddpeter.app.rainweather.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.viewpager2.widget.ViewPager2;

import com.dddpeter.app.rainweather.R;
import com.google.android.material.tabs.TabLayout;

import java.util.Objects;


public class CustomBotTabItem {

    //底部Tab标题
    private final String[] mTitles = {"今日", "24H", "城市", "关于"};
    private TabLayout mTabLayout;
    private ViewPager2 mViewPager;
    private Context mContext;

    //返回CustomBotTabItem实例
    public static CustomBotTabItem create() {
        return TabItemHolder.sCustomTabItem;
    }

    //引入布局需要的Context
    public CustomBotTabItem setContext(Context context) {
        mContext = context;
        return this;
    }

    //需要自定义的TabLayout
    public CustomBotTabItem setTabLayout(TabLayout tabLayout) {
        mTabLayout = tabLayout;
        return this;
    }

    //设置与TabLayout关联的ViewPager
    public CustomBotTabItem setViewPager(ViewPager2 viewPager) {
        mViewPager = viewPager;
        return this;
    }

    //创建Tab
    public void build() {
        initTabLayout();
    }

    //初始化Tab
    private void initTabLayout() {

        for (String title : mTitles) {
            TabLayout.Tab tab = mTabLayout.newTab().setText(title);
            mTabLayout.addTab(tab);
        }
        Objects.requireNonNull(mTabLayout.getTabAt(0)).setCustomView(getTabView(0, mTitles[0], R.drawable.home));
        Objects.requireNonNull(mTabLayout.getTabAt(1)).setCustomView(getTabView(1, mTitles[1], R.drawable.main));
        Objects.requireNonNull(mTabLayout.getTabAt(2)).setCustomView(getTabView(2, mTitles[2], R.drawable.air));
        Objects.requireNonNull(mTabLayout.getTabAt(3)).setCustomView(getTabView(3, mTitles[3], R.drawable.about));
        tabSelectListener();

    }

    //自定义Tab样式
    private View getTabView(final int position, String title, int resId) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.bottom_tab_item, null);
        TextView tvTitle = (TextView) view.findViewById(R.id.id_tv_title);
        tvTitle.setText(title);
        final ImageView imageView = (ImageView) view.findViewById(R.id.id_iv_image);
        imageView.setImageResource(resId);
        
        // 默认第一个tab选中，设置浮层样式
        if (position == 0) {
            view.setSelected(true);
            view.setBackgroundColor(mContext.getResources().getColor(R.color.floating_tab_selected_background));
            tvTitle.setTextColor(mContext.getResources().getColor(R.color.floating_tab_text_selected));
            imageView.setColorFilter(mContext.getResources().getColor(R.color.floating_tab_icon_selected));
        } else {
            view.setSelected(false);
            view.setBackgroundColor(mContext.getResources().getColor(R.color.floating_tab_background));
            tvTitle.setTextColor(mContext.getResources().getColor(R.color.floating_tab_text_normal));
            imageView.setColorFilter(mContext.getResources().getColor(R.color.floating_tab_icon_normal));
        }
        return view;
    }

    //Tab监听
    private void tabSelectListener() {
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                changeTabStatus(tab, true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                changeTabStatus(tab, false);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    //切换Tab文字是否选中的的颜色
    private void changeTabStatus(TabLayout.Tab tab, boolean selected) {
        View view = tab.getCustomView();
        mViewPager.setCurrentItem(tab.getPosition());
        if (view == null) {
            return;
        }
        
        TextView tvTitle = (TextView) view.findViewById(R.id.id_tv_title);
        ImageView imageView = (ImageView) view.findViewById(R.id.id_iv_image);
        
        if (selected) {
            // 选中状态：浅色背景 + 深蓝色图标
            view.setSelected(true);
            view.setBackgroundColor(mContext.getResources().getColor(R.color.floating_tab_selected_background));
            tvTitle.setTextColor(mContext.getResources().getColor(R.color.floating_tab_text_selected));
            imageView.setColorFilter(mContext.getResources().getColor(R.color.floating_tab_icon_selected));
        } else {
            // 未选中状态：黑色背景 + 白色图标
            view.setSelected(false);
            view.setBackgroundColor(mContext.getResources().getColor(R.color.floating_tab_background));
            tvTitle.setTextColor(mContext.getResources().getColor(R.color.floating_tab_text_normal));
            imageView.setColorFilter(mContext.getResources().getColor(R.color.floating_tab_icon_normal));
        }
    }

    //创建CustomBotTabItem实例
    private static class TabItemHolder {
        private static final CustomBotTabItem sCustomTabItem = new CustomBotTabItem();
    }
}
