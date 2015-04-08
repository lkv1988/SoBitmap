# SoBitmap
SoBitmap is not an ImageLoader, it born for process single bitmap. Some conditions, we want a image displayed in some limit, such as the max size, the memory cost and its format. SoBitmap handle these all for you, then release you to concern the real important things. You can totally use SoBitmap as a black box, the only things you need care are the input configuration and the output bitmap.

# Feature

- support local file, MediaStore and network stream
- support two config way:

	1. exact limit include max input, max output, and compress quality down step
	2. fuzzy limit that you only need set a level or just by default.
- use okhttp as httpclient for downloading, I think we can trust it(Shall we have a choice about it?)
- all callback heppen in UI thread, so relax about it

# Usage

### Include in your project

- Gradle

```
repositories {
    maven { url "https://oss.sonatype.org/content/repositories/snapshots" }
}

dependencies {
	compile 'com.github.airk000:sobitmap:0.1.+'
}
```

### Permissions

```
<!-- if SoBitmap need to load image from network -->
<uses-permission android:name="android.permission.INTERNET" />
<!-- if your want use external storage for cache -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

### Min SDK

API9 (Android2.3)

### Custom display option

#####Exactly
```
Options.ExactOptionsBuilder builder = new Options.ExactOptionsBuilder();
builder.step(10)
        .format(Bitmap.CompressFormat.JPEG)
        .maxOutput(200)
        .maxInput(10 * 1000)
        .maxSize(5000);
Options ops = builder.build();
```

#####Fuzzy
```
Options.FuzzyOptionsBuilder builder = new Options.FuzzyOptionsBuilder();
builder.maxSize(5000)
        .format(Bitmap.CompressFormat.PNG)
        .level(Options.QualityLevel.HIGH);
Options ops = builder.build();
```

#####Change the default option
```
SoBitmap.getInstance(context).setDefaultOption(myCustomOps);
```

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

# *TODO*:

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