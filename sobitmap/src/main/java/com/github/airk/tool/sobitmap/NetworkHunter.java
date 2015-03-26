package com.github.airk.tool.sobitmap;

import android.net.Uri;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by kevin on 15/3/24.
 * <p/>
 * Handle network request
 */
final class NetworkHunter extends Hunter {
    private OkHttpClient client;

    NetworkHunter() {
        super();
        client = new OkHttpClient();
        client.setConnectTimeout(15000, TimeUnit.MILLISECONDS);
        client.setReadTimeout(20000, TimeUnit.MILLISECONDS);
        client.setWriteTimeout(20000, TimeUnit.MILLISECONDS);
    }

    @Override
    boolean canHandle(Uri source) {
        String scheme = source.getScheme();
        return scheme.equals("http") || scheme.equals("https");
    }

    @Override
    File preCacheFile() {
        File file = null;
        try {
            Request netReq = new Request.Builder().url(request.source.toString()).build();
            Response response = client.newCall(netReq).execute();
            if (SoBitmap.LOG) {
                Log.d(SoBitmap.TAG, tag() + ": Downloading...");
            }
            file = new File(request.cacheDir, request.tag);
            Util.inputStreamToFile(file, response.body().byteStream());
            if (SoBitmap.LOG) {
                Log.d(SoBitmap.TAG, tag() + ": Downloaded to file -> " + file.getAbsolutePath());
            }
        } catch (IOException ignore) {
            request.e = new HuntException(HuntException.REASON_IO_EXCEPTION);
            request.onException(request.e);
        }
        return file;
    }

    @Override
    void cleanup(File cacheFile) {
        cacheFile.delete();
    }

    @Override
    String tag() {
        return "NetworkHunter";
    }

}
