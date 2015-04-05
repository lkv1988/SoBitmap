package com.github.airk.tool.sobitmap.sample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.airk.tool.sobitmap.Callback;
import com.github.airk.tool.sobitmap.HuntException;
import com.github.airk.tool.sobitmap.SoBitmap;

/**
 * Created by kevin on 15/4/5.
 */
public class NormalActivity extends ActionBarActivity {
    String BIG_SIZE_URL = "http://www.photo0086.com/member/3337/pic/2011032108344734474.JPG";
//    String BIG_SIZE_URL = "http://f.hiphotos.baidu.com/image/w%3D2048/sign=527f77af184c510faec4e51a5461242d/d1a20cf431adcbefa332d761aeaf2edda3cc9f57.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal);
        final View progressBar = findViewById(R.id.progress);
        final TextView textView = (TextView) findViewById(R.id.exception_info);
        final ImageView imageView = (ImageView) findViewById(R.id.image);

        Uri uri = Uri.parse(BIG_SIZE_URL);
        SoBitmap.getInstance(this).hunt(uri, new Callback() {

            @Override
            public void onHunted(Bitmap bitmap, BitmapFactory.Options options) {
                progressBar.setVisibility(View.GONE);
                imageView.setImageBitmap(bitmap);
                textView.setText("sample:" + options.inSampleSize + " W:" + options.outWidth + " H:" + options.outHeight);
            }

            @Override
            public void onException(HuntException e) {
                textView.setText(e.getMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SoBitmap.getInstance(this).shutdown();
    }
}
