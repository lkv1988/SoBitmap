package com.github.airk.tool.sobitmap.simple;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.airk.tool.sobitmap.Callback;
import com.github.airk.tool.sobitmap.HuntException;
import com.github.airk.tool.sobitmap.Options;
import com.github.airk.tool.sobitmap.SoBitmap;

import java.io.File;


public class MainActivity extends ActionBarActivity {

    String BIG_SIZE_URL = "http://www.photo0086.com/member/3337/pic/2011032108344734474.JPG";
//    String BIG_SIZE_URL = "http://f.hiphotos.baidu.com/image/w%3D2048/sign=527f77af184c510faec4e51a5461242d/d1a20cf431adcbefa332d761aeaf2edda3cc9f57.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final View progressBar = findViewById(R.id.progress);
        final TextView textView = (TextView) findViewById(R.id.exception_info);
        final ImageView imageView = (ImageView) findViewById(R.id.image);

        Options options = new Options.Builder()
                .maxOutput(50)
                .maxSize(1024)
                .build();
        SoBitmap.getInstance(this).setDefaultOption(options);
        Uri uri = Uri.parse(BIG_SIZE_URL);
//        Uri uri = Uri.fromFile(new File("/sdcard/DCIM/Camera/IMG_20150321_173922.jpg"));
        SoBitmap.getInstance(this).hunt(uri, new Callback() {

            @Override
            public void onHunted(Bitmap bitmap, BitmapFactory.Options options) {
                progressBar.setVisibility(View.GONE);
                imageView.setImageBitmap(bitmap);
                textView.setText("sample:" + options.inSampleSize + " W:" + options.outWidth + " H:" + options.outHeight + " :" + bitmap.getAllocationByteCount() / 1000);
            }

            @Override
            public void onException(HuntException e) {
                textView.setText(e.toString());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SoBitmap.getInstance(this).shutdown();
    }
}
