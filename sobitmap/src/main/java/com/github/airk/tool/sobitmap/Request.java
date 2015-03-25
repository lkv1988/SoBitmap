package com.github.airk.tool.sobitmap;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import java.io.InputStream;
import java.util.concurrent.Future;

/**
 * Created by kevin on 15/3/24.
 * <p/>
 * Bitmap hunt request
 */
final class Request implements Callback {
    final String tag;
    final Uri source;
    final Options options;
    private final Callback callback;
    final Hunter hunter;
    Future<?> task;
    String key;
    HuntException e;
    int quality = 100;
    int recursionCount = -1;
    long startInMs = -1;
    long costInMs = -1;

    InputStream is;

    private Handler handler;

    Request(String tag, Uri source, Options options, Callback callback, Hunter hunter) {
        this.tag = tag;
        this.source = source;
        this.options = options;
        this.callback = callback;
        this.hunter = hunter;

        handler = new Handler(Looper.getMainLooper());
        key = "KEY:" + source.toString() + "&&" + Integer.toHexString(options.hashCode());
    }

    @Override
    public String toString() {
        return "Request{" +
                "tag='" + tag + '\'' +
                ", source=" + source +
                ", options=" + options +
                ", callback=" + callback +
                ", key='" + key + '\'' +
                '}';
    }

    @Override
    public void onHunted(final Bitmap bitmap) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                callback.onHunted(bitmap);
            }
        });
    }

    @Override
    public void onException(final HuntException e) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                callback.onException(e);
            }
        });
    }
}
