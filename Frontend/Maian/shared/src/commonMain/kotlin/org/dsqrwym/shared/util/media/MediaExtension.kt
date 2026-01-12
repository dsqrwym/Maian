package org.dsqrwym.shared.util.media

import io.github.kdroidfilter.composemediaplayer.VideoPlayerState

fun VideoPlayerState.isFinished(): Boolean {
    return !isPlaying &&
            !isLoading &&
            !userDragging &&
            positionText == durationText &&
            !loop
}
