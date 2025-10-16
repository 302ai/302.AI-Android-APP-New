package com.newAi302.app.network;



import static com.newAi302.app.utils.base.WearUtil.APP_DATE_SERVER;


/**
 * author : lzh
 * e-mail : maizhancheng@hytto.com
 * time   : 2022/7/28
 * desc   : 不同的url访问不同的域名地址,目前app维护3个域名地址
 * version: 1.0.0
 */
public class HostDispenser {

    public String getHost(String oldHttpUrl) {
        return APP_DATE_SERVER;
    }
}
