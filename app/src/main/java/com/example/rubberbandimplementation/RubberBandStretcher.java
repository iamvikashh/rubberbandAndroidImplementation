/* -*- c-basic-offset: 4 indent-tabs-mode: nil -*-  vi:set ts=8 sts=4 sw=4: */

/*
    Rubber Band Library
    An audio time-stretching and pitch-shifting library.
    Copyright 2007-2014 Particular Programs Ltd.

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License as
    published by the Free Software Foundation; either version 2 of the
    License, or (at your option) any later version.  See the file
    COPYING included with this distribution for more information.

    Alternatively, if you have a valid commercial licence for the
    Rubber Band Library obtained by agreement with the copyright
    holders, you may redistribute and/or modify it under the terms
    described in that licence.

    If you wish to distribute code using the Rubber Band Library
    under terms other than those of the GNU General Public License,
    you must obtain a valid commercial licence before doing so.
*/

package com.example.rubberbandimplementation;
import java.nio.ShortBuffer;

public class RubberBandStretcher {
    private static RubberBandStretcher stretcher = null;

    public RubberBandStretcher(int sampleRate, int channels,
                               int options,
                               double initialTimeRatio,
                               double initialPitchScale) {
        handle = 0;
        initialise(sampleRate, channels, options,
                initialTimeRatio, initialPitchScale);
    }

    private static RubberBandStretcher getInstance(int sampleRate, int channels,
                                                  int options,
                                                  double initialTimeRatio,
                                                  double initialPitchScale) {
        if (stretcher == null) {
            stretcher = new RubberBandStretcher(sampleRate,channels,options,initialTimeRatio,initialPitchScale);  // Initialize with required params
        }
        return stretcher;
    }

    public native void dispose();

    public native void reset();

    public native void setTimeRatio(double ratio);

    public native void setPitchScale(double scale);

    public native int getChannelCount();

    public native double getTimeRatio();

    public native double getPitchScale();

    public native int getLatency();

    public native void setTransientsOption(int options);

    public native void setDetectorOption(int options);

    public native void setPhaseOption(int options);

    public native void setFormantOption(int options);

    public native void setPitchOption(int options);

    public native void setExpectedInputDuration(long samples);

    public native void setMaxProcessSize(int samples);

    public native int getSamplesRequired();
    public native int getStartDelay();


    public native void study(float[][] input, int offset, int n, boolean finalBlock);

    public void study(float[][] input, boolean finalBlock) {
        study(input, 0, input[0].length, finalBlock);
    }

    public native void process(float[][] input, int offset, int n, boolean finalBlock);

    public void process(float[][] input, boolean finalBlock) {
        process(input, 0, input[0].length, finalBlock);
    }

    /**
     * Provide a block of "samples" sample frames for processing.
     * See also getSamplesRequired() and setMaxProcessSize().
     *
     * @param buffer       Audio data to process
     * @param channelCount The number of channel
     * @param finalBlock   Set to True if this is the last block of input data.
     */
    public void process(ShortBuffer buffer, int channelCount, boolean finalBlock) {
        int framesToWrite = buffer.remaining() / channelCount;
        short[] inputBuffer = new short[framesToWrite * channelCount];
        float[][] inputFloatBuffer = new float[channelCount][framesToWrite];
        buffer.get(inputBuffer);

        for (int i = 0; i < framesToWrite; i++)
            for (int c = 0; c < channelCount; c++)
                inputFloatBuffer[c][i] = inputBuffer[i * channelCount + c] / (float) 32768;

        process(inputFloatBuffer, 0, inputFloatBuffer[0].length, finalBlock);
    }

    /**
     * Call when audio stream ended.
     */
    public void queueEndOfStream() {
        process(new float[2][0], 0, 0, true);
    }

    public native int available();

    public native int retrieve(float[][] output, int offset, int n);

    public int retrieve(float[][] output) {
        return retrieve(output, 0, output[0].length);
    }

    /**
     * Obtain some processed output data from the stretcher.  Up to
     * "samples" samples will be stored in the output arrays (one per
     * channel for de-interleaved audio data) pointed to by "output".
     *
     * @param output       ShortBuffer to get output data
     * @param channelCount The number of channel
     * @return The actual number of samples in retrieved data
     */
    public int retrieve(ShortBuffer output, int channelCount) {
        int framesToWrite = output.remaining() / channelCount;
        float[][] out = new float[channelCount][framesToWrite];
        int retrieved = retrieve(out);

        for (int i = 0; i < framesToWrite; i++)
            for (int c = 0; c < channelCount; c++)
                output.put(normToShort(out[c][i]));

        return retrieved;
    }

    private short normToShort(float n) {
        if (n < -1) n = -1;
        else if (n > 1) n = 1;
        return (short) (n * 32767);
    }

    private native void initialise(int sampleRate, int channels, int options,
                                   double initialTimeRatio,
                                   double initialPitchScale);

    private long handle;

    public static final int OptionProcessOffline       = 0x00000000;
    public static final int OptionProcessRealTime      = 0x00000001;

    public static final int OptionStretchElastic       = 0x00000000;
    public static final int OptionStretchPrecise       = 0x00000010;

    public static final int OptionTransientsCrisp      = 0x00000000;
    public static final int OptionTransientsMixed      = 0x00000100;
    public static final int OptionTransientsSmooth     = 0x00000200;

    public static final int OptionDetectorCompound     = 0x00000000;
    public static final int OptionDetectorPercussive   = 0x00000400;
    public static final int OptionDetectorSoft         = 0x00000800;

    public static final int OptionPhaseLaminar         = 0x00000000;
    public static final int OptionPhaseIndependent     = 0x00002000;

    public static final int OptionThreadingAuto        = 0x00000000;
    public static final int OptionThreadingNever       = 0x00010000;
    public static final int OptionThreadingAlways      = 0x00020000;

    public static final int OptionWindowStandard       = 0x00000000;
    public static final int OptionWindowShort          = 0x00100000;
    public static final int OptionWindowLong           = 0x00200000;

    public static final int OptionSmoothingOff         = 0x00000000;
    public static final int OptionSmoothingOn          = 0x00800000;

    public static final int OptionFormantShifted       = 0x00000000;
    public static final int OptionFormantPreserved     = 0x01000000;

    public static final int OptionPitchHighSpeed       = 0x00000000;
    public static final int OptionPitchHighQuality     = 0x02000000;
    public static final int OptionPitchHighConsistency = 0x04000000;

    public static final int OptionChannelsApart        = 0x00000000;
    public static final int OptionChannelsTogether     = 0x10000000;

    public static final int DefaultOptions             = 0x00000000;
    public static final int PercussiveOptions          = 0x00102000;

    public static final int OptionEngineFaster         = 0x00000000;
    public static final int OptionEngineFiner          = 0x20000000;
    static {
        System.loadLibrary("rubberbandimplementation");
    }
}


