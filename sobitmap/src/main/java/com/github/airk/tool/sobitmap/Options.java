package com.github.airk.tool.sobitmap;

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

    public Options(long maxInput, long maxOutput, int maxSize, int qualityStep) {
        this.maxInput = maxInput;
        this.maxOutput = maxOutput;
        this.maxSize = maxSize;
        this.qualityStep = qualityStep;
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
