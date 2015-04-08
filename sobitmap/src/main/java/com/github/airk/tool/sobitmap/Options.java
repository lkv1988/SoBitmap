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

import android.graphics.Bitmap;

/**
 * Created by kevin on 15/3/24.
 * <p/>
 * Bitmap hunt options
 */
public final class Options {
    /**
     * 1. Use max input\output and step together
     */
    int maxInput = -1; // we are in kb
    int maxOutput = -1;
    /**
     * Bitmap compress quality
     */
    int qualityStep = -1;
    /**
     * 2. Only use the level param then make SoBitmap auto decide all the things inside.
     */
    QualityLevel level = null;
    /**
     * Whether ignore other parameters but only use the level for auto deciding while hunting.
     */
    boolean onlyLevel = false;

    /**
     * The max output bitmap size for width and height in pixel.
     * It's an approximate value, cause the inSampleSize always take the rounded down value nearest power of 2.
     * (http://developer.android.com/intl/en-us/reference/android/graphics/BitmapFactory.Options.html#inSampleSize)
     */
    int maxSize;
    /**
     * JPG, PNG, WEBP, if possible, highly recommend WEBP, fast and small for storage.
     */
    final Bitmap.CompressFormat format;

    public enum QualityLevel {
        HIGH {
            @Override
            int getStep() {
                return 5;
            }

            @Override
            float getMemoryFactor() {
                return 0.8f;
            }
        },
        MEDIUM {
            @Override
            int getStep() {
                return 15;
            }

            @Override
            float getMemoryFactor() {
                return 0.5f;
            }
        },
        LOW {
            @Override
            int getStep() {
                return 20;
            }

            @Override
            float getMemoryFactor() {
                return 0.35f;
            }
        };

        /**
         * compress quality step
         */
        abstract int getStep();

        /**
         * memory usage factor of total
         */
        abstract float getMemoryFactor();
    }

    Options(int maxSize, Bitmap.CompressFormat format) {
        this.maxSize = maxSize;
        this.format = format;
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
        if (format != options.format) return false;
        if (level != options.level) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (maxInput ^ (maxInput >>> 32));
        result = 31 * result + (int) (maxOutput ^ (maxOutput >>> 32));
        result = 31 * result + maxSize;
        result = 31 * result + qualityStep;
        result = 31 * result + (format != null ? format.hashCode() : 0);
        result = 31 * result + (level != null ? level.hashCode() : 0);
        return result;
    }

    public static class ExactOptionsBuilder {
        private Options opts;
        private int maxInput = -1;
        private int maxOutput = -1;
        private int step = -1;

        private int size = -1;
        private Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;

        public ExactOptionsBuilder maxSize(int size) {
            if (size < 0) {
                throw new IllegalArgumentException("Max size must greater than 0.");
            }
            this.size = size;
            return this;
        }

        public ExactOptionsBuilder maxInput(int kb) {
            if (kb < 0) {
                throw new IllegalArgumentException("Max input must greater than 0 and remember it's in KB.");
            }
            this.maxInput = kb;
            return this;
        }

        public ExactOptionsBuilder maxOutput(int kb) {
            if (kb < 0) {
                throw new IllegalArgumentException("Max output must greater than 0 and remember it's in KB.");
            }
            this.maxOutput = kb;
            return this;
        }

        public ExactOptionsBuilder step(int step) {
            if (step <= 0 || step >= 100) {
                throw new IllegalArgumentException("Wrong step (" + step + "), please keep it in 0 ~ 100.");
            }
            this.step = step;
            return this;
        }

        public ExactOptionsBuilder format(Bitmap.CompressFormat f) {
            format = f;
            return this;
        }

        public Options build() {
            if (maxOutput == -1) {
                throw new IllegalArgumentException("If you sure about using Exact options," +
                        " at least you should set the max output memory size, otherwise you are recommended to use the FuzzyOptions.");
            }
            opts = new Options(size, format);
            opts.maxOutput = maxOutput;
            opts.maxInput = maxInput;
            opts.qualityStep = step;

            opts.onlyLevel = false;
            opts.level = null;
            return opts;
        }
    }

    public static class FuzzyOptionsBuilder {
        private Options opts;
        private int size = -1;
        private QualityLevel level = QualityLevel.MEDIUM;
        private Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;

        public FuzzyOptionsBuilder maxSize(int size) {
            if (size < 0) {
                throw new IllegalArgumentException("Max size must greater than 0.");
            }
            this.size = size;
            return this;
        }

        public FuzzyOptionsBuilder format(Bitmap.CompressFormat f) {
            format = f;
            return this;
        }

        public FuzzyOptionsBuilder level(QualityLevel l) {
            level = l;
            return this;
        }

        public Options build() {
            opts = new Options(size, format);
            opts.level = level;
            opts.onlyLevel = true;
            return opts;
        }
    }
}
