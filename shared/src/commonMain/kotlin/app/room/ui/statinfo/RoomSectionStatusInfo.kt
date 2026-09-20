package app.room.ui.statinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import app.LocalRoomViewmodel
import app.i18n.roomUserCount
import app.i18n.strings
import app.preferences.Preferences
import app.preferences.set
import app.protocol.models.ConnectionState
import app.protocol.sync.AutoplayState
import app.theme.Radius
import app.theme.Space
import app.theme.Type
import app.theme.palette
import app.uicomponents.chromeSurface
import app.uicomponents.controls.GlyphButton
import app.uicomponents.controls.Icon
import app.uicomponents.controls.LockGlyph
import app.uicomponents.controls.RowGap
import app.uicomponents.controls.Tag
import app.uicomponents.controls.Text
import app.uicomponents.controls.UnlockGlyph
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import kotlinx.coroutines.launch

private val EPISODE = Regex("(?:s|season)(\\d{1,2})(?:e|episode)(\\d{1,2})")

/**
 * The status line: a 6dp connection square, the room name, the user count or the connection
 * state, and the episode tag when the file name carries one. On portrait phones it splits into
 * two floating glass chips hugging the corners, so the mini player stays clean; landscape keeps
 * the single chrome pill.
 */
@Composable
fun RoomStatusInfoSection(modifier: Modifier = Modifier) {
    val viewmodel = LocalRoomViewmodel.current
    val p = palette
    val connectionState by viewmodel.networkManager.state.collectAsState()
    val userList by viewmodel.session.userList.collectAsState()
    val encrypted by viewmodel.networkManager.encrypted.collectAsState()
    val autoplay by viewmodel.readiness.state.collectAsState()
    val readiness by viewmodel.readiness.summary.collectAsState()

    // The server's list includes us; while joining, show one instead of a flash of zero.
    val totalUsers = when {
        userList.isNotEmpty() -> userList.size
        connectionState == ConnectionState.CONNECTED -> 1
        else -> 0
    }
    val square = when (connectionState) {
        ConnectionState.CONNECTED -> p.ok
        ConnectionState.CONNECTING, ConnectionState.SCHEDULING_RECONNECT -> p.accent
        ConnectionState.DISCONNECTED -> p.bad
    }
    val readinessLine: String? = when {
        connectionState != ConnectionState.CONNECTED -> null
        autoplay is AutoplayState.CountingDown ->
            strings.roomStartingIn((autoplay as AutoplayState.CountingDown).secondsLeft)
        readiness.alone -> null
        readiness.notReady.size == 1 -> strings.roomWaitingForOne(readiness.notReady.single())
        readiness.notReady.size > 1 -> strings.roomWaitingForMany(readiness.notReady.size)
        else -> strings.roomEveryoneReady(readiness.participantCount)
    }

    val state = when (connectionState) {
        ConnectionState.CONNECTED -> readinessLine
            ?: strings.roomUserCount(totalUsers)
        ConnectionState.CONNECTING -> strings.roomConnecting
        ConnectionState.SCHEDULING_RECONNECT -> strings.roomReconnecting
        ConnectionState.DISCONNECTED -> strings.roomPingDisconnected
    }
    val media by viewmodel.playerManager.media.collectAsState()
    val episode = remember(media?.fileName) {
        media?.fileName?.lowercase()?.let { EPISODE.find(it) }?.let { m ->
            "S" + m.groupValues[1].padStart(2, '0') + "E" + m.groupValues[2].padStart(2, '0')
        }
    }

    val container = LocalWindowInfo.current.containerSize
    val portrait = container.height > container.width

    /** The shared innards: dot, lock, room, state, reconnect, episode. */
    val inner: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {
        Box(Modifier.size(6.dp).background(square, Radius.tightShape))
        if (connectionState == ConnectionState.CONNECTED) {
            RowGap(Space.gapTight)
            Icon(
                imageVector = if (encrypted) LockGlyph else UnlockGlyph,
                contentDescription = if (encrypted) strings.roomConnectionEncrypted
                    else strings.roomConnectionPlaintext,
                tint = if (encrypted) p.ok else p.inkDim,
                modifier = Modifier.size(14.dp),
            )
        }
        RowGap(Space.gapTight + 2.dp)
        Text(
            text = viewmodel.session.currentRoom,
            style = Type.label,
            color = p.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        RowGap(Space.gapTight + 2.dp)
        Text(state, style = Type.value, color = p.inkDim, maxLines = 1)
        if (connectionState == ConnectionState.DISCONNECTED || connectionState == ConnectionState.SCHEDULING_RECONNECT) {
            RowGap(Space.gapTight)
            GlyphButton(
                icon = Icons.Filled.Refresh,
                name = strings.roomReconnectNow,
                tint = p.accent,
                size = Space.glyph,
            ) { viewmodel.networkManager.reconnectNow() }
        }
        if (episode != null) {
            RowGap(Space.gapTight + 2.dp)
            Tag(episode)
        }
    }

    if (portrait) {
        // Two floating glass chips in the corners; the picture between them stays untouched.
        Row(
            modifier = modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }
                .padding(horizontal = Space.gapTight),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                Modifier
                    .chromeSurface(Radius.panelShape)
                    .heightIn(min = Space.rowCompact)
                    .padding(horizontal = Space.gap),
                verticalAlignment = Alignment.CenterVertically,
            ) { inner() }
            Spacer(Modifier.weight(1f))
            Box(
                Modifier
                    .chromeSurface(Radius.panelShape)
                    .heightIn(min = Space.rowCompact)
                    .clickable { viewmodel.viewModelScope.launch { Preferences.ROOM_ALLOW_PORTRAIT.set(false) } }
                    .padding(horizontal = Space.gap),
                contentAlignment = Alignment.Center,
            ) {
                Text("Fullscreen ⛶", style = Type.value, color = p.ink)
            }
        }
    } else {
        Row(
            modifier = modifier
                .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }
                .chromeSurface(Radius.panelShape)
                .heightIn(min = Space.rowCompact)
                .padding(horizontal = Space.gap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            inner()
            RowGap(Space.gapTight)
            Box(
                Modifier
                    .clip(Radius.panelShape)
                    .border(Space.hair, p.rule, Radius.panelShape)
                    .clickable { viewmodel.viewModelScope.launch { Preferences.ROOM_ALLOW_PORTRAIT.set(true) } }
                    .padding(horizontal = Space.gap, vertical = 3.dp),
            ) {
                Text("⛶ Mini", style = Type.value, color = p.ink)
            }
        }
    }
}
