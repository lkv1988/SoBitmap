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

import android.content.ContentResolver;
import android.net.Uri;

import java.io.File;

/**
 * Created by kevin on 15/3/24.
 * <p/>
 * Handle file request
 */
final class FileHunter extends Hunter {

    @Override
    boolean canHandle(Uri source) {
        return ContentResolver.SCHEME_FILE.equals(source.getScheme());
    }

    @Override
    File preCacheFile() {
        return new File(request.source.getPath());
    }

    @Override
    void cleanup(File cacheFile) {
        //no-op
    }

    @Override
    String tag() {
        return "FileHunter";
    }

}
