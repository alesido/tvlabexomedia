/*
 * Copyright (C) 2016 Brian Wernick
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

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Surface;

import com.devbrackets.android.exomedia.ExoMedia;
import com.devbrackets.android.exomedia.core.ListenerMux;
import com.devbrackets.android.exomedia.core.api.VideoViewApi;
import com.devbrackets.android.exomedia.core.video.ResizingTextureView;
import com.google.android.exoplayer2.drm.MediaDrmCallback;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;

import java.util.Map;

/**
 * A {@link VideoViewApi} implementation that uses the ExoPlayer
 * as the backing media player.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class ExoTextureVideoView extends ResizingTextureView implements VideoViewApi {
    protected ExoVideoDelegate delegate;

    public ExoTextureVideoView(Context context) {
        super(context);
        setup();
    }

    public ExoTextureVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public ExoTextureVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    public ExoTextureVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setup();
    }

    @Override
    public void setVideoUri(@Nullable Uri uri) {
        delegate.setVideoUri(uri);
    }

    @Override
    public void setVideoAndSubtitlesUri(Uri videoUri, Uri subtitlesUri) {
        delegate.setVideoAndSubtitlesUri(videoUri, subtitlesUri);
    }

    @Override
    public void setVideoUri(@Nullable Uri uri, @Nullable MediaSource mediaSource) {
        delegate.setVideoUri(uri, mediaSource);
    }

    @Override
    public void setDrmCallback(@Nullable MediaDrmCallback drmCallback) {
        delegate.setDrmCallback(drmCallback);
    }

    @Override
    public boolean restart() {
        return delegate.restart();
    }

    @Override
    public boolean setVolume(@FloatRange(from = 0.0, to = 1.0) float volume) {
        return delegate.setVolume(volume);
    }

    @Override
    public void seekTo(@IntRange(from = 0) long milliseconds) {
        delegate.seekTo(milliseconds);
    }

    @Override
    public Boolean isMediaSeekable() {
        return null;
    }

    @Override
    public boolean isPlaying() {
        return delegate.isPlaying();
    }

    @Override
    public void start() {
        delegate.start();
    }

    @Override
    public void pause() {
        delegate.pause();
    }

    @Override
    public void stopPlayback(boolean clearSurface) {
        delegate.stopPlayback(clearSurface);
    }

    @Override
    public void suspend() {
        delegate.suspend();
    }

    @Override
    public long getDuration() {
        return delegate.getDuration();
    }

    @Override
    public long getCurrentPosition() {
        return delegate.getCurrentPosition();
    }

    @Override
    public int getBufferedPercent() {
        return delegate.getBufferedPercent();
    }

    @Override
    public boolean setPlaybackSpeed(float speed) {
        return delegate.setPlaybackSpeed(speed);
    }

    @Override
    public boolean trackSelectionAvailable() {
        return delegate.trackSelectionAvailable();
    }

    @Override
    public void setTrack(ExoMedia.RendererType trackType, int trackIndex) {
        delegate.setTrack(trackType, trackIndex);
    }

    @Nullable
    @Override
    public Map<ExoMedia.RendererType, TrackGroupArray> getAvailableTracks() {
        return delegate.getAvailableTracks();
    }

    @Override
    public void release() {
        delegate.release();
    }

    @Override
    public void setListenerMux(ListenerMux listenerMux) {
        delegate.setListenerMux(listenerMux);
    }

    @Override
    public void onVideoSizeChanged(int width, int height) {
        if (updateVideoSize(width, height)) {
            requestLayout();
        }
    }

    protected void setup() {
        delegate = new ExoVideoDelegate(getContext(), this);

        setSurfaceTextureListener(new ExoMediaVideoSurfaceTextureListener());
        updateVideoSize(0, 0);
    }

    protected class ExoMediaVideoSurfaceTextureListener implements SurfaceTextureListener {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            delegate.onSurfaceReady(new Surface(surfaceTexture));
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            delegate.onSurfaceDestroyed();
            surfaceTexture.release();

            return true;
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
            // Purposefully left blank
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            // Purposefully left blank
        }
    }
}
