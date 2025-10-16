package com.newAi302.app.ui.thirdloginsdk;


import android.app.Activity;
import android.content.Intent;

import com.newAi302.app.ui.thirdloginsdk.GoogleLogin;
import com.newAi302.app.ui.thirdloginsdk.ITLoginListener;
import com.newAi302.app.ui.thirdloginsdk.ITLogoutListener;
import com.newAi302.app.ui.thirdloginsdk.IXLogin;
import com.newAi302.app.utils.LogUtils;

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/7/16
 * desc   :
 * version: 1.0
 */
public class TLoginMgr {

    private static TLoginMgr instance;
    private IXLogin mLogin;
    private LoginType mLoginType;

    private TLoginMgr() {
    }

    public static TLoginMgr getInstance() {
        if (instance == null) {
            instance = new TLoginMgr();
        }
        return instance;
    }

    public void login(Activity context, LoginType type, ITLoginListener listener) {
        if (mLogin == null || mLoginType != type) {
            release();
            mLoginType = type;
            mLogin = getLogin(mLoginType);
        }
        if (mLogin != null) {
            mLogin.login(context, listener);
        }
    }

    public void onActivityResult(int reqCode, int resCode, Intent data) {
        if (mLogin != null) {
            mLogin.onActivityResult(reqCode, resCode, data);
        }
    }

    public void logout(Activity context, ITLogoutListener listener) {
        if (mLoginType != null) {
            mLogin = getLogin(mLoginType);
        }
        if (mLogin != null) {
            mLogin.logout(context, listener);
        }
    }

    public void release() {
        if (mLogin != null) {
            mLogin.release();
            mLogin = null;
        }
    }

    private IXLogin getLogin(LoginType type) {
        if (type == LoginType.Google) {
            return new GoogleLogin();
        }/*else if (type == LoginType.Facebook) {
            return new FaceBookLogin();
        } */
        return null;
    }

    public enum LoginType {
        Google, Facebook
    }
}