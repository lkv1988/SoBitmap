/*
 * Copyright 2015 Kevin Liu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.airk.tool.sobitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/**
 * Created by kevin on 15/3/24.
 * <p/>
 * SoBitmap
 */
public final class SoBitmap {
    static final String TAG = "SoBitmap";
    /**
     * Usage: adb shell setprop log.tag.SoBitmap V
     */
    static boolean LOG = Log.isLoggable(TAG, Log.VERBOSE);

    private volatile static SoBitmap sInstance;
    private Context context;
    private Options defaultOps;
    private File cacheDir;
    //TODO multi thread support
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private LinkedHashSet<Hunter> hunterSet = new LinkedHashSet<>();
    private ConcurrentHashMap<String, Request> requestMap;

    static final int MSG = 1;
    private Handler uiHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG:
                    String tag = (String) msg.obj;
                    requestMap.remove(tag);
                    return true;
                default:
                    return false;
            }
        }
    });

    private static final List<Class<? extends Hunter>> HUNTERS = Arrays.asList(
            FileHunter.class,
            NetworkHunter.class,
            MediaStoreHunter.class
    );

    /**
     * Get SoBitmap single instance
     *
     * @param context Context
     * @return SoBitmap instance
     */
    public static SoBitmap getInstance(Context context) {
        if (sInstance == null) {
            synchronized (SoBitmap.class) {
                if (sInstance == null) {
                    sInstance = new SoBitmap(context);
                }
            }
        }
        return sInstance;
    }

    /**
     * User can set SoBitmap's singleton instance to follow what he want by using this method. The Builder{@link com.github.airk.tool.sobitmap.SoBitmap.Builder}
     * can give users more opportunity to custom SoBitmap but not invoke the SoBitmap's constructor immediately, so we can keep SoBitmap's singleton mode safe.
     * ps: the method must be invoked before {@link #getInstance(android.content.Context)}, otherwise there will be a IllegalStateException.
     *
     * @param context Context
     * @param builder {@link com.github.airk.tool.sobitmap.SoBitmap.Builder}
     * @return SoBitmap instance
     */
    public static SoBitmap setInstanceByBuilder(@NonNull Context context, @NonNull Builder builder) {
        if (sInstance != null) {
            throw new IllegalStateException("Singleton instance has been created, please call this method before getInstance(Context)");
        }
        sInstance = new SoBitmap(context, builder.useExternalCache);
        return sInstance;
    }

    /**
     * Custom SoBitmap singleton instance. Now just support setup whether use external cache file.
     */
    public static class Builder {
        boolean useExternalCache = true;

        /**
         * Shall SoBitmap use external storage for cache, default is true.
         *
         * @param useExternalCache true for use
         */
        public Builder setUseExternalCache(boolean useExternalCache) {
            this.useExternalCache = useExternalCache;
            return this;
        }
    }

    private SoBitmap(Context context) {
        this(context, true);
    }

    private SoBitmap(Context context, boolean useExternalCache) {
        if (LOG) {
            Log.d(TAG, "New instance.");
        }
        this.context = context.getApplicationContext();
        defaultOps = new Options.FuzzyOptionsBuilder().build();
        requestMap = new ConcurrentHashMap<>();
        for (Class<? extends Hunter> cls : HUNTERS) {
            try {
                hunterSet.add(cls.newInstance());
            } catch (Exception ignore) {
            }
        }
        if (useExternalCache) {
            if (context.getExternalCacheDir() == null) {
                Log.w(TAG, "External storage invalid, use internal instead.");
                cacheDir = context.getCacheDir();
            } else {
                cacheDir = context.getExternalCacheDir();
            }
        } else {
            cacheDir = context.getCacheDir();
        }
    }

    /**
     * Set default display options, then you can just use the simple version of hunt.
     *
     * @param option the default display option you want SoBitmap use
     * @return SoBitmap single instance
     */
    public SoBitmap setDefaultOption(@NonNull Options option) {
        if (sInstance == null) {
            throw new IllegalStateException("SoBitmap has been shutdown, please get a new one.");
        }
        sInstance.defaultOps = option;
        return sInstance;
    }

    /**
     * Hunt bitmap use default display options.
     *
     * @param uri      Bitmap source
     * @param callback Callback to user {@link com.github.airk.tool.sobitmap.Callback}
     * @return true if hunt in process successful, false otherwise
     */
    public boolean hunt(@NonNull Uri uri, @NonNull Callback callback) {
        if (sInstance == null) {
            Log.e(TAG, "SoBitmap has been shutdown. No more request can be handled");
            return false;
        }
        return hunt(null, uri, sInstance.defaultOps, callback);
    }

    /**
     * Hunt bitmap with given tag, and use default display options.
     *
     * @param tag      Tag for cancel {@link #cancel(String)}
     * @param uri      Bitmap source
     * @param callback Callback to user {@link com.github.airk.tool.sobitmap.Callback}
     * @return true if hunt in process successful, false otherwise
     */
    public boolean hunt(String tag, @NonNull Uri uri, @NonNull Callback callback) {
        if (sInstance == null) {
            Log.e(TAG, "SoBitmap has been shutdown. No more request can be handled");
            return false;
        }
        return hunt(tag, uri, sInstance.defaultOps, callback);
    }

    /**
     * Hunt bitmap
     *
     * @param tag      for cancel request
     * @param uri      Bitmap source
     * @param options  Display options {@link com.github.airk.tool.sobitmap.Options}
     * @param callback Callback to user {@link com.github.airk.tool.sobitmap.Callback}
     * @return true if hunt in process successful, false otherwise
     */
    public boolean hunt(String tag, @NonNull Uri uri, @NonNull Options options, @NonNull Callback callback) {
        if (LOG) {
            Log.d(TAG, "hunt call.");
        }

        if (executor.isShutdown() || executor.isTerminated()) {
            Log.e(TAG, "SoBitmap has been shutdown. No more request can be handled");
            return false;
        }

        if (uri == Uri.EMPTY) {
            throw new IllegalArgumentException("Empty uri is not allowed, please check it through.");
        }

        Request request = generateRequest(tag, uri, options, callback);
        if (request == null) {
            Log.e(TAG, "Can handle " + uri.toString());
            return false;
        }

        if (LOG) {
            Log.d(TAG, "hunt called with: " + request.toString());
        }
        try {
            request.task = executor.submit(request);
        } catch (RejectedExecutionException ignore) {
            if (LOG) {
                Log.e(TAG, "Task rejected.");
            }
            return false;
        }
        if (LOG) {
            Log.d(TAG, "Task submitted with key: " + request.key);
        }
        requestMap.put(request.key, request);
        return true;
    }

    /**
     * Get bitmap result immediately and you really should not call this on the UI thread,
     * you can not either in the fact.
     *
     * @param tag Tag the request.
     * @param uri the bitmap's source.
     * @return Bitmap result
     * @throws InterruptedException this request maybe interrupt, please treat it rightly.
     */
    public Bitmap huntBlock(String tag, @NonNull Uri uri) throws InterruptedException {
        if (Util.checkMainThread()) {
            throw new RuntimeException("You never should call this on UI thread, please fix it.");
        }
        final CountDownLatch latch = new CountDownLatch(1);
        final Bitmap[] ret = {null};
        hunt(tag, uri, new Callback() {
            @Override
            public void onHunted(Bitmap bitmap, BitmapFactory.Options options) {
                ret[0] = bitmap;
                latch.countDown();
            }

            @Override
            public void onException(HuntException e) {
                latch.countDown();
            }
        });
        latch.await();
        return ret[0];
    }

    /**
     * Judge if we can handle this request, then produce the request instance
     */
    private Request generateRequest(String tag, Uri uri, Options options, Callback callback) {
        Hunter hunter = null;
        for (Hunter h : hunterSet) {
            if (h.canHandle(uri)) {
                hunter = h;
                break;
            }
        }
        if (hunter != null) {
            return new Request(context, tag, uri, options, callback, hunter, uiHandler, cacheDir);
        }
        return null;
    }

    /**
     * Cancel request with given tag
     *
     * @param tag Tag you have set for the request
     */
    public void cancel(String tag) {
        if (requestMap.containsKey(tag)) {
            Request request = requestMap.remove(tag);
            request.task.cancel(false);
            if (LOG) {
                Log.d(TAG, "Task " + request.key + "has been canceled.");
            }
        }
    }

    /**
     * Cancel all request in SoBitmap
     */
    public void cancelAll() {
        for (Map.Entry<String, Request> entry : requestMap.entrySet()) {
            entry.getValue().task.cancel(false);
        }
        requestMap.clear();
    }

    public void shutdown() {
        if (LOG) {
            Log.d(TAG, "SoBitmap Shutdown!");
        }
        executor.shutdownNow();
        sInstance = null;
    }

}
