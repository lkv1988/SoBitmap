package com.github.airk.tool.sobitmap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by kevin on 15/3/24.
 * <p/>
 * Bitmap hunter, handle resize & quality & compress events.
 */
abstract class Hunter implements Runnable {
    Request request;

    protected Hunter() {
    }

    protected enum DecodeType {
        FILE,
        INPUT_STREAM,
    }

    abstract boolean canHandle(Uri source);

    abstract boolean preHunt();

    abstract String tag();

    abstract DecodeType decodeType();

    @Override
    public void run() {
        if (SoBitmap.LOG) {
            Log.d(tag(), "Pre-hunt call.");
        }
        request.startInMs = System.currentTimeMillis();
        if (preHunt()) {
            decode();
        }
    }

    private void decode() {
        if (SoBitmap.LOG) {
            Log.d(tag(), "decode call.");
        }
        request.recursionCount++;
        BitmapFactory.Options bitmapOps = new BitmapFactory.Options();
        bitmapOps.inJustDecodeBounds = true;

        long mark = -1;
        MarkableInputStream markIs = null;
        if (decodeType() == DecodeType.INPUT_STREAM) {
            markIs = new MarkableInputStream(request.is);
            request.is = markIs;
            mark = markIs.savePosition(65535);
        }
        if (decodeBitmap(bitmapOps) == null)
            return;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            Util.calculateInSampleSize(request.options.maxSize, request.options.maxSize,
                    bitmapOps.outWidth, bitmapOps.outHeight,
                    bitmapOps);
            if (decodeType() == DecodeType.INPUT_STREAM && mark != -1) {
                markIs.reset(mark);
            }
            Bitmap bitmap = decodeBitmap(bitmapOps);
            //TODO format set by user
            bitmap.compress(Bitmap.CompressFormat.JPEG, request.quality, os);
            bitmap.recycle();

            if (os.toByteArray().length / 1024 > request.options.maxOutput) {
                if (SoBitmap.LOG) {
                    Log.w(tag(), "Recursion! Reason: not small enough!");
                }
                request.quality -= request.options.qualityStep;
                os.close();
                decode();
            } else {
                Bitmap ret = BitmapFactory.decodeStream(new ByteArrayInputStream(os.toByteArray()));
                request.e = null;
                request.costInMs = System.currentTimeMillis() - request.startInMs;
                if (SoBitmap.LOG && request.recursionCount >= 0) {
                    Log.d(tag(), "Bitmap hunting finished, cost " + request.costInMs + " MS with recursion "
                            + request.recursionCount + " times.");
                }
                request.onHunted(ret);
            }
        } catch (OutOfMemoryError ignore) {
            if (SoBitmap.LOG) {
                Log.w(tag(), "Recursion! Reason: OOM!");
            }
            request.e = new HuntException(HuntException.REASON_OOM);
            request.quality -= request.options.qualityStep;
            decode();
        } catch (IOException ignore) {
            if (SoBitmap.LOG) {
                Log.w(tag(), "Recursion! Reason: IOException!");
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
        switch (decodeType()) {
            case FILE:
                return BitmapFactory.decodeFile(request.source.getPath(), bitmapOps);
            case INPUT_STREAM:
                return BitmapFactory.decodeStream(request.is, null, bitmapOps);
            default:
                request.onException(new HuntException(HuntException.REASON_UNSUPPORT_TYPE));
                return null;
        }
    }
}
