package com.github.airk.tool.sobitmap;

import android.content.ContentResolver;
import android.net.Uri;

import java.io.File;

/**
 * Created by kevin on 15/3/24.
 * <p/>
 * Handle file request
 */
final class FileHunter extends Hunter {

    @Override
    boolean canHandle(Uri source) {
        return ContentResolver.SCHEME_FILE.equals(source.getScheme());
    }

    @Override
    File preCacheFile() {
        return new File(request.source.getPath());
    }

    @Override
    void cleanup(File cacheFile) {
        //no-op
    }

    @Override
    String tag() {
        return "FileHunter";
    }

}
