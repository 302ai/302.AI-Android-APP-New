package com.newAi302.app.utils.base;

import android.text.TextUtils;

import com.newAi302.app.constant.SPKeyConstance;
import com.newAi302.app.utils.SPUtils;
import com.newAi302.app.utils.ThreadUtils;

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/7/1
 * desc   : 缓存数据管理类
 * version: 1.0
 */
public class WearData {

    private static WearData instance = new WearData();

    private WearData() {
    }

    public static WearData getInstance() {
        return instance;
    }

    /**
     * 保持Token
     */
    public void saveToken(String token) {
        SPUtils.getInstance().put(SPKeyConstance.USER_LOGIN_SUCCESS, token);
    }

    /**
     * 获取token
     */
    public String getToken() {
        return SPUtils.getInstance().getString(SPKeyConstance.USER_LOGIN_SUCCESS);
    }

    public void saveGetModelList(Boolean isGet) {
        SPUtils.getInstance().put(SPKeyConstance.GET_MODEL_LIST_SUCCESS, isGet);
    }

    /**
     * 获取token
     */
    public Boolean getGetModelList() {
        return SPUtils.getInstance().getBoolean(SPKeyConstance.GET_MODEL_LIST_SUCCESS);
    }

    //保存邮箱登录code
    public void saveLoginEmailCode(String emailCode) {
        SPUtils.getInstance().put(SPKeyConstance.USER_LOGIN_EMAIL_CODE, emailCode);
    }

    public String getEmailCode() {
        return SPUtils.getInstance().getString(SPKeyConstance.USER_LOGIN_EMAIL_CODE);
    }

    //保存邮箱登录密码
    public void saveLoginEmailPassWord(String emailPassWord) {
        SPUtils.getInstance().put(SPKeyConstance.USER_LOGIN_EMAIL_PASSWORD, emailPassWord);
    }

    public String getEmailPassWord() {
        return SPUtils.getInstance().getString(SPKeyConstance.USER_LOGIN_EMAIL_PASSWORD);
    }

    //保存手机登录手机号码
    public void saveLoginPhoneCode(String phoneCode) {
        SPUtils.getInstance().put(SPKeyConstance.USER_LOGIN_PHONE_CODE, phoneCode);
    }

    public String getPhoneCode() {
        return SPUtils.getInstance().getString(SPKeyConstance.USER_LOGIN_PHONE_CODE);
    }

    //保存手机登录密码
    public void saveLoginPhonePassWord(String phonePassWord) {
        SPUtils.getInstance().put(SPKeyConstance.USER_LOGIN_PHONE_PASSWORD, phonePassWord);
    }

    public String getLoginPhonePassWord() {
        return SPUtils.getInstance().getString(SPKeyConstance.USER_LOGIN_PHONE_PASSWORD);
    }

    //用户选择的国家 如 86
    public void saveCountryCode(String countryCode) {
        SPUtils.getInstance().put(SPKeyConstance.USER_SELECT_COUNTRY_CODE, countryCode);
    }

    public String getCountryCode() {
        return SPUtils.getInstance().getString(SPKeyConstance.USER_SELECT_COUNTRY_CODE);
    }

    //保存选择记住密码  -- 邮箱
    public void saveRememberPassword(boolean isSelect) {
        SPUtils.getInstance().put(SPKeyConstance.USER_SELECT_REMEMBER_PASSWORD_EMAIL, isSelect);
    }

    //判断用户是否选择了记住密码操作 -- 邮箱
    public boolean isRememberPassWord() {
        return SPUtils.getInstance().getBoolean(SPKeyConstance.USER_SELECT_REMEMBER_PASSWORD_EMAIL, false);
    }

    //保存选择记住密码  -- 手机
    public void saveRememberPasswordPhone(boolean isSelect) {
        SPUtils.getInstance().put(SPKeyConstance.USER_SELECT_REMEMBER_PASSWORD_PHONE, isSelect);
    }

    //判断用户是否选择了记住密码操作 -- 手机
    public boolean isRememberPassWordPhone() {
        return SPUtils.getInstance().getBoolean(SPKeyConstance.USER_SELECT_REMEMBER_PASSWORD_PHONE, false);
    }

    public boolean isReadAndAgreeUTsAndPAs() {
        return SPUtils.getInstance().getBoolean(SPKeyConstance.READ_AGREE_UTS_PAS, false);
    }

    public void saveReadAndAgreeUTsAndPAs(boolean isSelect) {
        SPUtils.getInstance().put(SPKeyConstance.READ_AGREE_UTS_PAS, isSelect);
    }

    /**
     * 判断是否登录
     */
    public Boolean isLogin() {
        return !TextUtils.isEmpty(getToken());
    }

    /**
     * 退出登录
     */
    public void onLogout() {
        ThreadUtils.runOnUiThreadDelayed(new Runnable() {
            @Override
            public void run() {
                SPUtils.getInstance().remove(SPKeyConstance.USER_LOGIN_SUCCESS);
            }
        }, 500);
    }
}
