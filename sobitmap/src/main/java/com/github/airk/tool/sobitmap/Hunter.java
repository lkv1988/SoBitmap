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
    private boolean abort = false;

    public void hunt(Request request) {
        if (SoBitmap.LOG) {
            Log.d(SoBitmap.TAG, tag() + ":Pre-hunt call.");
        }
        this.request = request;
        abort = false;
        request.startAllMs = System.currentTimeMillis();
        cacheFile = preCacheFile();
        if (cacheFile == null || !cacheFile.exists()) {
            Log.e(SoBitmap.TAG, tag() + ": cache file error.");
            if (request.e == null) {
                request.e = new HuntException(HuntException.REASON_FILE_NOT_FOUND);
                request.onException(request.e);
            }
            return;
        }
        request.startDecodeMs = System.currentTimeMillis();
        decode();
    }

    private void decode() {
        request.recursionCount++;
        if (SoBitmap.LOG) {
            Log.d(SoBitmap.TAG, tag() + ": Decode call. " + request.recursionCount + " time, quality " + request.quality + "%.");
        }
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
                request.e = new HuntException(HuntException.REASON_CANT_DECODE);
                request.onException(request.e);
                cleanup(cacheFile);
                return;
            }
            if (abort) {
                request.onHunted(bitmap, bitmapOps);
                logTime();
                cleanup(cacheFile);
                return;
            }
            bitmap.compress(request.options.format, request.quality, os);
            bitmap.recycle();

            if (os.toByteArray().length / 1024 > request.options.maxOutput) {
                if (SoBitmap.LOG) {
                    Log.w(SoBitmap.TAG, tag() + ": Recursion! Reason: not small enough!");
                }
                int newQ = request.quality - request.options.qualityStep;
                if (newQ <= 0) {
                    if (SoBitmap.LOG) {
                        Log.w(SoBitmap.TAG, tag() + ": Abort! The quality is too low.");
                    }
                    abort = true;
                } else {
                    request.quality = newQ;
                }
                os.close();
                decode();
            } else {
                Bitmap ret = BitmapFactory.decodeStream(new ByteArrayInputStream(os.toByteArray()));
                request.e = null;
                request.onHunted(ret, bitmapOps);
                logTime();
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

    private void logTime() {
        if (!SoBitmap.LOG)
            return;
        long now = System.currentTimeMillis();
        long total = now - request.startAllMs;
        long decode = now - request.startDecodeMs;
        Log.d(SoBitmap.TAG, tag() + ": Bitmap hunting finished, cost " + total + " ms in total," +
                " and decoding cost " + decode + " ms.");
    }

    private Bitmap decodeBitmap(BitmapFactory.Options bitmapOps) {
        return BitmapFactory.decodeFile(cacheFile.getPath(), bitmapOps);
    }
}
