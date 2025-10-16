package com.newAi302.app.network;

import com.newAi302.app.network.callback.NetworkProgressListener;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;

/**
 * author :
 * e-mail :
 * time   : 2024/05/6
 * desc   : 网络请求 - 进度 - 代理类
 * version: 1.0
 */
public class NetworkProgressProxy extends RequestBody {

    // 被代理的对象
    private RequestBody mRequestBody;
    //回调接口
    private NetworkProgressListener mListener;
    //当前的长度
    private long mCurrentLength;
    //总长度
    private long mTotalLength;

    public NetworkProgressProxy(RequestBody requestBody, NetworkProgressListener listener) {
        this.mRequestBody = requestBody;
        this.mListener = listener;
    }

    @Override
    public long contentLength() throws IOException {
        return mRequestBody.contentLength();
    }

    @Override
    public MediaType contentType() {
        return mRequestBody.contentType();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        //总长度
        mTotalLength = contentLength();
        //这里也是静态代理
        //把sink传进去,也是跟MyMultipartBody这个静态代理如出一辙的手法,这样可以弄个中间件过一层
        ForwardingSink forwardingSink = new ForwardingSink(sink) {
            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                //写内容就会来这里,其实也是sink在写
                mCurrentLength += byteCount;
                if (mListener != null) {
                    mListener.onProgress(mCurrentLength, mTotalLength);
                }
                super.write(source, byteCount);
            }
        };
        //包装成BufferedSink
        BufferedSink buffer = Okio.buffer(forwardingSink);
        // 最终调用者还是被代理对象的方法
        mRequestBody.writeTo(buffer);
        // 刷新，RealConnection 连接池
        buffer.flush();
    }
}
