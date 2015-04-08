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

package com.github.airk.tool.sobitmap.sample;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.airk.tool.sobitmap.Callback;
import com.github.airk.tool.sobitmap.HuntException;
import com.github.airk.tool.sobitmap.Options;
import com.github.airk.tool.sobitmap.SoBitmap;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by kevin on 15/4/7.
 */
public class ImageActivity extends ActionBarActivity implements AdapterView.OnItemSelectedListener {
    private final String TAG = "ImageActivity";

    @InjectView(R.id.source_type)
    Spinner sourceType;
    @InjectView(R.id.choose_result)
    Spinner chooseResult;
    @InjectView(R.id.max_input)
    EditText maxInput;
    @InjectView(R.id.max_output)
    EditText maxOutput;
    @InjectView(R.id.step)
    EditText step;
    @InjectView(R.id.quality_level)
    Spinner qualityLevel;
    @InjectView(R.id.max_size)
    EditText maxSize;
    @InjectView(R.id.compress_format)
    Spinner compressFormat;
    @InjectView(R.id.img)
    SquareImageView img;
    @InjectView(R.id.progress)
    ProgressBar progress;
    @InjectView(R.id.out_info)
    TextView outInfo;
    @InjectView(R.id.hunt_btn)
    Button huntBtn;

    public static final String[] IMGS = new String[]{
            "http://ww2.sinaimg.cn/large/d75e3906jw1eildxl8hygj20vy0jx0x4.jpg", //172094 168K
            "http://58.62.125.197:8080/uploadfiles/20130718014554879.jpg", //2480204 2M
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        ButterKnife.inject(this);

        step.setText("15");
        sourceType.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int vid = parent.getId();
        switch (vid) {
            case R.id.source_type:
                switch (position) {
                    case 0: //none
                        break;
                    case 1: //local file
                        new LocalFileTask(this, chooseResult).execute();
                        break;
                    case 2: //network stream
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.url_array));
                        chooseResult.setAdapter(adapter);
                        break;
                    case 3: //mediastore
                        new MediaStoreTask(this, chooseResult).execute();
                        break;
                    default:
                        break;
                }
                break;
            case R.id.choose_result:
                break;
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @OnClick(R.id.hunt_btn)
    void doHunt() {
        if (qualityLevel.getSelectedItemPosition() == 0 && TextUtils.isEmpty(maxOutput.getText().toString())) {
            Toast.makeText(this, "You should select one at least between Exact or Fuzzy", Toast.LENGTH_LONG).show();
            return;
        }
        int size = -1;
        Bitmap.CompressFormat format;
        if (!TextUtils.isEmpty(maxSize.getText())) {
            size = Integer.parseInt(maxSize.getText().toString());
        }
        switch (compressFormat.getSelectedItemPosition()) {
            case 0:
                format = Bitmap.CompressFormat.JPEG;
                break;
            case 1:
                format = Bitmap.CompressFormat.PNG;
                break;
            case 2:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    format = Bitmap.CompressFormat.WEBP;
                } else {
                    format = Bitmap.CompressFormat.JPEG;
                    Toast.makeText(this, "Your device api level is lower than ICS, can not use WEBP format," +
                            " we use JPEG instead for you.", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                format = Bitmap.CompressFormat.JPEG;
                break;
        }
        Options options;
        if (qualityLevel.getSelectedItemPosition() != 0) {
            Options.FuzzyOptionsBuilder build = new Options.FuzzyOptionsBuilder();
            if (size > 0) {
                build.maxSize(size);
            }
            options = build.format(format).build();
        } else {
            int maxInput_ = -1;
            int maxOutput_ = -1;
            int step_ = 15;
            if (!TextUtils.isEmpty(maxInput.getText().toString())) {
                maxInput_ = Integer.parseInt(maxInput.getText().toString());
            }
            if (!TextUtils.isEmpty(maxOutput.getText().toString())) {
                maxOutput_ = Integer.parseInt(maxOutput.getText().toString());
            }
            if (!TextUtils.isEmpty(step.getText().toString())) {
                step_ = Integer.parseInt(step.getText().toString());
            }
            Options.ExactOptionsBuilder builder = new Options.ExactOptionsBuilder();
            if (size > 0) {
                builder.maxSize(size);
            }
            if (maxInput_ > 0) {
                builder.maxInput(maxInput_);
            }
            if (maxOutput_ > 0) {
                builder.maxOutput(maxOutput_);
            }
            if (step_ > 0 && step_ < 100) {
                builder.step(step_);
            }
            options = builder.format(format).build();
        }
        int type = sourceType.getSelectedItemPosition();
        Uri target;
        String choose = (String) chooseResult.getAdapter().getItem(chooseResult.getSelectedItemPosition());
        switch (type) {
            case 1: //local file
                target = Uri.fromFile(new File(choose));
                break;
            case 2: //network
                target = Uri.parse(IMGS[chooseResult.getSelectedItemPosition()]);
                break;
            case 3: //mediastore
            default:
                target = Uri.parse(choose);
                break;
        }
        progress.setVisibility(View.VISIBLE);
        img.setImageResource(R.mipmap.ic_launcher);
        outInfo.setText("-");
        SoBitmap.getInstance(this).hunt(TAG, target, options, new Callback() {
            @Override
            public void onHunted(Bitmap bitmap, BitmapFactory.Options options) {
                progress.setVisibility(View.INVISIBLE);
                img.setImageBitmap(bitmap);
                outInfo.setText(String.format("W: %d, H: %d, M: %dKB", options.outWidth, options.outHeight, bitmap.getAllocationByteCount() / 1024));
            }

            @Override
            public void onException(HuntException e) {
                progress.setVisibility(View.INVISIBLE);
                outInfo.setText(e.getMessage());
            }
        });
    }

}
