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
    private static final int MAX_LOAD_COUNT = 20;

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
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    @Override
    public void tearDown() throws Exception {
        SoBitmap.getInstance(activity).shutdown();
        super.tearDown();
    }

    class BlackHoleCallback implements Callback {

        @Override
        public void onHunted(Bitmap bitmap, BitmapFactory.Options options) {
        }

        @Override
        public void onException(HuntException e) {
        }
    }

    class TestCallback implements Callback {

        @Override
        public void onHunted(Bitmap bitmap, BitmapFactory.Options options) {
            assertNotNull(bitmap);
            img.setImageBitmap(bitmap);
            tv.setText(String.format("W: %d, H: %d, M: %dKB", options.outWidth, options.outHeight, bitmap.getAllocationByteCount() / 1024));
        }

        @Override
        public void onException(HuntException e) {
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
        List<String> result = new LocalFileTask(activity, null).execute().get();
        /** test device must have at least one <External storage>/DCIM/Camera picture. */
        assertNotNull(result);
        assertTrue(result.size() > 0);
        Options.ExactOptionsBuilder builder = new Options.ExactOptionsBuilder();
        builder.step(10)
                .format(Bitmap.CompressFormat.JPEG)
                .maxOutput(200)
                .maxInput(10 * 1000)
                .maxSize(5000);
        SoBitmap.getInstance(activity).hunt(TAG, Uri.fromFile(new File(result.get(0))),
                builder.build(), new TestCallback() {
                    @Override
                    public void onHunted(Bitmap bitmap, BitmapFactory.Options options) {
                        super.onHunted(bitmap, options);
                        latch.countDown();
                    }

                    @Override
                    public void onException(HuntException e) {
                        super.onException(e);
                        assertEquals(1, 2);
                    }
                });
        latch.await();
    }

    public void testFuzzyOption() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        List<String> result = new LocalFileTask(activity, null).execute().get();
        assertNotNull(result);
        assertTrue(result.size() > 0);
        Options.FuzzyOptionsBuilder builder = new Options.FuzzyOptionsBuilder();
        builder.maxSize(5000)
                .format(Bitmap.CompressFormat.PNG)
                .level(Options.QualityLevel.HIGH);
        SoBitmap.getInstance(activity).hunt(TAG, Uri.fromFile(new File(result.get(0))),
                builder.build(), new TestCallback() {
                    @Override
                    public void onHunted(Bitmap bitmap, BitmapFactory.Options options) {
                        super.onHunted(bitmap, options);
                        latch.countDown();
                    }

                    @Override
                    public void onException(HuntException e) {
                        super.onException(e);
                        assertEquals(1, 2);
                    }
                });
        latch.await();
    }

    public void testLocalFile() throws Exception {
        final List<String> ret = new LocalFileTask(activity, null).execute().get();
        assertNotNull(ret);
        assertTrue(ret.size() > 0);
        final int testSize = Math.min(MAX_LOAD_COUNT, ret.size());
        final CountDownLatch latch = new CountDownLatch(testSize);
        final AtomicInteger counter = new AtomicInteger(0);

        SoBitmap.getInstance(activity).setDefaultOption(smallOps);
        SoBitmap.getInstance(activity).hunt(Uri.fromFile(new File(ret.get(counter.get()))), new TestCallback() {
            @Override
            public void onHunted(Bitmap bitmap, BitmapFactory.Options options) {
                super.onHunted(bitmap, options);
                latch.countDown();
                int count = counter.incrementAndGet();
                if (count <= testSize - 1) {
                    SoBitmap.getInstance(activity).hunt(Uri.fromFile(new File(ret.get(count))), this);
                }
            }

            @Override
            public void onException(HuntException e) {
                super.onException(e);
                assertEquals(1, 2);
            }
        });
        latch.await();
    }

    public void testNetworkStream() throws Exception {
        final CountDownLatch latch = new CountDownLatch(ImageActivity.IMGS.length);
        final AtomicInteger counter = new AtomicInteger(0);
        SoBitmap.getInstance(activity).hunt(Uri.parse(ImageActivity.IMGS[counter.get()]), new TestCallback() {
            @Override
            public void onHunted(Bitmap bitmap, BitmapFactory.Options options) {
                super.onHunted(bitmap, options);
                latch.countDown();
                int count = counter.incrementAndGet();
                if (count <= ImageActivity.IMGS.length - 1) {
                    SoBitmap.getInstance(activity).hunt(Uri.parse(ImageActivity.IMGS[count]), this);
                }
            }

            @Override
            public void onException(HuntException e) {
                super.onException(e);
                assertEquals(1, 2);
            }
        });
        latch.await();
    }

    public void testMediaStore() throws Exception {
        final List<String> result = new MediaStoreTask(activity, null).execute().get();
        assertNotNull(result);
        final int testCount = Math.min(MAX_LOAD_COUNT, result.size());
        final CountDownLatch latch = new CountDownLatch(testCount);
        final AtomicInteger counter = new AtomicInteger(0);
        SoBitmap.getInstance(activity).setDefaultOption(smallOps);
        SoBitmap.getInstance(activity).hunt(Uri.parse(result.get(counter.get())), new TestCallback() {
            @Override
            public void onHunted(Bitmap bitmap, BitmapFactory.Options options) {
                super.onHunted(bitmap, options);
                latch.countDown();
                int count = counter.incrementAndGet();
                if (count <= testCount - 1) {
                    SoBitmap.getInstance(activity).hunt(Uri.parse(result.get(count)), this);
                }
            }

            @Override
            public void onException(HuntException e) {
                super.onException(e);
                assertEquals(1, 2);
            }
        });
        latch.await();
    }

    public void testInputLimitWithLocal() throws Exception {
        List<String> result = new LocalFileTask(activity, null).execute().get();
        assertNotNull(result);
        assertTrue(result.size() > 0);
        final CountDownLatch latch = new CountDownLatch(1);

        Options.ExactOptionsBuilder builder = new Options.ExactOptionsBuilder();
        builder.maxOutput(1).maxInput(1);

        SoBitmap.getInstance(activity).hunt(TAG, Uri.fromFile(new File(result.get(0))), builder.build(), new TestCallback() {
            @Override
            public void onHunted(Bitmap bitmap, BitmapFactory.Options options) {
                super.onHunted(bitmap, options);
                assertEquals(1, 2);
            }

            @Override
            public void onException(HuntException e) {
                super.onException(e);
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

        SoBitmap.getInstance(activity).hunt(TAG, target, builder.build(), new TestCallback() {
            @Override
            public void onHunted(Bitmap bitmap, BitmapFactory.Options options) {
                super.onHunted(bitmap, options);
                assertEquals(1, 2);
            }

            @Override
            public void onException(HuntException e) {
                super.onException(e);
                assertEquals(e.getReason(), HuntException.REASON_TOO_LARGE);
                latch.countDown();
            }
        });
        latch.await();
    }

    public void testInputLimitWithMediaStore() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        List<String> result = new MediaStoreTask(activity, null).execute().get();
        assertNotNull(result);
        assertTrue(result.size() > 0);

        Options.ExactOptionsBuilder builder = new Options.ExactOptionsBuilder();
        builder.maxOutput(1).maxInput(1);

        SoBitmap.getInstance(activity).hunt(TAG, Uri.parse(result.get(0)), builder.build(), new TestCallback() {
            @Override
            public void onHunted(Bitmap bitmap, BitmapFactory.Options options) {
                super.onHunted(bitmap, options);
                assertEquals(1, 2);
            }

            @Override
            public void onException(HuntException e) {
                super.onException(e);
                assertEquals(e.getReason(), HuntException.REASON_TOO_LARGE);
                latch.countDown();
            }
        });
        latch.await();
    }

    public void testOutputLimitWithXXX() throws Exception {
        //no-op just for now util I know how to calculate a bitmap memory size correctly
    }

    public void testStepWithLocal() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        List<String> ret = new LocalFileTask(activity, null).execute().get();
        assertNotNull(ret);
        assertTrue(ret.size() > 0);
        Options.ExactOptionsBuilder builder = new Options.ExactOptionsBuilder();
        builder.maxOutput(10).step(20);
        SoBitmap.getInstance(activity).hunt(TAG, Uri.fromFile(new File(ret.get(0))), builder.build(), new TestCallback() {
            @Override
            public void onHunted(Bitmap bitmap, BitmapFactory.Options options) {
                super.onHunted(bitmap, options);
                latch.countDown();
            }

            @Override
            public void onException(HuntException e) {
                super.onException(e);
                assertEquals(1, 2);
            }
        });
        latch.await();

        Options.ExactOptionsBuilder builder1 = new Options.ExactOptionsBuilder();
        try {
            builder1.maxInput(10).maxOutput(10).step(0);
            assertEquals(1, 2);
        } catch (IllegalArgumentException ignore) {
        }

        Options.ExactOptionsBuilder builder2 = new Options.ExactOptionsBuilder();
        try {
            builder2.maxInput(10).maxOutput(10).step(100);
            assertEquals(1, 2);
        } catch (IllegalArgumentException ignore) {
        }

        Options.ExactOptionsBuilder builder3 = new Options.ExactOptionsBuilder();
        try {
            builder3.maxInput(10).maxOutput(10).step(-100);
            assertEquals(1, 2);
        } catch (IllegalArgumentException ignore) {
        }
    }

    //TODO compress error while OOM occurred
    public void testQualityLevelWithLocal() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        List<String> ret = new LocalFileTask(activity, null).execute().get();
        assertNotNull(ret);
        assertTrue(ret.size() > 0);
        final SoBitmap soBitmap = SoBitmap.getInstance(activity);
        final Uri target = Uri.fromFile(new File(ret.get(0)));
        final Options.FuzzyOptionsBuilder builder = new Options.FuzzyOptionsBuilder();
        soBitmap.hunt(TAG, target, builder.level(Options.QualityLevel.HIGH).build(), new TestCallback() {
            @Override
            public void onHunted(Bitmap bitmap, BitmapFactory.Options options) {
                super.onHunted(bitmap, options);
                soBitmap.hunt(TAG, target, builder.level(Options.QualityLevel.MEDIUM).build(), new TestCallback() {
                    @Override
                    public void onHunted(Bitmap bitmap, BitmapFactory.Options options) {
                        super.onHunted(bitmap, options);
                        soBitmap.hunt(TAG, target, builder.level(Options.QualityLevel.LOW).build(), new TestCallback() {
                            @Override
                            public void onHunted(Bitmap bitmap, BitmapFactory.Options options) {
                                super.onHunted(bitmap, options);
                                latch.countDown();
                            }

                            @Override
                            public void onException(HuntException e) {
                                super.onException(e);
                                assertEquals(1, 2);
                            }
                        });
                    }

                    @Override
                    public void onException(HuntException e) {
                        super.onException(e);
                        assertEquals(1, 2);
                    }
                });
            }

            @Override
            public void onException(HuntException e) {
                super.onException(e);
                assertEquals(1, 2);
            }
        });
        latch.await();
    }

    public void testMaxSizeLimitWithLocal() throws Exception {
        //TODO how to judge approximate value
    }

    public void testCompressFormatWithLocal() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        List<String> ret = new LocalFileTask(activity, null).execute().get();
        assertNotNull(ret);
        assertTrue(ret.size() > 0);
        final SoBitmap soBitmap = SoBitmap.getInstance(activity);
        final Uri target = Uri.fromFile(new File(ret.get(0)));
        final Options.FuzzyOptionsBuilder builder = new Options.FuzzyOptionsBuilder();
        soBitmap.hunt(TAG, target, builder.format(Bitmap.CompressFormat.JPEG).build(), new TestCallback() {
            @Override
            public void onHunted(Bitmap bitmap, BitmapFactory.Options options) {
                super.onHunted(bitmap, options);
                soBitmap.hunt(TAG, target, builder.format(Bitmap.CompressFormat.PNG).build(), new TestCallback() {
                    @Override
                    public void onHunted(Bitmap bitmap, BitmapFactory.Options options) {
                        super.onHunted(bitmap, options);
                        soBitmap.hunt(TAG, target, builder.format(Bitmap.CompressFormat.WEBP).build(), new TestCallback() {
                            @Override
                            public void onHunted(Bitmap bitmap, BitmapFactory.Options options) {
                                super.onHunted(bitmap, options);
                                latch.countDown();
                            }

                            @Override
                            public void onException(HuntException e) {
                                super.onException(e);
                                assertEquals(1, 2);
                            }
                        });
                    }

                    @Override
                    public void onException(HuntException e) {
                        super.onException(e);
                        assertEquals(1, 2);
                    }
                });
            }

            @Override
            public void onException(HuntException e) {
                super.onException(e);
                assertEquals(1, 2);
            }
        });
        latch.await();
    }

    public void testNotFoundWithLocal() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        Uri target = Uri.fromFile(new File("/sdcard/dummy_file"));

        SoBitmap.getInstance(activity).hunt(target, new TestCallback(){
            @Override
            public void onHunted(Bitmap bitmap, BitmapFactory.Options options) {
                super.onHunted(bitmap, options);
                assertEquals(1, 2);
            }

            @Override
            public void onException(HuntException e) {
                super.onException(e);
                assertEquals(e.getReason(), HuntException.REASON_FILE_NOT_FOUND);
                latch.countDown();
            }
        });

        latch.await();
    }

    public void testBlockBitmapWithLocal() throws Throwable {
        List<String> ret = new LocalFileTask(activity, null).execute().get();
        assertNotNull(ret);
        assertTrue(ret.size() > 0);
        final Bitmap bitmap = SoBitmap.getInstance(activity).huntBlock(TAG, Uri.fromFile(new File(ret.get(0))));
        assertNotNull(bitmap);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                img.setImageBitmap(bitmap);
            }
        });
    }

}
