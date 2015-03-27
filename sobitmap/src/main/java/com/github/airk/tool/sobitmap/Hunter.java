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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Created by kevin on 15/3/24.
 * <p/>
 * Bitmap hunt, handle resize & quality & compress events.
 */
abstract class Hunter {
    protected Request request;

    protected Hunter() {
    }

    /**
     * whether the Hunter can handle this uri
     */
    abstract boolean canHandle(Uri source);

    /**
     * pre-hunt, in case of some error and equip necessary child for request
     */
    abstract File preCacheFile();

    /**
     * hunter cleanup, such as clean temp cache file
     */
    abstract void cleanup(File cacheFile);

    /**
     * the hunter's tag, just for Log now
     */
    abstract String tag();

    private File cacheFile;

    public void hunt(Request request) {
        if (SoBitmap.LOG) {
            Log.d(SoBitmap.TAG, tag() + ":Pre-hunt call.");
        }
        this.request = request;
        request.startInMs = System.currentTimeMillis();
        cacheFile = preCacheFile();
        if (cacheFile == null || !cacheFile.exists()) {
            Log.e(SoBitmap.TAG, tag() + ": cache file error.");
            request.onException(new HuntException(HuntException.REASON_FILE_NOT_FOUND));
            return;
        }
        decode();
    }

    private void decode() {
        if (SoBitmap.LOG) {
            Log.d(SoBitmap.TAG, tag() + ": decode call.");
        }
        request.recursionCount++;
        BitmapFactory.Options bitmapOps = new BitmapFactory.Options();
        bitmapOps.inJustDecodeBounds = true;

        decodeBitmap(bitmapOps);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            Util.calculateInSampleSize(request.options.maxSize, request.options.maxSize,
                    bitmapOps.outWidth, bitmapOps.outHeight,
                    bitmapOps);
            Bitmap bitmap = decodeBitmap(bitmapOps);
            if (bitmap == null) {
                request.onException(new HuntException(HuntException.REASON_CANT_DECODE));
                return;
            }
            //TODO format set by user and WEBP bug fix
            bitmap.compress(Bitmap.CompressFormat.JPEG, request.quality, os);
            bitmap.recycle();

            if (os.toByteArray().length / 1024 > request.options.maxOutput) {
                if (SoBitmap.LOG) {
                    Log.w(SoBitmap.TAG, tag() + ": Recursion! Reason: not small enough!");
                }
                request.quality -= request.options.qualityStep;
                os.close();
                decode();
            } else {
                Bitmap ret = BitmapFactory.decodeStream(new ByteArrayInputStream(os.toByteArray()));
                request.e = null;
                request.costInMs = System.currentTimeMillis() - request.startInMs;
                if (SoBitmap.LOG && request.recursionCount >= 0) {
                    Log.d(SoBitmap.TAG, tag() + ": Bitmap hunting finished, cost " + request.costInMs + " MS with recursion "
                            + request.recursionCount + " times.");
                }
                request.onHunted(ret, bitmapOps);
                cleanup(cacheFile);
            }
        } catch (OutOfMemoryError ignore) {
            if (SoBitmap.LOG) {
                Log.w(SoBitmap.TAG, tag() + ": Recursion! Reason: OOM!");
            }
            request.e = new HuntException(HuntException.REASON_OOM);
            request.quality -= request.options.qualityStep;
            decode();
        } catch (IOException ignore) {
            if (SoBitmap.LOG) {
                Log.w(SoBitmap.TAG, tag() + ": Recursion! Reason: IOException!");
            }
            request.e = new HuntException(HuntException.REASON_IO_EXCEPTION);
            decode();
        } finally {
            try {
                os.close();
            } catch (IOException ignore) {
            }
        }
    }

    private Bitmap decodeBitmap(BitmapFactory.Options bitmapOps) {
        return BitmapFactory.decodeFile(cacheFile.getPath(), bitmapOps);
    }
}
