package com.example.rubberbandimplementation;

import static java.lang.Math.abs;

import android.util.Log;

import com.example.rubberbandimplementation.RubberBandStretcher;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.audio.AudioProcessor;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class CustomRubberBandAudioProcessor implements AudioProcessor {
    private static final String TAG = "CustomRubberBandAudioProcessor";
    private static final int CHANNELS = 2; // Stereo
    private int sampleRateHz = -1;
    private float speed = 1.0f;
    private float pitch = 1.0f;
    private AudioProcessor.AudioFormat inputAudioFormat;
    private AudioProcessor.AudioFormat outputAudioFormat;
    private RubberBandStretcher stretcher;
    private ByteBuffer inputBuffer;
    private ByteBuffer outputBuffer;
    private boolean isActive;
    private boolean inputEnded;
    private boolean isStreamEnded;
    private float CLOSE_THRESHOLD = 0.0001f;
    private boolean isLooping = false;
    private ByteBuffer loopBuffer;

    public CustomRubberBandAudioProcessor() {
        inputAudioFormat = AudioProcessor.AudioFormat.NOT_SET;
        outputAudioFormat = AudioProcessor.AudioFormat.NOT_SET;
        inputBuffer = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder());
        outputBuffer = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder());
    }

    @Override
    public AudioFormat configure(AudioFormat inputAudioFormat) throws UnhandledAudioFormatException {
        if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT
                || inputAudioFormat.channelCount != CHANNELS) {
            throw new UnhandledAudioFormatException(inputAudioFormat);
        }

        if (!inputAudioFormat.equals(this.inputAudioFormat)) {
            this.inputAudioFormat = inputAudioFormat;
            this.sampleRateHz = inputAudioFormat.sampleRate;
            isActive = false;
            reset();
        }

        return inputAudioFormat;
    }

    private void recreateStretcher() {
        if (stretcher != null) {
            stretcher.dispose();
        }

        int options = RubberBandStretcher.OptionProcessRealTime |
                RubberBandStretcher.OptionEngineFiner |
                RubberBandStretcher.OptionTransientsCrisp;

        stretcher = new RubberBandStretcher(
                sampleRateHz,
                CHANNELS,
                options,
                speed,
                pitch
        );

        isActive = speed != 1.0f || pitch != 1.0f;
    }

    public void setSpeed(float speed) {
        Log.d(TAG, "Changing speed to: " + speed);
        if (Math.abs(this.speed - speed) >= CLOSE_THRESHOLD) {
            this.speed = speed;
            if (stretcher != null) {
                stretcher.setTimeRatio(1.0 / speed);
                processRemainingInput();
            } else {
                recreateStretcher();
            }
            isActive = speed != 1.0f || pitch != 1.0f;
        }
    }

    public void setPitch(float pitch) {
        Log.d(TAG, "Changing pitch to: " + pitch);
        if (Math.abs(this.pitch - pitch) >= CLOSE_THRESHOLD) {
            this.pitch = pitch;
            if (stretcher != null) {
                stretcher.setPitchScale(pitch);
                processRemainingInput();
            } else {
                recreateStretcher();
            }
            isActive = speed != 1.0f || pitch != 1.0f;
        }
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public void queueInput(ByteBuffer inputBuffer) {
        if (!inputBuffer.hasRemaining()) {
            return;
        }

        if (stretcher == null) {
            recreateStretcher();
        }

        ShortBuffer inputShortBuffer = inputBuffer.asShortBuffer();

        // Store input for looping if necessary
        if (isLooping && loopBuffer == null) {
            loopBuffer = ByteBuffer.allocateDirect(inputBuffer.remaining()).order(ByteOrder.nativeOrder());
            loopBuffer.put(inputBuffer.duplicate());
            loopBuffer.flip();
        }

        // Process the input through RubberBand
        stretcher.process(inputShortBuffer,2, false);

        // Get available output
        processRemainingInput();

        inputBuffer.position(inputBuffer.position() + inputShortBuffer.position() * 2);
    }

    private void processRemainingInput() {
        int available;
        do {
            available = stretcher.available();
            if (available > 0) {
                ByteBuffer newOutputBuffer = ByteBuffer.allocateDirect(available * CHANNELS * 2)
                        .order(ByteOrder.nativeOrder());
                ShortBuffer outputShortBuffer = newOutputBuffer.asShortBuffer();
                stretcher.retrieve(outputShortBuffer,2);
                newOutputBuffer.limit(outputShortBuffer.position() * 2);
                appendToOutputBuffer(newOutputBuffer);
            }
        } while (available > 0);
    }

    private void appendToOutputBuffer(ByteBuffer newData) {
        int oldCapacity = outputBuffer.capacity();
        int newCapacity = oldCapacity + newData.remaining();
        ByteBuffer newBuffer = ByteBuffer.allocateDirect(newCapacity).order(ByteOrder.nativeOrder());
        outputBuffer.flip();
        newBuffer.put(outputBuffer);
        newBuffer.put(newData);
        outputBuffer = newBuffer;
    }

    @Override
    public void queueEndOfStream() {
        if (isLooping) {
            // In loop mode, we don't actually end the stream
            if (loopBuffer != null) {
                loopBuffer.flip();
                queueInput(loopBuffer.duplicate());
            }
        } else {
            inputEnded = true;
            if (stretcher != null) {
                stretcher.queueEndOfStream();
                processRemainingInput();
                isStreamEnded = true;
            }
        }
    }

    @Override
    public ByteBuffer getOutput() {
        ByteBuffer buffer = outputBuffer;
        outputBuffer = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder());
        return buffer;
    }

    @Override
    public boolean isEnded() {
        return inputEnded && (stretcher == null || (stretcher.available() == 0 && !outputBuffer.hasRemaining()));
    }

    @Override
    public void flush() {
        if (stretcher != null) {
            stretcher.reset();
        }
        outputBuffer.clear();
        inputEnded = false;
        isStreamEnded = false;
    }

    @Override
    public void reset() {
        flush();
        if (stretcher != null) {
            stretcher.dispose();
            stretcher = null;
        }
        inputBuffer = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder());
        outputBuffer = ByteBuffer.allocateDirect(0).order(ByteOrder.nativeOrder());
        isActive = false;
        loopBuffer = null;
    }

    public void setLooping(boolean looping) {
        this.isLooping = looping;
        if (!looping) {
            loopBuffer = null;
        }
    }
}