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
    long maxInput = -1L;
    long maxOutput = -1L;
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
     */
    final int maxSize;
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

    /**
     * Use detail parameters construct display option
     *
     * @param maxInput    Max input in memory
     * @param maxOutput   Max output in memory
     * @param maxSize     The max display size in pixel
     * @param qualityStep quality down step
     * @param format      JPG\PNG\WEBP
     */
    public Options(long maxInput, long maxOutput, int maxSize, int qualityStep, Bitmap.CompressFormat format) {
        this.maxInput = maxInput;
        this.maxOutput = maxOutput;
        this.maxSize = maxSize;
        this.qualityStep = qualityStep;
        this.format = format;

        onlyLevel = false;
        level = null;
    }

    /**
     * Use level to construct display option, don't care about the detail inside.
     *
     * @param maxSize The max display size in pixel
     * @param format  JPG\PNG\WEBP
     * @param level   {@link com.github.airk.tool.sobitmap.Options.QualityLevel}
     */
    public Options(int maxSize, Bitmap.CompressFormat format, QualityLevel level) {
        this.maxSize = maxSize;
        this.format = format;
        this.level = level;

        onlyLevel = true;
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
        return level == options.level;

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
}
