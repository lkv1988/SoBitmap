package com.github.airk.tool.sobitmap;

import android.content.Context;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/**
 * Created by kevin on 15/3/24.
 * <p/>
 * SoBitmap
 */
public final class SoBitmap {
    private static final String TAG = "SoBitmap";
    private static boolean debugMode = true;
    static boolean LOG = Log.isLoggable(TAG, Log.VERBOSE) || debugMode;

    private volatile static SoBitmap INSTANCE;
    private Options defaultOps;
    private static ExecutorService executor = Executors.newSingleThreadExecutor();
    private LinkedHashSet<Hunter> hunterSet = new LinkedHashSet<>();

    private static final long DEFAULT_MAX_INPUT = 5 * 1024 * 1024; // 5MB
    private static final long DEFAULT_MAX_OUTPUT = 300 * 1024; //300k
    private static final int DEFAULT_QUALITY_STEP = 15;

    private ConcurrentHashMap<String, Request> requestMap;

    private static final List<Class<? extends Hunter>> HUNTERS = Arrays.asList(
            FileHunter.class,
            NetworkHunter.class
    );

    public static SoBitmap getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SoBitmap.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SoBitmap(context);
                }
            }
        }
        return INSTANCE;
    }

    private SoBitmap(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        defaultOps = new Options(DEFAULT_MAX_INPUT, DEFAULT_MAX_OUTPUT, Math.max(dm.heightPixels, dm.widthPixels) * 2, DEFAULT_QUALITY_STEP);

        requestMap = new ConcurrentHashMap<>();
        for (Class<? extends Hunter> cls : HUNTERS) {
            try {
                hunterSet.add(cls.newInstance());
            } catch (InstantiationException | IllegalAccessException ignore) {
            }
        }
    }

    public SoBitmap setDefaultOption(Options option) {
        INSTANCE.defaultOps = option;
        return INSTANCE;
    }

    public Options getDefaultOption() {
        return INSTANCE.defaultOps;
    }

    public boolean hunt(Uri uri, Callback callback) {
        return hunt(TAG, uri, INSTANCE.defaultOps, callback);
    }

    public boolean hunt(String tag, Uri uri, Callback callback) {
        return hunt(tag, uri, INSTANCE.defaultOps, callback);
    }

    public boolean hunt(String tag, Uri uri, Options options, Callback callback) {
        if (LOG) {
            Log.d(TAG, "hunt call.");
        }

        if (executor.isShutdown() || executor.isTerminated()) {
            Log.e(TAG, "SoBitmap has been shutdown. No more request can be handled");
            return false;
        }

        if (uri == null || uri == Uri.EMPTY) {
            throw new IllegalArgumentException("Empty uri.");
        }
        if (options == null) {
            throw new IllegalArgumentException("Wrong options.");
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
            request.task = executor.submit(request.hunter);
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

    private Request generateRequest(String tag, Uri uri, Options options, Callback callback) {
        Hunter hunter = null;
        for (Hunter h : hunterSet) {
            if (h.canHandle(uri)) {
                hunter = h;
                break;
            }
        }
        if (hunter != null) {
            Request request = new Request(tag, uri, options, callback, hunter);
            hunter.request = request;
            return request;
        }
        return null;
    }

    public void cancel(String tag) {
        if (requestMap.containsKey(tag)) {
            Request request = requestMap.remove(tag);
            request.task.cancel(true);
            if (LOG) {
                Log.d(TAG, "Task " + request.key + "has been canceled.");
            }
        }
    }

    public void cancelAll() {
        for (Map.Entry<String, Request> entry : requestMap.entrySet()) {
            entry.getValue().task.cancel(true);
        }
        requestMap.clear();
    }

    public void shutdown() {
        executor.shutdown();
    }

}
