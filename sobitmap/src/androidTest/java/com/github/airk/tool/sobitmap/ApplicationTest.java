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

package com.github.airk.tool.sobitmap;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.test.ApplicationTestCase;

import java.io.File;
import java.util.concurrent.CountDownLatch;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    String BIG_SIZE_URL = "http://www.photo0086.com/member/3337/pic/2011032108344734474.JPG";
    String NORMAL_SIZE_URL = "http://f.hiphotos.baidu.com/image/w%3D2048/sign=527f77af184c510faec4e51a5461242d/d1a20cf431adcbefa332d761aeaf2edda3cc9f57.jpg";
    String LOCAL_FILE = "/sdcard/DCIM/Camera/IMG_20150321_173922.jpg";

    SoBitmap soBitmap;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createApplication();
        soBitmap = SoBitmap.getInstance(getApplication());
    }

    @Override
    protected void tearDown() throws Exception {
        soBitmap.shutdown();
        super.tearDown();
    }

    public void testBigNetworkPic() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        soBitmap.hunt(Uri.parse(BIG_SIZE_URL), new Callback() {
            @Override
            public void onHunted(Bitmap bitmap, BitmapFactory.Options options) {
                assertNotNull(bitmap);
                assertNotNull(options);
                latch.countDown();
            }

            @Override
            public void onException(HuntException e) {
                assertEquals(1, 2);
                latch.countDown();
            }
        });
        latch.await();
    }

    public void testNormalNetworkPic() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        soBitmap.hunt(Uri.parse(NORMAL_SIZE_URL), new Callback() {
            @Override
            public void onHunted(Bitmap bitmap, BitmapFactory.Options options) {
                assertNotNull(bitmap);
                assertNotNull(options);
                latch.countDown();
            }

            @Override
            public void onException(HuntException e) {
                assertEquals(1, 2);
                latch.countDown();
            }
        });
        latch.await();
    }

    public void testLocalFile() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        soBitmap.hunt(Uri.fromFile(new File(LOCAL_FILE)), new Callback() {
            @Override
            public void onHunted(Bitmap bitmap, BitmapFactory.Options options) {
                assertNotNull(bitmap);
                assertNotNull(options);
                latch.countDown();
            }

            @Override
            public void onException(HuntException e) {
                assertEquals(1, 2);
                latch.countDown();
            }
        });
        latch.await();
    }

    public void testCustomOptionWithNet() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        Options options = new Options.Builder()
                .maxSize(500)
                .maxOutput(50)
                .qualityStep(5).build();
        soBitmap.setDefaultOption(options);
        soBitmap.hunt(Uri.parse(BIG_SIZE_URL), new Callback() {
            @Override
            public void onHunted(Bitmap bitmap, BitmapFactory.Options options) {
                assertNotNull(bitmap);
                assertNotNull(options);
                latch.countDown();
            }

            @Override
            public void onException(HuntException e) {
                assertEquals(1, 2);
                latch.countDown();
            }
        });
        latch.await();
    }

    public void testCustomOptionWithLocal() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        Options options = new Options.Builder()
                .maxSize(500)
                .maxOutput(50)
                .qualityStep(5).build();
        soBitmap.setDefaultOption(options);
        soBitmap.hunt(Uri.fromFile(new File(LOCAL_FILE)), new Callback() {
            @Override
            public void onHunted(Bitmap bitmap, BitmapFactory.Options options) {
                assertNotNull(bitmap);
                assertNotNull(options);
                latch.countDown();
            }

            @Override
            public void onException(HuntException e) {
                assertEquals(1, 2);
                latch.countDown();
            }
        });
        latch.await();
    }

}