# SoBitmap
SoBitmap is a bitmap decode library for Android. Users can custom the max input memory size,
the max output bitmap memory size and the picture size in pixel he can accept, then SoBitmap
will do its best for producing the right bitmap for you. And of course, users have no need to
warry about the OOM exception, SoBitmap have handled it inside.

# Feature

- Support local file and network stream
- Custom option include max input\output and picture size, and the step of picture's quality
- Use okhttp as the httpclient for downloading

# Usage

### Permissions

```
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

### Min SDK

Api-9 (Android2.3)

### Custom display option
```
Options options = new Options.Builder()
                .maxOutput(50) // max output in 50KB
                .maxSize(1024) //max picture size in pixel
                .build();
SoBitmap.getInstance(this).setDefaultOption(options);
```

>The default option:

- MAX input 5MB
- MAX output 300KB
- STEP 15
- SIZE max screen size * 2



### Hunting bitmap

```
SoBitmap.getInstance(this).hunt(uri, new Callback() {
            @Override
            public void onHunted(Bitmap bitmap, BitmapFactory.Options options) {
                imageView.setImageBitmap(bitmap);
            }

            @Override
            public void onException(HuntException e) {
                textView.setText(e.toString());
            }
        });
```

# *TODO List*:

- MediaStore support
- WebP support
- User set picture format
- Multi thread speed up the decoding duration

# License

```
Copyright 2015 Kevin Liu

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```