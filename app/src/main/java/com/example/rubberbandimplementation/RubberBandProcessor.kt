package com.example.rubberbandimplementation

import com.google.android.exoplayer2.audio.AudioProcessor
import com.google.android.exoplayer2.audio.BaseAudioProcessor
import java.nio.ByteBuffer

class RubberBandProcessor : BaseAudioProcessor() {

    private var pitchScale: Double = 1.0
    private var stretcher: RubberBandStretcher? = null

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        // Configure your input and output format, e.g., sample rate, channels
        val sampleRate = inputAudioFormat.sampleRate
        val channels = inputAudioFormat.channelCount
        stretcher = RubberBandStretcher(sampleRate, channels, RubberBandStretcher.DefaultOptions, 1.0, pitchScale)
        return inputAudioFormat
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        // Use the stretcher to process the audio input
        stretcher?.process(inputBuffer.asShortBuffer(), 2, false)
        // Retrieve the processed output
        val outputBuffer = ByteBuffer.allocate(inputBuffer.capacity())
        stretcher?.retrieve(outputBuffer.asShortBuffer(), 2)
        // Send the processed audio to the ExoPlayer
       // replaceOutputBuffer(outputBuffer)
    }

    override fun onQueueEndOfStream() {
        // Handle end of audio stream
        stretcher?.queueEndOfStream()
        super.queueEndOfStream()
    }

    fun setPitchScale(scale: Double) {
        pitchScale = scale
        stretcher?.setPitchScale(scale)
    }

    override fun onReset() {
        stretcher?.dispose()
        stretcher = null
    }
}
