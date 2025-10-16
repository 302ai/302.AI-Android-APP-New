package com.newAi302.app.network;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/7/1
 * desc   :
 * version: 1.0
 */
public class GenericsUtils {

    public GenericsUtils() {
    }

    public static Class getSuperClassGenricClass(Class clazz) {
        return getSuperClassGenricClass(clazz, 0);
    }

    public static Class getSuperClassGenricClass(Class clazz, int index) throws IndexOutOfBoundsException {
        Type genType = clazz.getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)) {
            return Object.class;
        } else {
            Type[] params = ((ParameterizedType)genType).getActualTypeArguments();
            if (index < params.length && index >= 0) {
                return !(params[index] instanceof Class) ? Object.class : (Class)params[index];
            } else {
                return Object.class;
            }
        }
    }

    public static Type getSuperClassGenricType(Class clazz) {
        return getSuperClassGenricType(clazz, 0);
    }

    public static Type getSuperClassGenricType(Class clazz, int index) throws IndexOutOfBoundsException {
        Type genType = clazz.getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)) {
            return Object.class;
        } else {
            Type[] params = ((ParameterizedType)genType).getActualTypeArguments();
            return (Type)(index < params.length && index >= 0 ? params[index] : Object.class);
        }
    }
}
