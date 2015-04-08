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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.airk.tool.sobitmap.Callback;
import com.github.airk.tool.sobitmap.HuntException;
import com.github.airk.tool.sobitmap.Options;
import com.github.airk.tool.sobitmap.SoBitmap;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by kevin on 15/4/8.
 */
public class SoBitmapTest extends ActivityInstrumentationTestCase2<ImageActivity> {
    public SoBitmapTest() {
        super(ImageActivity.class);
    }

    private static final String TAG = "SoBitmapTest";

    Activity activity;
    Options smallOps;

    ImageView img;
    TextView tv;
    ScrollView scrollView;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        activity = getActivity();
        int maxSize = activity.getResources().getDisplayMetrics().widthPixels;
        smallOps = new Options.FuzzyOptionsBuilder().format(Bitmap.CompressFormat.JPEG)
                .level(Options.QualityLevel.LOW).maxSize(maxSize / 2).build();
        img = (ImageView) activity.findViewById(R.id.img);
        tv = (TextView) activity.findViewById(R.id.out_info);
        scrollView = (ScrollView) activity.findViewById(R.id.scroll);
    }

    class BlackHoleCallback implements Callback {

        @Override
        public void onHunted(Bitmap bitmap, BitmapFactory.Options options) {
        }

        @Override
        public void onException(HuntException e) {
        }
    }

    static class TestCallback implements Callback {
        final ImageView img;
        final TextView tv;
        Hook hook;

        TestCallback(ImageView img, TextView tv) {
            this.img = img;
            this.tv = tv;
        }

        public void setHook(Hook hook) {
            this.hook = hook;
        }

        interface Hook {
            void onHuntHook();

            void onExceptionHook();
        }

        @Override
        public void onHunted(Bitmap bitmap, BitmapFactory.Options options) {
            assertNotNull(bitmap);
            img.setImageBitmap(bitmap);
            tv.setText(String.format("W: %d, H: %d, M: %dKB", options.outWidth, options.outHeight, bitmap.getAllocationByteCount() / 1024));
            hook.onHuntHook();
        }

        @Override
        public void onException(HuntException e) {
            hook.onExceptionHook();
        }
    }

    public void testShutDown() throws Exception {
        SoBitmap soBitmap = SoBitmap.getInstance(activity);
        soBitmap.shutdown();
        assertFalse(soBitmap.hunt(Uri.parse(ImageActivity.IMGS[0]), new BlackHoleCallback()));
    }

    public void testCustomSingleton() throws Exception {
        SoBitmap soBitmap = SoBitmap.setInstanceByBuilder(activity, new SoBitmap.Builder());
        soBitmap.shutdown();

        soBitmap = SoBitmap.setInstanceByBuilder(activity, new SoBitmap.Builder().setUseExternalCache(true));
        soBitmap.shutdown();

        soBitmap = SoBitmap.setInstanceByBuilder(activity, new SoBitmap.Builder().setUseExternalCache(false));
        soBitmap.shutdown();
    }

    public void testExactOption() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final LocalFileTask task = new LocalFileTask(activity, null);
        setCallbackForAsyncTask(task, new AsyncCallback() {
            @Override
            public void onCallback(Object ret) {
                latch.countDown();
                assertTrue(ret instanceof List);
                List<String> result = (List<String>) ret;
                /** test device must have at least one <External storage>/DCIM/Camera picture. */
                assertNotNull(result);
                assertTrue(result.size() > 0);
                Options.ExactOptionsBuilder builder = new Options.ExactOptionsBuilder();
                builder.step(10)
                        .format(Bitmap.CompressFormat.JPEG)
                        .maxOutput(200)
                        .maxInput(10 * 1000)
                        .maxSize(5000);
                SoBitmap.getInstance(activity).hunt(TAG, Uri.fromFile(new File(result.get(0))), builder.build(), new BlackHoleCallback());
            }
        });
        task.execute();
        latch.await();
    }

    public void testFuzzyOption() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final LocalFileTask task = new LocalFileTask(activity, null);
        setCallbackForAsyncTask(task, new AsyncCallback() {
            @Override
            public void onCallback(Object ret) {
                latch.countDown();
                assertTrue(ret instanceof List);
                List<String> result = (List<String>) ret;
                assertNotNull(result);
                assertTrue(result.size() > 0);
                Options.FuzzyOptionsBuilder builder = new Options.FuzzyOptionsBuilder();
                builder.maxSize(5000)
                        .format(Bitmap.CompressFormat.PNG)
                        .level(Options.QualityLevel.HIGH);
                SoBitmap.getInstance(activity).hunt(TAG, Uri.fromFile(new File(result.get(0))), builder.build(), new BlackHoleCallback());
            }
        });
        task.execute();
        latch.await();
    }

    public void testLocalFile() throws Exception {
        scrollToBottom();
        final List<String> ret = new LocalFileTask(activity, null).execute().get();
        assertNotNull(ret);
        final int testSize = ret.size() > 20 ? 20 : ret.size();
        final CountDownLatch latch = new CountDownLatch(testSize);
        final AtomicInteger counter = new AtomicInteger(0);

        SoBitmap.getInstance(activity).setDefaultOption(smallOps);

        final TestCallback callback = new TestCallback(img, tv);
        callback.setHook(
                new TestCallback.Hook() {
                    @Override
                    public void onHuntHook() {
                        latch.countDown();
                        int count = counter.incrementAndGet();
                        if (count <= testSize - 1) {
                            SoBitmap.getInstance(activity).hunt(Uri.fromFile(new File(ret.get(count))), callback);
                        }
                    }

                    @Override
                    public void onExceptionHook() {
                        assertEquals(1, 2);
                    }
                });
        SoBitmap.getInstance(activity).hunt(Uri.fromFile(new File(ret.get(counter.get()))), callback);
        latch.await();
    }

    private void scrollToBottom() {
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    public void testNetworkStream() throws Exception {
        scrollToBottom();
        final CountDownLatch latch = new CountDownLatch(ImageActivity.IMGS.length);
        final AtomicInteger counter = new AtomicInteger(0);
        final TestCallback callback = new TestCallback(img, tv);
        callback.setHook(new TestCallback.Hook() {
            @Override
            public void onHuntHook() {
                latch.countDown();
                int count = counter.incrementAndGet();
                if (count <= ImageActivity.IMGS.length - 1) {
                    SoBitmap.getInstance(activity).hunt(Uri.parse(ImageActivity.IMGS[count]), callback);
                }
            }

            @Override
            public void onExceptionHook() {
                assertEquals(1, 2);
            }
        });
        SoBitmap.getInstance(activity).hunt(Uri.parse(ImageActivity.IMGS[counter.get()]), callback);
        latch.await();
    }

    public void testMediaStore() throws Exception {
        scrollToBottom();
        final List<String> result = new MediaStoreTask(activity, null).execute().get();
        assertNotNull(result);
        final int testCount = Math.min(20, result.size());
        final CountDownLatch latch = new CountDownLatch(testCount);
        final AtomicInteger counter = new AtomicInteger(0);
        SoBitmap.getInstance(activity).setDefaultOption(smallOps);
        final TestCallback callback = new TestCallback(img, tv);
        callback.setHook(new TestCallback.Hook() {
            @Override
            public void onHuntHook() {
                latch.countDown();
                int count = counter.incrementAndGet();
                if (count <= testCount - 1) {
                    SoBitmap.getInstance(activity).hunt(Uri.parse(result.get(count)), callback);
                }
            }

            @Override
            public void onExceptionHook() {
                assertEquals(1, 2);
            }
        });
        SoBitmap.getInstance(activity).hunt(Uri.parse(result.get(counter.get())), callback);
        latch.await();
    }

    public void testInputLimitWithLocal() throws Exception {
        List<String> result = new LocalFileTask(activity, null).execute().get();
        assertNotNull(result);
        assertTrue(result.size() > 0);
        final CountDownLatch latch = new CountDownLatch(1);

        Options.ExactOptionsBuilder builder = new Options.ExactOptionsBuilder();
        builder.maxOutput(1).maxInput(1);

        SoBitmap.getInstance(activity).hunt(TAG, Uri.fromFile(new File(result.get(0))), builder.build(), new Callback() {
            @Override
            public void onHunted(Bitmap bitmap, BitmapFactory.Options options) {
                assertEquals(1, 2);
            }

            @Override
            public void onException(HuntException e) {
                assertEquals(e.getReason(), HuntException.REASON_TOO_LARGE);
                latch.countDown();
            }
        });
        latch.await();
    }

    public void testInputLimitWithNetwork() throws Exception {
        Uri target = Uri.parse(ImageActivity.IMGS[0]);
        final CountDownLatch latch = new CountDownLatch(1);

        Options.ExactOptionsBuilder builder = new Options.ExactOptionsBuilder();
        builder.maxOutput(1).maxInput(1);

        SoBitmap.getInstance(activity).hunt(TAG, target, builder.build(), new Callback() {
            @Override
            public void onHunted(Bitmap bitmap, BitmapFactory.Options options) {
                assertEquals(1, 2);
            }

            @Override
            public void onException(HuntException e) {
                assertEquals(e.getReason(), HuntException.REASON_TOO_LARGE);
                latch.countDown();
            }
        });
        latch.await();
    }

    public void testInputLimitWithMediaStore() throws Exception {
    }

    public void testOutputLimitWithXXX() throws Exception {
        //no-op just for now util I know how to calculate a bitmap memory size correctly
    }

    public void testStepWithLocal() throws Exception {
    }

    public void testQualityLevelWithLocal() throws Exception {
    }

    public void testMaxSizeLimitWithLocal() throws Exception {
    }

    public void testCompressFormatWithLocal() throws Exception {
    }

    public void testTooLargeWithNetwork() throws Exception {
    }

    public void testNotFoundWithLocal() throws Exception {
    }

    public void testBlockBitmapWithLocal() throws Exception {
    }

    private void setCallbackForAsyncTask(AsyncTask task, AsyncCallback callback) {
        try {
            Method m = task.getClass().getDeclaredMethod("setCallback", new Class[]{AsyncCallback.class});
            m.setAccessible(true);
            m.invoke(task, callback);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
