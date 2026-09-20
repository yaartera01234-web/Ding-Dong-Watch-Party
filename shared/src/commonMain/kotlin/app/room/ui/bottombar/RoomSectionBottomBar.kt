package app.room.ui.bottombar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeGestures
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import app.LocalRoomViewmodel
import app.room.ui.misc.ReactionBus
import app.player.Playback
import app.theme.DD
import app.theme.Space
import app.theme.Type
import app.uicomponents.controls.Icon
import app.uicomponents.controls.PauseGlyph
import app.uicomponents.controls.PlayGlyph
import app.uicomponents.controls.RowGap
import app.uicomponents.controls.Text
import app.utils.timestampFromMillis

/** The reactions a single tap flings into the room as chat lines. */
private val REACTIONS = listOf("❤️", "🔥", "😂", "💡")

/**
 * The transport. Landscape gets the full strip — thin seek line over one row with the outlined
 * play/pause key, the reaction pill, the running time, the control panel and the gradient Add
 * Media key. Portrait keeps the mini player clean: just the play key, the time and Add Media,
 * sitting at the foot of the picture like the design shot.
 */
@Composable
fun RoomBottomBarSection(modifier: Modifier) {
    val viewmodel = LocalRoomViewmodel.current
    val hasVideo by viewmodel.hasVideo.collectAsState()
    val playing by viewmodel.playerManager.isNowPlaying.collectAsState()
    val positionMs by viewmodel.playerManager.timeCurrentMillis.collectAsState()
    val durationMs by viewmodel.playerManager.timeFullMillis.collectAsState()
    val container = LocalWindowInfo.current.containerSize
    val tall = container.height > container.width

    Box(modifier.windowInsetsPadding(WindowInsets.safeGestures)) {
        if (tall) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .zIndex(999f)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (hasVideo) {
                    PlayPauseKey(viewmodel, playing, small = true)
                    RowGap(Space.gap)
                    Text(
                        text = "${timestampFromMillis(positionMs)} / ${timestampFromMillis(durationMs)}",
                        style = Type.value,
                        color = Color.White,
                        maxLines = 1,
                    )
                }
                Spacer(Modifier.weight(1f))
                RoomMediaAddButton()
            }
        } else {
            Column(
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .zIndex(999f),
            ) {
                if (hasVideo) {
                    RoomSeekbar(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                ) {
                    if (hasVideo) {
                        PlayPauseKey(viewmodel, playing, small = false)
                        RowGap(Space.gap)
                        // The reaction pill.
                        Row(
                            Modifier
                                .clip(DD.pillShape)
                                .background(Color.Black.copy(alpha = 0.35f))
                                .border(1.dp, DD.line, DD.pillShape)
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            REACTIONS.forEach { emoji ->
                                Text(
                                    text = emoji,
                                    style = Type.note.copy(fontSize = 14.sp),
                                    color = Color.White,
                                    modifier = Modifier
                                        .padding(horizontal = 5.dp)
                                        .clickable {
                                        viewmodel.dispatcher.sendMessage(emoji)
                                        ReactionBus.spawn(emoji)
                                    },
                                )
                            }
                        }
                        RowGap(Space.gap)
                        Text(
                            text = "${timestampFromMillis(positionMs)} / ${timestampFromMillis(durationMs)}",
                            style = Type.value,
                            color = Color.White,
                            maxLines = 1,
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    if (hasVideo) {
                        RoomControlPanelButton(modifier = Modifier)
                    }
                    RoomMediaAddButton()
                }
            }
        }
    }
}

/**
 * The portrait transport, drawn inside the chat column just above the composer: the play
 * key and the running time at the start, the gradient Add Media key at the end. Nothing
 * reaches the top of the picture but the Fullscreen key.
 */
@Composable
fun RoomPortraitBar() {
    val viewmodel = LocalRoomViewmodel.current
    val hasVideo by viewmodel.hasVideo.collectAsState()
    val playing by viewmodel.playerManager.isNowPlaying.collectAsState()
    val positionMs by viewmodel.playerManager.timeCurrentMillis.collectAsState()
    val durationMs by viewmodel.playerManager.timeFullMillis.collectAsState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (hasVideo) {
            PlayPauseKey(viewmodel, playing, small = true)
            RowGap(Space.gap)
            Text(
                text = "${timestampFromMillis(positionMs)} / ${timestampFromMillis(durationMs)}",
                style = Type.value,
                color = Color.White,
                maxLines = 1,
            )
        }
        Spacer(Modifier.weight(1f))
        RoomMediaAddButton()
    }
}

/** The outlined play/pause key shared by both arrangements. */
@Composable
private fun PlayPauseKey(viewmodel: app.room.RoomViewmodel, playing: Boolean, small: Boolean) {
    Box(
        Modifier
            .size(if (small) 36.dp else 40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .border(1.dp, DD.line, RoundedCornerShape(12.dp))
            .clickable {
                viewmodel.dispatcher.controlPlayback(if (playing) Playback.PAUSE else Playback.PLAY, true)
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (playing) PauseGlyph else PlayGlyph,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(if (small) 16.dp else 18.dp),
        )
    }
}
