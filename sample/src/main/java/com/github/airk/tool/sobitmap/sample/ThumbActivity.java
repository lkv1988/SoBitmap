package com.github.airk.tool.sobitmap.sample;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.airk.tool.sobitmap.Callback;
import com.github.airk.tool.sobitmap.HuntException;
import com.github.airk.tool.sobitmap.SoBitmap;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by kevin on 15/4/5.
 */
public class ThumbActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<ArrayList<Uri>> {

    ThumbAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thumb);
        RecyclerView list = (RecyclerView) findViewById(R.id.list);
        list.setHasFixedSize(true);
        int span = (int) (getResources().getDisplayMetrics().widthPixels / TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                120f, getResources().getDisplayMetrics()));
        list.setLayoutManager(new GridLayoutManager(this, span));
        adapter = new ThumbAdapter(this);
        adapter.setData(null);
        list.setAdapter(adapter);
        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<ArrayList<Uri>> onCreateLoader(int id, Bundle args) {
        return new ThumbLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Uri>> loader, ArrayList<Uri> data) {
        adapter.setData(data);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<Uri>> loader) {
        adapter.setData(null);
    }

    static class ThumbAdapter extends RecyclerView.Adapter<ThumbAdapter.ThumbHolder> {

        LayoutInflater inflater;
        Picasso picasso;
        private ArrayList<Uri> data;
        SoBitmap soBitmap;

        ThumbAdapter(Context context) {
            inflater = LayoutInflater.from(context);
            picasso = Picasso.with(context);
            data = new ArrayList<>();
            soBitmap = SoBitmap.getInstance(context);
        }

        public void setData(ArrayList<Uri> data) {
            if (data != null) {
                this.data = new ArrayList<>(data);
            } else {
                this.data.clear();
            }
            notifyDataSetChanged();
        }

        @Override
        public ThumbHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = inflater.inflate(R.layout.item_thumb, parent, false);
            return new ThumbHolder(v);
        }

        @Override
        public void onBindViewHolder(final ThumbHolder holder, int position) {
            holder.info.setSelected(true);
            Uri uri = data.get(position);
            picasso.load(uri).fit().centerInside().into(holder.img);
            if (TextUtils.isEmpty(holder.info.getText().toString())) {
                holder.info.setText(uri.toString());
            }
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class ThumbHolder extends RecyclerView.ViewHolder {
            ImageView img;
            TextView info;

            public ThumbHolder(View itemView) {
                super(itemView);
                img = (ImageView) itemView.findViewById(R.id.img);
                info = (TextView) itemView.findViewById(R.id.info);
            }
        }

    }

    static class ThumbLoader extends AsyncTaskLoader<ArrayList<Uri>> {

        public ThumbLoader(Context context) {
            super(context);
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            forceLoad();
        }

        @Override
        public ArrayList<Uri> loadInBackground() {
            ArrayList<Uri> data = new ArrayList<>();
            ContentResolver cr = getContext().getContentResolver();
            Cursor c = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
            if (c != null) {
                c.moveToFirst();
                do {
                    int id = c.getInt(c.getColumnIndex(MediaStore.Images.Media._ID));
                    Uri u = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
                    data.add(u);
                } while (c.moveToNext());
                c.close();
                return data;
            } else {
                return null;
            }
        }
    }
}
