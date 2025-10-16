package com.newAi302.app.network.cookie;

import okhttp3.CookieJar;

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/7/1
 * desc   :
 * version: 1.0
 */
public interface ClearableCookieJar extends CookieJar {

    /**
     * Clear all the session cookies while maintaining the persisted ones.
     */
    void clearSession();

    /**
     * Clear all the cookies from persistence and from the cache.
     */
    void clear();
}
