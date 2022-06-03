/*
 * Copyright (C) 2017 - 2018 ExoMedia Contributors
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

package com.devbrackets.android.exomedia.core.source.builder;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsDataSourceFactory;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.hls.playlist.HlsPlaylistParserFactory;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.TransferListener;

public class HlsMediaSourceBuilder extends MediaSourceBuilder {

    private HlsPlaylistParserFactory customPlaylistParserFactory;

    @NonNull
    @Override
    public MediaSource build(@NonNull Context context, @NonNull Uri uri, @NonNull String userAgent, @NonNull Handler handler, @Nullable TransferListener transferListener) {
        DataSource.Factory dataSourceFactory = buildDataSourceFactory(context, userAgent, transferListener);

        HlsMediaSource.Factory hlsMediaSourceFactory = new HlsMediaSource.Factory(dataSourceFactory);
        if (customPlaylistParserFactory != null) {
            hlsMediaSourceFactory.setPlaylistParserFactory(customPlaylistParserFactory);
        }

        return hlsMediaSourceFactory.createMediaSource(MediaItem.fromUri(uri));
    }

    public void setCustomPlaylistParserFactory(HlsPlaylistParserFactory customPlaylistParserFactory) {
        this.customPlaylistParserFactory = customPlaylistParserFactory;
    }
}
