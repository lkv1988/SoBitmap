package com.github.airk.tool.sobitmap;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;

/**
 * Created by kevin on 15/4/5.
 */
final class MediaStoreHunter extends Hunter {
    @Override
    boolean canHandle(Uri source) {
        return (source.getScheme().equals(ContentResolver.SCHEME_CONTENT)
                && source.getAuthority().equals(MediaStore.AUTHORITY));
    }

    @Override
    File preCacheFile() {
        Uri uri = request.source;
        String path = null;
        File f = null;
        Cursor c = request.context.getContentResolver().query(uri, new String[]{MediaStore.MediaColumns.DATA}, null, null, null);
        if (c != null) {
            c.moveToFirst();
            path = c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA));
            c.close();
        }
        if (path != null) {
            f = new File(path);
        }
        return f;
    }

    @Override
    void cleanup(File cacheFile) {
        //no-op
    }

    @Override
    String tag() {
        return "MediaStoreHunter";
    }
}
