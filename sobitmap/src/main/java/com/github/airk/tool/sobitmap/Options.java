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

import android.util.Log;

/**
 * Created by kevin on 15/3/24.
 * <p/>
 * Bitmap hunt options
 */
public final class Options {
    final long maxInput;
    final long maxOutput;
    final int maxSize;
    final int qualityStep;

    Options(long maxInput, long maxOutput, int maxSize, int qualityStep) {
        this.maxInput = maxInput;
        this.maxOutput = maxOutput;
        this.maxSize = maxSize;
        this.qualityStep = qualityStep;
    }

    public static class Builder {
        long input = SoBitmap.DEFAULT_MAX_INPUT;
        long output = SoBitmap.DEFAULT_MAX_OUTPUT;
        int size = -1;
        int step = SoBitmap.DEFAULT_QUALITY_STEP;

        public Builder() {
        }

        public Builder maxInput(long kb) {
            input = kb;
            return this;
        }

        public Builder maxOutput(long kb) {
            output = kb;
            return this;
        }

        public Builder qualityStep(int step) {
            if (step <= 0 && step >= 100) {
                Log.w(SoBitmap.TAG, "Options: step is invalid, will use the default one(15)");
                this.step = SoBitmap.DEFAULT_QUALITY_STEP;
            } else {
                this.step = step;
            }
            return this;
        }

        public Builder maxSize(int sizeInPixel) {
            if (sizeInPixel < 0) {
                throw new IllegalArgumentException("Wrong size given, it must be more than 0");
            }
            size = sizeInPixel;
            return this;
        }

        public Options build() {
            if (size == -1)
                throw new IllegalArgumentException("You must give a valid MAX size by maxSize() method while using custom options.");
            return new Options(input, output, size, step);
        }
    }

    @Override
    public String toString() {
        return "Options{" +
                "maxInput=" + maxInput +
                ", maxOutput=" + maxOutput +
                ", maxSize=" + maxSize +
                ", qualityStep=" + qualityStep +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Options options = (Options) o;

        if (maxInput != options.maxInput) return false;
        if (maxOutput != options.maxOutput) return false;
        if (maxSize != options.maxSize) return false;
        if (qualityStep != options.qualityStep) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (maxInput ^ (maxInput >>> 32));
        result = 31 * result + (int) (maxOutput ^ (maxOutput >>> 32));
        result = 31 * result + maxSize;
        result = 31 * result + qualityStep;
        return result;
    }
}
