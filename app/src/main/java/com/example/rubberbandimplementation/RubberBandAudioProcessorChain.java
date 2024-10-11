package com.example.rubberbandimplementation;

import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.audio.AudioProcessor;
import com.google.android.exoplayer2.audio.AudioProcessorChain;
import com.google.android.exoplayer2.audio.SilenceSkippingAudioProcessor;
import com.google.android.exoplayer2.audio.SonicAudioProcessor;

public class RubberBandAudioProcessorChain implements AudioProcessorChain {

    private final AudioProcessor[] audioProcessors;
    private final SilenceSkippingAudioProcessor silenceSkippingAudioProcessor;
    private final RubberBandAudioProcessor sonicAudioProcessor;

    /**
     * Creates a new default chain of audio processors, with the user-defined {@code
     * audioProcessors} applied before silence skipping and speed adjustment processors.
     */
    public RubberBandAudioProcessorChain(AudioProcessor... audioProcessors) {
        this(audioProcessors, new SilenceSkippingAudioProcessor(), new RubberBandAudioProcessor());
    }

    /**
     * Creates a new default chain of audio processors, with the user-defined {@code
     * audioProcessors} applied before silence skipping and speed adjustment processors.
     */
    public RubberBandAudioProcessorChain(
            AudioProcessor[] audioProcessors,
            SilenceSkippingAudioProcessor silenceSkippingAudioProcessor,
            RubberBandAudioProcessor sonicAudioProcessor) {
        // The passed-in type may be more specialized than AudioProcessor[], so allocate a new array
        // rather than using Arrays.copyOf.
        this.audioProcessors = new AudioProcessor[audioProcessors.length + 2];
        System.arraycopy(
                /* src= */ audioProcessors,
                /* srcPos= */ 0,
                /* dest= */ this.audioProcessors,
                /* destPos= */ 0,
                /* length= */ audioProcessors.length);
        this.silenceSkippingAudioProcessor = silenceSkippingAudioProcessor;
        this.sonicAudioProcessor = sonicAudioProcessor;
        this.audioProcessors[audioProcessors.length] = silenceSkippingAudioProcessor;
        this.audioProcessors[audioProcessors.length + 1] = sonicAudioProcessor;
    }

    @Override
    public AudioProcessor[] getAudioProcessors() {
        return audioProcessors;
    }

    @Override
    public PlaybackParameters applyPlaybackParameters(PlaybackParameters playbackParameters) {
        sonicAudioProcessor.setSpeed(playbackParameters.speed);
        sonicAudioProcessor.setPitch(playbackParameters.pitch);
        return playbackParameters;
    }

    @Override
    public boolean applySkipSilenceEnabled(boolean skipSilenceEnabled) {
        silenceSkippingAudioProcessor.setEnabled(skipSilenceEnabled);
        return skipSilenceEnabled;
    }

    @Override
    public long getMediaDuration(long playoutDuration) {
        return sonicAudioProcessor.getMediaDuration(playoutDuration);
    }

    @Override
    public long getSkippedOutputFrameCount() {
        return silenceSkippingAudioProcessor.getSkippedFrames();
    }
}
