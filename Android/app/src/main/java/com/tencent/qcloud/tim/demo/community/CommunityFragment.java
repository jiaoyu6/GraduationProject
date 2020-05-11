package com.tencent.qcloud.tim.demo.community;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tencent.qcloud.tim.demo.R;
import com.tencent.qcloud.tim.demo.menu.Menu;
import com.tencent.qcloud.tim.demo.profile.ProfileLayout;
import com.tencent.qcloud.tim.uikit.base.BaseFragment;
import com.tencent.qcloud.tim.uikit.component.TitleBarLayout;

import static android.view.View.GONE;


public class CommunityFragment extends BaseFragment {
    private View mBaseView;
    private TitleBarLayout mTitleBar;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.community_fragment, container, false);
        mTitleBar = mBaseView.findViewById(R.id.self_info_title_bar);
        mTitleBar.getLeftGroup().setVisibility(GONE);
        mTitleBar.getRightGroup().setVisibility(GONE);
        mTitleBar.setTitle(getResources().getString(R.string.profile), TitleBarLayout.POSITION.MIDDLE);



        return mBaseView;
    }

}
