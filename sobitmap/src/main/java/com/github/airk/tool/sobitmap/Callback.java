package com.github.airk.tool.sobitmap;

import android.graphics.Bitmap;

/**
 * Created by kevin on 15/3/24.
 * <p/>
 * Hunter's callback
 */
public interface Callback {
    public void onHunted(Bitmap bitmap);

    public void onException(HuntException e);
}
