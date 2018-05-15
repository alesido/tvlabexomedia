package com.devbrackets.android.exomedia.core.exoplayer;

/**
 * Created on 5/15/18.
 */
public interface ExoPlayerStateReportListener
{
    void onStreamFormatRecognized(String mediaSourceClassName);

    void onPlaybackStateChangeReport(String reportText);
}
