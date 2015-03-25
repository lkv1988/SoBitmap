package com.github.airk.tool.sobitmap.simple;

import android.graphics.Bitmap;
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
import com.github.airk.tool.sobitmap.SoBitmap;


public class MainActivity extends ActionBarActivity {

    String BIG_SIZE_URL = "http://www.photo0086.com/member/3337/pic/2011032108344734474.JPG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final View progressBar = findViewById(R.id.progress);
        final ImageView imageView = (ImageView) findViewById(R.id.image);
        SoBitmap.getInstance(this).hunt(Uri.parse(BIG_SIZE_URL), new Callback() {
            @Override
            public void onHunted(final Bitmap bitmap) {
                progressBar.setVisibility(View.GONE);
                imageView.setImageBitmap(bitmap);
            }

            @Override
            public void onException(HuntException e) {

            }
        });
    }

}
