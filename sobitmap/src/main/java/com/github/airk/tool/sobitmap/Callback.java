package com.github.airk.tool.sobitmap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by kevin on 15/3/24.
 * <p/>
 * Hunter's callback
 */
public interface Callback {
    public void onHunted(Bitmap bitmap, BitmapFactory.Options options);

    public void onException(HuntException e);
}
