package com.devbrackets.android.exomedia.core.exoplayer;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.widget.TextView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.decoder.DecoderCounters;

import java.util.Locale;

/**
 * A helper class for periodically report with debug information obtained from player.
 */
public final class ExoPlayerStateReportHelper extends Player.DefaultEventListener implements Runnable {

    private static final int REFRESH_INTERVAL_MS = 1000;

    private final ExoPlayer player;
    private final Handler messageHandler;
    private final ExoPlayerStateReportListener reportListener;

    private boolean started;

    private Format videoFormat;
    private DecoderCounters audioDecoderCounters;
    private DecoderCounters videoDecoderCounters;
    private Format audioFormat;

    /**
     * @param player The {@link SimpleExoPlayer} from which debug information should be obtained.
     */
    public ExoPlayerStateReportHelper(ExoPlayer player, ExoPlayerStateReportListener reportListener) {
        this.player = player;
        this.reportListener = reportListener;
        messageHandler = new Handler();
    }

    /**
     * Starts periodic updates of the {@link TextView}. Must be called from the application's main
     * thread.
     */
    public void start() {
        if (started) {
            return;
        }
        started = true;
        player.addListener(this);
        updateAndPost();
    }

    /**
     * Stops periodic updates of the {@link TextView}. Must be called from the application's main
     * thread.
     */
    public void stop() {
        if (!started) {
            return;
        }
        started = false;
        player.removeListener(this);
    }

    // Player.EventListener implementation.

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        updateAndPost();
    }

    @Override
    public void onPositionDiscontinuity(@Player.DiscontinuityReason int reason) {
        updateAndPost();
    }

    // Runnable implementation.

    @Override
    public void run() {
        updateAndPost();
    }

    // Private methods.

    @SuppressLint("SetTextI18n")
    private void updateAndPost()
    {
        reportListener.onPlaybackStateChangeReport(
                getPlayerStateString() + getPlayerWindowIndexString()
                        + getVideoString() + getAudioString());

        messageHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateAndPost();
            }
        }, REFRESH_INTERVAL_MS);
    }

    public void setVideoFormat(Format videoFormat) {
        this.videoFormat = videoFormat;
    }

    public void setVideoDecoderCounters(DecoderCounters videoDecoderCounters) {
        this.videoDecoderCounters = videoDecoderCounters;
    }

    public void setAudioDecoderCounters(DecoderCounters audioDecoderCounters) {
        this.audioDecoderCounters = audioDecoderCounters;
    }

    public void setAudioFormat(Format audioFormat) {
        this.audioFormat = audioFormat;
    }

    private String getPlayerStateString() {
        String text = "playWhenReady:" + player.getPlayWhenReady() + " playbackState:";
        switch (player.getPlaybackState()) {
            case Player.STATE_BUFFERING:
                text += "buffering";
                break;
            case Player.STATE_ENDED:
                text += "ended";
                break;
            case Player.STATE_IDLE:
                text += "idle";
                break;
            case Player.STATE_READY:
                text += "ready";
                break;
            default:
                text += "unknown";
                break;
        }
        return text;
    }

    private String getPlayerWindowIndexString() {
        return " window:" + player.getCurrentWindowIndex();
    }

    private String getVideoString() {
        if (videoFormat == null) {
            return "";
        }
        return "\n" + videoFormat.sampleMimeType + "(id:" + videoFormat.id + " r:" + videoFormat.width + "x"
                + videoFormat.height + getPixelAspectRatioString(videoFormat.pixelWidthHeightRatio)
                + getDecoderCountersBufferCountString(videoDecoderCounters) + ")";
    }

    private String getAudioString() {
        Format format = audioFormat;
        if (format == null) {
            return "";
        }
        return "\n" + format.sampleMimeType + "(id:" + format.id + " hz:" + format.sampleRate + " ch:"
                + format.channelCount
                + getDecoderCountersBufferCountString(audioDecoderCounters) + ")";
    }

    private static String getDecoderCountersBufferCountString(DecoderCounters counters) {
        if (counters == null) {
            return "";
        }
        counters.ensureUpdated();
        return " sib:" + counters.skippedInputBufferCount
                + " sb:" + counters.skippedOutputBufferCount
                + " rb:" + counters.renderedOutputBufferCount
                + " db:" + counters.droppedBufferCount
                + " mcdb:" + counters.maxConsecutiveDroppedBufferCount
                + " dk:" + counters.droppedToKeyframeCount;
    }

    private static String getPixelAspectRatioString(float pixelAspectRatio) {
        return pixelAspectRatio == Format.NO_VALUE || pixelAspectRatio == 1f ? ""
                : (" par:" + String.format(Locale.US, "%.02f", pixelAspectRatio));
    }
}
