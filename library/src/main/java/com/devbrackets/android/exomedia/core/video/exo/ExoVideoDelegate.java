/*
 * Copyright (C) 2016 - 2018 ExoMedia Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devbrackets.android.exomedia.core.video.exo;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.Surface;

import com.devbrackets.android.exomedia.ExoMedia;
import com.devbrackets.android.exomedia.core.ListenerMux;
import com.devbrackets.android.exomedia.core.exoplayer.ExoMediaPlayer;
import com.devbrackets.android.exomedia.core.exoplayer.ExoPlayerStateReportListener;
import com.devbrackets.android.exomedia.core.listener.CaptionListener;
import com.devbrackets.android.exomedia.core.exoplayer.WindowInfo;
import com.devbrackets.android.exomedia.core.listener.CaptionListener;
import com.devbrackets.android.exomedia.core.listener.MetadataListener;
import com.devbrackets.android.exomedia.core.video.ClearableSurface;
import com.devbrackets.android.exomedia.listener.OnBufferUpdateListener;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.drm.MediaDrmCallback;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.playlist.HlsPlaylistParserFactory;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import java.util.List;
import java.util.Map;

public class ExoVideoDelegate {
    protected ExoMediaPlayer exoMediaPlayer;

    protected ListenerMux listenerMux;
    protected boolean playRequested = false;

    protected Context context;
    protected ClearableSurface clearableSurface;

    @NonNull
    protected InternalListeners internalListeners = new InternalListeners();

    public ExoVideoDelegate(@NonNull Context context, @NonNull ClearableSurface clearableSurface) {
        this.context = context.getApplicationContext();
        this.clearableSurface = clearableSurface;

        setup();
    }

    public void setVideoUri(@Nullable Uri uri) {
        setVideoUri(uri, null);
    }

    public void setVideoAndSubtitlesUri(Uri videoUri, Uri subtitlesUri) {
        setVideoUri(videoUri, null);
        exoMediaPlayer.setVideoAndSubtitlesUri(videoUri, subtitlesUri);
    }

    public void setVideoUri(@Nullable Uri uri, @Nullable MediaSource mediaSource) {
        //Makes sure the listeners get the onPrepared callback
        listenerMux.setNotifiedPrepared(false);
        exoMediaPlayer.seekTo(0);

        if (mediaSource != null) {
            exoMediaPlayer.setMediaSource(mediaSource);
            listenerMux.setNotifiedCompleted(false);
        } else if (uri != null) {
            exoMediaPlayer.setUri(uri);
            listenerMux.setNotifiedCompleted(false);
        } else {
            exoMediaPlayer.setMediaSource(null);
        }
    }

    /**
     * Sets the {@link MediaDrmCallback} to use when handling DRM for media.
     * This should be called before specifying the videos uri or path
     * <br>
     * <b>NOTE:</b> DRM is only supported on API 18 +
     *
     * @param drmCallback The callback to use when handling DRM media
     */
    public void setDrmCallback(@Nullable MediaDrmCallback drmCallback) {
        exoMediaPlayer.setDrmCallback(drmCallback);
    }

    public void customizeHlsPlaylistParserFactory(HlsPlaylistParserFactory hlsPlaylistParserFactory) {
        exoMediaPlayer.customizeHlsPlaylistParserFactory(hlsPlaylistParserFactory);
    }

    public boolean restart() {
        if(!exoMediaPlayer.restart()) {
            return false;
        }

        //Makes sure the listeners get the onPrepared callback
        listenerMux.setNotifiedPrepared(false);
        listenerMux.setNotifiedCompleted(false);

        return true;
    }

    @FloatRange(from = 0.0, to = 1.0)
    public float getVolume() {
        return exoMediaPlayer.getVolume();
    }

    public boolean setVolume(@FloatRange(from = 0.0, to = 1.0) float volume) {
        exoMediaPlayer.setVolume(volume);
        return true;
    }

    public void seekTo(@IntRange(from = 0) long milliseconds) {
        exoMediaPlayer.seekTo(milliseconds);
    }

    public boolean isPlaying() {
        return exoMediaPlayer.getPlayWhenReady();
    }

    public void start() {
        exoMediaPlayer.setPlayWhenReady(true);
        listenerMux.setNotifiedCompleted(false);
        playRequested = true;
    }

    public void pause() {
        exoMediaPlayer.setPlayWhenReady(false);
        playRequested = false;
    }

    /**
     * Performs the functionality to stop the video in playback
     *
     * @param clearSurface <code>true</code> if the surface should be cleared
     */
    public void stopPlayback(boolean clearSurface) {
        exoMediaPlayer.stop();
        playRequested = false;

        if (clearSurface) {
            listenerMux.clearSurfaceWhenReady(clearableSurface);
        }
    }

    public void suspend() {
        exoMediaPlayer.release();
        playRequested = false;
    }

    public long getDuration() {
        if (!listenerMux.isPrepared()) {
            return 0;
        }

        return exoMediaPlayer.getDuration();
    }

    public long getCurrentPosition() {
        if (!listenerMux.isPrepared()) {
            return 0;
        }

        return exoMediaPlayer.getCurrentPosition();
    }

    public int getBufferedPercent() {
        return exoMediaPlayer.getBufferedPercentage();
    }

    @Nullable
    public WindowInfo getWindowInfo() {
        return exoMediaPlayer.getWindowInfo();
    }

    public boolean trackSelectionAvailable() {
        return true;
    }

    public void setCaptionListener(@Nullable CaptionListener listener) {
        exoMediaPlayer.setCaptionListener(listener);
    }

    /**
     * @deprecated use {@link #setTrack(ExoMedia.RendererType, int, int)}
     */
    @Deprecated
    public void setTrack(ExoMedia.RendererType trackType, int trackIndex) {
        exoMediaPlayer.setSelectedTrack(trackType, trackIndex);
    }

    public void setTrack(@NonNull ExoMedia.RendererType trackType, int groupIndex, int trackIndex) {
        exoMediaPlayer.setSelectedTrack(trackType, groupIndex, trackIndex);
    }

    public int getSelectedTrackIndex(@NonNull ExoMedia.RendererType type, int groupIndex) {
        return exoMediaPlayer.getSelectedTrackIndex(type, groupIndex);
    }

    /**
     * Clear all selected tracks for the specified renderer.
     * @param type The renderer type
     */
    public void clearSelectedTracks(@NonNull ExoMedia.RendererType type) {
        exoMediaPlayer.clearSelectedTracks(type);
    }

    @Nullable
    public Map<ExoMedia.RendererType, TrackGroupArray> getAvailableTracks() {
        return exoMediaPlayer.getAvailableTracks();
    }

    public void setRendererEnabled(@NonNull ExoMedia.RendererType type, boolean enabled) {
        exoMediaPlayer.setRendererEnabled(type, enabled);
    }

    /**
     * Return true if at least one renderer for the given type is enabled
     * @param type The renderer type
     * @return true if at least one renderer for the given type is enabled
     */
    public boolean isRendererEnabled(@NonNull ExoMedia.RendererType type) {
        return exoMediaPlayer.isRendererEnabled(type);
    }

    public boolean setPlaybackSpeed(float speed) {
        return exoMediaPlayer.setPlaybackSpeed(speed);
    }

    public float getPlaybackSpeed() {
        return exoMediaPlayer.getPlaybackSpeed();
    }

    public void release() {
        exoMediaPlayer.release();
    }

    public void setListenerMux(ListenerMux listenerMux) {
        if (this.listenerMux != null) {
            exoMediaPlayer.removeListener(this.listenerMux);
            exoMediaPlayer.removeAnalyticsListener(this.listenerMux);
        }

        this.listenerMux = listenerMux;
        exoMediaPlayer.addListener(listenerMux);
        exoMediaPlayer.addAnalyticsListener(listenerMux);
    }

    public void setRepeatMode(int repeatMode) {
        exoMediaPlayer.setRepeatMode(repeatMode);
    }

    // alsi++
    public void enableHealthMonitor(boolean enable) {
        if (enable) {
            exoMediaPlayer.setBandwidthMeterListener(internalListeners);
            exoMediaPlayer.setVideoRendererListener(internalListeners);
            exoMediaPlayer.setPlayerStateReportListener(internalListeners);
        }
        else {
            exoMediaPlayer.setBandwidthMeterListener(null);
            exoMediaPlayer.setVideoRendererListener(null);
            exoMediaPlayer.setPlayerStateReportListener(null);
        }
    }

    public void onSurfaceReady(Surface surface) {
        exoMediaPlayer.setSurface(surface);
        if (playRequested) {
            exoMediaPlayer.setPlayWhenReady(true);
        }
    }

    public void onSurfaceDestroyed() {
        exoMediaPlayer.clearSurface();
    }

    protected void setup() {
        initExoPlayer();
    }

    protected void initExoPlayer() {
        exoMediaPlayer = new ExoMediaPlayer(context);

        exoMediaPlayer.setMetadataListener(internalListeners);
        exoMediaPlayer.setBufferUpdateListener(internalListeners);

        // alsi++
        exoMediaPlayer.addPlayerEventListener(internalListeners);

        // als+++
        exoMediaPlayer.setCaptionListener(internalListeners);
    }

    protected class InternalListeners implements CaptionListener, MetadataListener, OnBufferUpdateListener,
            BandwidthMeter.EventListener, VideoRendererEventListener, ExoPlayerStateReportListener,
            Player.EventListener {

        /** alsi+++ Caption listener callback
         */
        @Override
        public void onCues(List<Cue> cues) {
            if (listenerMux != null)
                listenerMux.onCues(cues);
        }

        @Override
        public void onMetadata(Metadata metadata) {
            listenerMux.onMetadata(metadata);
        }

        @Override
        public void onBufferingUpdate(@IntRange(from = 0, to = 100) int percent) {
            if (listenerMux != null)
                listenerMux.onBufferingUpdate(percent);
        }

        @Override
        public void onBandwidthSample(int elapsedMs, long bytes, long bitrate) {
            if (listenerMux != null)
                listenerMux.onBandwidthSample(elapsedMs, bytes, bitrate);
        }

        @Override
        public void onVideoEnabled(DecoderCounters counters) {
            // left empty as not applicable
        }

        @Override
        public void onVideoDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {
            if (listenerMux != null)
                listenerMux.onVideoDecoderInitialized(decoderName);
        }

        @Override
        public void onVideoInputFormatChanged(Format format) {
            if (listenerMux != null)
                listenerMux.onVideoInputFormatChanged(format);
        }

        @Override
        public void onDroppedFrames(int count, long elapsedMs) {
            if (listenerMux != null)
                listenerMux.onDroppedFrames(count, elapsedMs);
        }

        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
            // left empty as not applicable
        }

        @Override
        public void onRenderedFirstFrame(Surface surface) {
            // left empty as not applicable
        }

        @Override
        public void onVideoDisabled(DecoderCounters counters) {
            // left empty as not applicable
        }

        public void onStreamFormatRecognized(String mediaSourceClassName) {
            if (listenerMux != null)
                listenerMux.onStreamFormatRecognized(mediaSourceClassName);
        }

        public void onPlaybackStateChangeReport(String reportText, DecoderCounters ... counters) {
            if (listenerMux != null)
                listenerMux.onPlaybackStateChangeReport(reportText, counters);
        }

        // region Player Event Listener

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            // reserved for possible future implementation
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            // reserved for possible future implementation
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if (listenerMux != null)
                listenerMux.onPlayerStateChanged(playbackState);
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
            // reserved for possible future implementation
        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
            // reserved for possible future implementation
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            // reserved for possible future implementation
        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            // reserved for possible future implementation
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            // reserved for possible future implementation
        }

        @Override
        public void onSeekProcessed() {
            // reserved for possible future implementation
        }

        // endregion
    }
}
