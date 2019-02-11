/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.video.controls.video;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.RendererCapabilities;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector.MappedTrackInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Helper class for displaying track selection dialogs.
 */
public final class TrackSelectionHelper {

    /**
     * Sorting resolutions logic
     */
    public static Comparator<TOIFormat> formatComparator = new Comparator<TOIFormat>() {

        @Override
        public int compare(TOIFormat o1, TOIFormat o2) {
            if (o1.format.height == o2.format.height) {
                if (o1.format.bitrate == o2.format.bitrate) {
                    return 0;
                } else if (o1.format.bitrate > o2.format.bitrate) {
                    return 1;
                } else {
                    return -1;
                }
            } else if (o1.format.height > o2.format.height) {
                return 1;
            } else {
                return -1;
            }
        }
    };

    public TrackSelectionHelper() {
    }

    public ArrayList<TOIFormat> getResolutions(SimpleExoPlayer mSimpleExoPlayer, DefaultTrackSelector trackSelector) {
        ArrayList<TOIFormat> returnArray = null;
        int renderindex = -1;
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        for (int i = 0; mappedTrackInfo != null && i < mappedTrackInfo.length; i++) {
            TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(i);
            if (trackGroups.length != 0) {
                if (mSimpleExoPlayer.getRendererType(i) == C.TRACK_TYPE_VIDEO) {
                    renderindex = i;
                    returnArray = getVideoTracks(trackSelector.getCurrentMappedTrackInfo(), i, trackGroups);
                }
            }
        }
        if (returnArray == null) {
            returnArray = new ArrayList<>();
        }
        /**
         * Auto resolution default added on 0th position
         */
        returnArray.add(0, new TOIFormat(null, renderindex, -1, -1));

        return returnArray;
    }

    public ArrayList<TOIFormat> getVideoTracks(MappedTrackInfo trackInfo, int rendererIndex, TrackGroupArray trackGroups) {
        ArrayList<TOIFormat> formats = new ArrayList<>();
        for (int groupIndex = 0; groupIndex < trackGroups.length; groupIndex++) {
            boolean isGroupAdaptive = trackInfo.getAdaptiveSupport(rendererIndex, groupIndex, false) != RendererCapabilities.ADAPTIVE_NOT_SUPPORTED && trackGroups.get(groupIndex).length > 1;
            if (!isGroupAdaptive)
                continue;
            TrackGroup group = trackGroups.get(groupIndex);
            for (int trackIndex = 0; trackIndex < group.length; trackIndex++) {
                if (trackInfo.getTrackFormatSupport(rendererIndex, groupIndex, trackIndex) == RendererCapabilities.FORMAT_HANDLED) {
                    formats.add(new TOIFormat(group.getFormat(trackIndex), rendererIndex, groupIndex, trackIndex));
                }
            }
        }
        Collections.sort(formats, formatComparator);
        int arrayIndex = 0;
        while (arrayIndex < formats.size() - 1) {
            if (formats.get(arrayIndex).format.height == formats.get(arrayIndex + 1).format.height) {
                formats.remove(arrayIndex);
                continue;
            }
            arrayIndex += 1;
        }
        return formats;
    }

    /**
     * format == null if Resolution is Auto
     */
    public class TOIFormat {
        Format format;
        int trackIndex;
        int groupIndex;
        int renderIndex;

        public TOIFormat(Format format, int renderindex, int groupIndex, int trackIndex) {
            this.format = format;
            this.trackIndex = trackIndex;
            this.groupIndex = groupIndex;
            this.renderIndex = renderindex;
        }

        public Format getFormat() {
            return format;
        }

        public int getTrackIndex() {
            return trackIndex;
        }

        public int getGroupIndex() {
            return groupIndex;
        }
    }

}
