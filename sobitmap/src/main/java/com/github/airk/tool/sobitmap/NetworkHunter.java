package com.github.airk.tool.sobitmap;

import android.net.Uri;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by kevin on 15/3/24.
 * <p/>
 * Handle network request
 */
final class NetworkHunter extends Hunter {

    OkHttpClient client;

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
    boolean preHunt() {
        try {
            Request netReq = new Request.Builder().url(request.source.toString()).build();
            Response response = client.newCall(netReq).execute();
            request.is = response.body().byteStream();
        } catch (IOException ignore) {
            request.e = new HuntException(HuntException.REASON_IO_EXCEPTION);
            request.onException(request.e);
            return false;
        }
        return true;
    }

    @Override
    String tag() {
        return "NetworkHunter";
    }

    @Override
    DecodeType decodeType() {
        return DecodeType.INPUT_STREAM;
    }

}
