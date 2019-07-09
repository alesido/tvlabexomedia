package com.devbrackets.android.exomedia.core;

import androidx.annotation.IntRange;

import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.decoder.DecoderCounters;

/**
 * Created on 5/10/18.
 */
public interface HealthMonitor
{
    void onBufferingUpdate(@IntRange(from = 0, to = 100) int percent);

    void onBandwidthSample(int elapsedMs, long bytes, long bitrate);

    void onVideoInputFormatChanged(Format format);

    void onDroppedFrames(int count, long elapsedMs);

    void onStreamFormatRecognized(String mediaSourceClassName);

    void onPlaybackStateChangeReport(String reportText, DecoderCounters... counters);

    void onVideoDecoderInitialized(String decoderName);
}
