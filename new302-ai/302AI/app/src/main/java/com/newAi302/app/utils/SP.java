package com.newAi302.app.utils;

import android.app.Application;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.Nullable;

import com.tencent.mmkv.MMKV;

import java.util.HashSet;
import java.util.Set;

public final class SP {
    private static SP mInstance;
    private MMKV kv;

    private SP() {
    }

    public static SP getInstance() {
        if (mInstance == null) {
            synchronized (SP.class) {
                if (mInstance == null) {
                    mInstance = new SP();
                }
            }
        }
        return mInstance;
    }

    public void init(Application app) {
        String rootDir = MMKV.initialize(app);
        Log.e("SP", " rootDir = " + rootDir);
        kv = MMKV.defaultMMKV();
    }

    public void put(String k, boolean v) {
        kv.encode(k, v);
    }

    public boolean getBoolean(String k) {
        return getBoolean(k, false);
    }

    public boolean getBoolean(String k, boolean defaultV) {
        return kv.decodeBool(k, defaultV);
    }

    public void put(String k, int v) {
        kv.encode(k, v);
    }

    public int getInt(String k) {
        return getInt(k, 0);
    }

    public int getInt(String k, int defaultV) {
        return kv.decodeInt(k, defaultV);
    }

    public void put(String k, String v) {
        kv.encode(k, v);
    }

    public String getString(String k) {
        return getString(k, "");
    }

    public String getString(String k, String defaultV) {
        return kv.decodeString(k, defaultV);
    }

    public void put(String k, float v) {
        kv.encode(k, v);
    }

    public float getFloat(String k) {
        return getFloat(k, 0.0f);
    }

    public float getFloat(String k, float defaultV) {
        return kv.decodeFloat(k, defaultV);
    }

    public void put(String k, double v) {
        kv.encode(k, v);
    }

    public double getDouble(String k) {
        return getDouble(k, 0.0D);
    }

    public double getDouble(String k, double defaultV) {
        return kv.decodeDouble(k, defaultV);
    }

    public void put(String k, long v) {
        kv.encode(k, v);
    }

    public long getLong(String k) {
        return getLong(k, 0);
    }

    public long getLong(String k, long defaultV) {
        return kv.decodeLong(k, defaultV);
    }

    public void put(String k, byte[] v) {
        kv.encode(k, v);
    }

    public byte[] getByte(String k) {
        return getByte(k, null);
    }

    public byte[] getByte(String k, byte[] defaultV) {
        return kv.decodeBytes(k, defaultV);
    }

    public void put(String k, Set<String> v) {
        kv.encode(k, v);
    }

    public Set<String> getSetString(String k) {
        return getSetString(k, null);
    }

    public Set<String> getSetString(String k, Set<String> defaultV) {
        return getSetString(k, defaultV, HashSet.class);
    }

    public Set<String> getSetString(String k, Set<String> defaultV, Class<? extends Set> clz) {
        return kv.decodeStringSet(k, defaultV, clz);
    }

    public void put(String k, Parcelable v) {
        kv.encode(k, v);
    }

    public <T extends Parcelable> T getParcelable(String k) {
        return getParcelable(k, null);
    }

    public <T extends Parcelable> T getParcelable(String k, Class<T> tClass) {
        return getParcelable(k, tClass, null);
    }

    public <T extends Parcelable> T getParcelable(String k, Class<T> tClass, @Nullable T defaultValue) {
        return kv.decodeParcelable(k, tClass, defaultValue);
    }

    public void remove(String k) {
        kv.remove(k);
    }
}
