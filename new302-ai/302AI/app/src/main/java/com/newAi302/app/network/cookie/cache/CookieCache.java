package com.newAi302.app.network.cookie.cache;

import java.util.Collection;

import okhttp3.Cookie;

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/7/1
 * desc   :
 * version: 1.0
 */
public interface CookieCache extends Iterable<Cookie> {
    /**
     * Add all the new cookies to the session, existing cookies will be overwritten.
     *
     * @param cookies
     */
    void addAll(Collection<Cookie> cookies);

    /**
     * Clear all the cookies from the session.
     */
    void clear();
}
