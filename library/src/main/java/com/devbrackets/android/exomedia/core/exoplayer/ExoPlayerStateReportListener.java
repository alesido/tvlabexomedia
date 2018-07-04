package com.devbrackets.android.exomedia.core.exoplayer;

import com.google.android.exoplayer2.decoder.DecoderCounters;

/**
 * Created on 5/15/18.
 */
public interface ExoPlayerStateReportListener
{
    void onStreamFormatRecognized(String mediaSourceClassName);

    void onPlaybackStateChangeReport(String reportText, DecoderCounters ... decoderCounters);
}
