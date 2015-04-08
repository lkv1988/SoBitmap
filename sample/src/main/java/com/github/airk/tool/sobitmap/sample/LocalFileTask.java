package com.github.airk.tool.sobitmap.sample;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kevin on 15/4/8.
 * <p/>
 * AsyncTask for loading local file on device
 */
public class LocalFileTask extends AsyncTask<Void, Void, List<String>> {
    ProgressDialog progressDialog;
    final Activity activity;
    final Spinner target;

    public LocalFileTask(Activity activity, Spinner target) {
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
        File dir = new File(Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/Camera");
        if (dir.exists()) {
            List<String> data = new ArrayList<>();
            for (File f : dir.listFiles()) {
                if (f.isFile()) {
                    data.add(f.getAbsolutePath());
                }
            }
            return data;
        }
        return null;
    }
}
