package com.newAi302.app.base.mvp;

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/7/1
 * desc   : MVPBaseActivity基础类
 * version: 1.0
 */

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

public abstract class MVPBaseActivity<IView extends BaseIView, B extends ViewBinding, P extends BasePresenter> extends BaseActivity<B> {

    protected P mPresenter;

    public MVPBaseActivity() {
    }

    @Nullable
    public abstract P createPresenter();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mPresenter = this.createPresenter();
        initView();
        initListener();
        if (this.mPresenter != null) {
            mPresenter.attachView((IView) this);
            mPresenter.initData(savedInstanceState);
        }
        initData(savedInstanceState);
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        if (mPresenter != null) {
            mPresenter.onNewIntent();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        View view = super.onCreateView(name, context, attrs);
        if (this.mPresenter != null) {
            this.mPresenter.onCreateView();
        }
        return view;
    }

    @Nullable
    @Override
    public View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        View view = super.onCreateView(parent, name, context, attrs);
        if (this.mPresenter != null) {
            this.mPresenter.onCreateView();
        }
        return view;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.mPresenter != null) {
            this.mPresenter.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (this.mPresenter != null) {
            this.mPresenter.onPause();
        }
        if (this.isFinishing()) {
            onWillDestroy();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            this.mPresenter.detachView();
            this.mPresenter.onDestroy();
        }
    }

    public BasePresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }


}
