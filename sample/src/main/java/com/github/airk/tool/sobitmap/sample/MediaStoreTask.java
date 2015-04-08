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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kevin on 15/4/8.
 * <p/>
 * AsyncTask for loading MediaStore images on device
 */
public class MediaStoreTask extends AsyncTask<Void, Void, List<String>> {
    ProgressDialog progressDialog;
    final Activity activity;
    final Spinner target;

    public MediaStoreTask(Activity activity, Spinner target) {
        this.activity = activity;
        this.target = target;
        progressDialog = new ProgressDialog(activity);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.show();
    }

    @Override
    protected void onPostExecute(List<String> strings) {
        super.onPostExecute(strings);
        progressDialog.dismiss();
        if (strings != null && target != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, strings);
            target.setAdapter(adapter);
        }
    }

    @Override
    protected List<String> doInBackground(Void... params) {
        List<String> data = new ArrayList<>();
        ContentResolver cr = activity.getContentResolver();
        Cursor c = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
            do {
                int id = c.getInt(c.getColumnIndex(MediaStore.Images.Media._ID));
                Uri u = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
                data.add(u.toString());
            } while (c.moveToNext());
            c.close();
            return data;
        } else {
            return null;
        }
    }
}
