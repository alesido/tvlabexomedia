package com.devbrackets.android.exomedia.core.exoplayer;

import android.util.Log;

import com.google.android.exoplayer2.Player;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created on 2/15/18.
 */

class ExoEventWatcher extends Player.DefaultEventListener
{
    public static final String TAG = ExoEventWatcher.class.getSimpleName();

    interface Listener {
        void onStaleImageTimeout();
        void onLoadingWaitTimeout();
        void onPlayerStaleTimeout();
        void onOddDiscontinuity();
    }

    private static ExoEventWatcher instance;

    public static ExoEventWatcher getInstance() {
        if (null == instance) {
            instance = new ExoEventWatcher();
        }
        return instance;
    }

    private Set<Listener> listeners = new LinkedHashSet<>();

    private Long staleImageTimeoutMillis;


    private ExoEventWatcher() {
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public void setStaleImageTimeout(long timeoutMillis) {
        this.staleImageTimeoutMillis = timeoutMillis;
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        super.onLoadingChanged(isLoading);
        Log.d(TAG, "onLoadingChanged: ");
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        super.onPlayerStateChanged(playWhenReady, playbackState);
        Log.d(TAG, "onPlayerStateChanged: now state: " + playbackState);
    }

    @Override
    public void onSeekProcessed() {
        super.onSeekProcessed();
    }

    @Override
    public void onPositionDiscontinuity(int reason) {
        super.onPositionDiscontinuity(reason);
    }
}
