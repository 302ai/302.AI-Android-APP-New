package com.newAi302.app.network.cookie.persistence;

import java.util.Collection;
import java.util.List;

import okhttp3.Cookie;

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/7/1
 * desc   :
 * version: 1.0
 */
public interface CookiePersistor {

    List<Cookie> loadAll();

    /**
     * Persist all cookies, existing cookies will be overwritten.
     *
     * @param cookies cookies persist
     */
    void saveAll(Collection<Cookie> cookies);

    /**
     * Removes indicated cookies from persistence.
     *
     * @param cookies cookies to remove from persistence
     */
    void removeAll(Collection<Cookie> cookies);

    /**
     * Clear all cookies from persistence.
     */
    void clear();
}
