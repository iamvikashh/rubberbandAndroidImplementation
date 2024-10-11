package com.example.rubberbandimplementation

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioProcessor
import com.google.android.exoplayer2.audio.AudioSink
import com.google.android.exoplayer2.audio.DefaultAudioSink
import com.google.android.exoplayer2.audio.SilenceSkippingAudioProcessor
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {
    private lateinit var player: ExoPlayer
    private var playbackPosition = 0L
    private var currentWindow = 0
    private var playWhenReady = true
    private lateinit var pitchTextView: TextView
    private lateinit var pitchSeekBar: SeekBar
    private lateinit var centsSeekBar: SeekBar
    private lateinit var bpmSeekBar: SeekBar
    private lateinit var loopSwitch: SwitchCompat
    private var currentPitch = 0f // Represents the current pitch in semitones
    private var currentCents = 0f // Represents the current pitch in cents
    private var currentBPM = 1f
    private lateinit var rubberBandProcessor:RubberBandAudioProcessor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


         rubberBandProcessor = RubberBandAudioProcessor()

        pitchTextView = findViewById(R.id.pitch_value)
        pitchSeekBar = findViewById(R.id.pitch_seekbar)
        centsSeekBar = findViewById(R.id.cents_seekbar)
        bpmSeekBar = findViewById(R.id.bpm_seekbar)
        loopSwitch = findViewById(R.id.loop_switch)

        pitchSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Convert SeekBar progress to pitch adjustment (-16 to +16)
                currentPitch = (progress - 16).toFloat()
                updatePlaybackParameters()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        centsSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Convert SeekBar progress to cents adjustment (-50 to +50)
                currentCents = (progress).toFloat()
                updatePlaybackParameters()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        bpmSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentBPM = 0.1f + (progress / 100f * 1.9f)  // This maps progress to the 0.1 - 2.0 range
                updatePlaybackParameters()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        loopSwitch.setOnCheckedChangeListener { _, isChecked ->
            player.repeatMode = if (isChecked) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF

        }

        playSample(this)
    }

    private fun playSample(context: Context) {
        if (!::player.isInitialized) {
            val trackSelector = DefaultTrackSelector(context)
            val loadControl = DefaultLoadControl.Builder()
                .setBufferDurationsMs(32 * 1024, 64 * 1024, 1024, 1024)
                .build()

            val chain = RubberBandAudioProcessorChain()
            Log.e("AudioProcessors", chain?.audioProcessors?.size.toString())

            // val chain = RubberBandAudioProcessorChain()
            val rendererFactory = object : DefaultRenderersFactory(context) {
                override fun buildAudioSink(
                    context: Context,
                    enableFloatOutput: Boolean,
                    enableAudioTrackPlaybackParams: Boolean,
                    enableOffload: Boolean
                ): AudioSink {
                    return DefaultAudioSink.Builder()
                       .setAudioProcessors(arrayOf( rubberBandProcessor,SilenceSkippingAudioProcessor()))
                        .setEnableAudioTrackPlaybackParams(false)
                        .setEnableFloatOutput(enableFloatOutput)
                        .build()
                }
            }
            player = ExoPlayer.Builder(context, rendererFactory)
                .setTrackSelector(trackSelector)
                .setLoadControl(loadControl)
                .build()

            val mainPcv = findViewById<PlayerControlView>(R.id.main_pcv)
            mainPcv.player = player
            mainPcv.showTimeoutMs = 0

            val factory = DefaultDataSourceFactory(context, Util.getUserAgent(context, "RubberBandFlutter"))
            val audioSource = ProgressiveMediaSource.Factory(factory)
                .createMediaSource(MediaItem.fromUri("file:///android_asset/tabla1.mp3"))

            rubberBandProcessor.setPitch(getPitchFactor(currentPitch,currentCents))
            rubberBandProcessor.setSpeed(currentBPM)
            //player.setPlaybackParameters(PlaybackParameters(currentBPM, getPitchFactor(currentPitch, currentCents)))
            player.setMediaSource(audioSource)
            player.prepare()
            player.seekTo(currentWindow, playbackPosition)
            player.playWhenReady = playWhenReady
        }
    }

    private fun releasePlayer() {
        player?.let {
            playbackPosition = it.currentPosition
            currentWindow = it.currentWindowIndex
            playWhenReady = it.playWhenReady
            it.playWhenReady = false
            it.release()
        }
    }

    override fun onResume() {
        super.onResume()
        playSample(this)
    }

    override fun onRestart() {
        super.onRestart()
        playSample(this)
    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    private fun getPitchFactor(semitones: Float, cents: Float): Float {
        // Calculate the pitch factor using 2^((semitones + cents/100) / 12)
        return Math.pow(2.0, ((semitones + cents / 100) / 12).toDouble()).toFloat()
    }

    private fun updatePlaybackParameters() {
        val pitchFactor = getPitchFactor(currentPitch, currentCents)
        rubberBandProcessor.setPitch(pitchFactor)
        rubberBandProcessor.setSpeed(currentBPM)
       // player.setPlaybackParameters(PlaybackParameters(currentBPM, pitchFactor))
        val dec = DecimalFormat("#,###.00")
        val bpm =dec.format(currentBPM)
        pitchTextView.text = "¸: $pitchFactor, Cents: $currentCents Speed X : $bpm"
    }
}
