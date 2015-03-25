package com.github.airk.tool.sobitmap;

import android.content.ContentResolver;
import android.net.Uri;
import android.util.Log;

import java.io.File;

/**
 * Created by kevin on 15/3/24.
 * <p/>
 * Handle file request
 */
final class FileHunter extends Hunter {
    private static final String TAG = "FileHunter";

    @Override
    boolean canHandle(Uri source) {
        return ContentResolver.SCHEME_FILE.equals(source.getScheme());
    }

    @Override
    boolean preHunt() {
        File file = new File(request.source.getPath());
        if (!file.exists()) {
            request.e = new HuntException(HuntException.REASON_FILE_NOT_FOUND);
            if (SoBitmap.LOG) {
                Log.e(TAG, request.e.getMessage());
            }
            request.onException(request.e);
            return false;
        }
        if (file.length() > request.options.maxInput * 1000) {
            request.e = new HuntException(HuntException.REASON_TOO_LARGE);
            if (SoBitmap.LOG) {
                Log.e(TAG, request.e.getMessage());
            }
            request.onException(request.e);
            return false;
        }
        return true;
    }

    @Override
    String tag() {
        return "FileHunter";
    }

    @Override
    DecodeType decodeType() {
        return DecodeType.FILE;
    }


}
