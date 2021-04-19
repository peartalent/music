package com.imn.ivisusample

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.imn.ivisusample.databinding.ActivityPlayBinding
import com.imn.ivisusample.player.AudioPlayer
import com.imn.ivisusample.utils.formatAsTime
import com.imn.ivisusample.utils.getDrawableCompat
import kotlin.math.sqrt

class PlayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayBinding
    private lateinit var player: AudioPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val dash = "http://www.bok.net/dash/tears_of_steel/cleartext/stream.mpd"
        val hls = "http://qthttp.apple.com.edgesuite.net/1010qwoeiuryfg/sl.m3u8"
        val smoothStreaming = "http://playready.directtaps.net/smoothstreaming/SSWSS720H264/SuperSpeedway_720.ism/Manifest"
        val progressive = "http://ftp.nluug.nl/pub/graphics/blender/demo/movies/Sintel.2010.720p.mkv"
        val x = "http://192.168.100.164:3000/voice/6efd690020264448bcc8978317daa0a11618766249586.wav"
        val anime = "https://beststream.io/api/play/17d504ec82ce1b729cd1e1bd3aecfb49"

        binding.exoPlayerView.prepare(Uri.parse(x))
    }

    override fun onStart() {
        super.onStart()
        listenOnPlayerStates()
        initUI()
    }

    override fun onStop() {
        player.release()
        super.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.exoPlayerView.onPause()
    }

    private fun initUI() = with(binding) {
        visualizer.apply {
            ampNormalizer = { sqrt(it.toFloat()).toInt() }
            onStartSeeking = {
                player.pause()
            }
            onSeeking = { binding.timelineTextView.text = it.formatAsTime() }
            onFinishedSeeking = { time, isPlayingBefore ->
                player.seekTo(time)
                if (isPlayingBefore) {
                    player.resume()
                }
            }
            onAnimateToPositionFinished = { time, isPlaying ->
                updateTime(time, isPlaying)
                player.seekTo(time)
            }
        }
        playButton.setOnClickListener { player.togglePlay() }
//        seekForwardButton.setOnClickListener { visualizer.seekOver(SEEK_OVER_AMOUNT) }
//        seekBackwardButton.setOnClickListener { visualizer.seekOver(-SEEK_OVER_AMOUNT) }

        lifecycleScope.launchWhenCreated {
            val amps = player.loadAmps()
            visualizer.setWaveForm(amps, player.tickDuration)
        }
    }

    private fun listenOnPlayerStates() = with(binding) {
        player = AudioPlayer.getInstance(applicationContext).init().apply {
            onStart = { playButton.icon = getDrawableCompat(R.drawable.ic_pause_24) }
            onStop = { playButton.icon = getDrawableCompat(R.drawable.ic_play_arrow_24) }
            onPause = { playButton.icon = getDrawableCompat(R.drawable.ic_play_arrow_24) }
            onResume = { playButton.icon = getDrawableCompat(R.drawable.ic_pause_24) }
            onProgress = { time, isPlaying -> updateTime(time, isPlaying) }
        }
    }

    private fun updateTime(time: Long, isPlaying: Boolean) = with(binding) {
        timelineTextView.text = time.formatAsTime()
        visualizer.updateTime(time, isPlaying)
    }

    companion object {
        const val SEEK_OVER_AMOUNT = 5000
    }
}
