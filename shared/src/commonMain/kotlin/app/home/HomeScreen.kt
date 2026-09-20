package app.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import app.LocalGlobalViewmodel
import app.i18n.AppStrings
import app.i18n.strings
import app.preferences.Preferences
import app.preferences.preferencesLoadFailure
import app.preferences.value
import app.protocol.OFFICIAL_SERVER_ADDRESS
import app.protocol.OFFICIAL_SERVER_NAME
import app.protocol.Session
import app.server.ui.ServerHostPanel
import app.theme.DD
import app.uicomponents.frames.NoticeHost
import app.uicomponents.frames.NoticeSeverity
import app.utils.ExitRoomMode
import app.utils.consumePendingShortcut
import app.utils.substringSafely
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import syncplaymobile.shared.generated.resources.Res
import syncplaymobile.shared.generated.resources.synkplay_fg

val officialServers = listOf("syncplay.pl:8995", "syncplay.pl:8996", "syncplay.pl:8997", "syncplay.pl:8998", "syncplay.pl:8999")

/** The official server is one host; only the port varies, so the picker offers just these. */
val officialPorts = listOf("8995", "8996", "8997", "8998", "8999")

private const val OFFICIAL_HOST = OFFICIAL_SERVER_NAME
private const val LOCAL_HOST = "127.0.0.1"

/** Where the join goes: the official server, someone else's, or the one this app hosts. */
private enum class ServerMode { Official, Custom, Host }

/**
 * The Ding Dong start screen: one centred card on the night ground, the cat on its gradient
 * tile, the three labelled fields, the ready check and the gradient join key — exactly as the
 * design screenshots show it.
 */
@Composable
fun HomeScreenUI(viewmodel: HomeViewmodel) {
    ExitRoomMode()
    val globalViewmodel = LocalGlobalViewmodel.current

    // The store is a hot snapshot, so the saved join is read in the first composition.
    val savedConfig by remember { mutableStateOf(JoinConfig.savedConfigNow()) }
    val hasSavedConfig = remember { Preferences.JOIN_CONFIG.value() != null }

    // A pending shortcut joins once, on arrival, through the same caps as the form.
    LaunchedEffect(Unit) {
        consumePendingShortcut()?.let { viewmodel.joinRoom(it.sanitised()) }
    }

    // The settings file could not be read and the app is running on defaults.
    val settingsWereReset = strings.homeSettingsWereReset
    LaunchedEffect(Unit) {
        if (preferencesLoadFailure != null) {
            viewmodel.notices.post(settingsWereReset, NoticeSeverity.Warn, holdMs = 6000L)
        }
    }

    var username by remember(savedConfig) { mutableStateOf(savedConfig.user) }
    var room by remember(savedConfig) { mutableStateOf(savedConfig.room) }
    var mode by remember(savedConfig) {
        mutableStateOf<ServerMode?>(
            when {
                !hasSavedConfig -> ServerMode.Official
                officialServers.contains("${savedConfig.ip.replace(OFFICIAL_SERVER_ADDRESS, OFFICIAL_HOST)}:${savedConfig.port}") -> ServerMode.Official
                savedConfig.ip == LOCAL_HOST || savedConfig.ip == "localhost" -> ServerMode.Host
                else -> ServerMode.Custom
            }
        )
    }
    var address by remember(savedConfig) { mutableStateOf(if (hasSavedConfig) savedConfig.ip else OFFICIAL_HOST) }
    var port by remember(savedConfig) { mutableStateOf(if (hasSavedConfig) savedConfig.port.toString() else "8997") }
    var password by remember(savedConfig) { mutableStateOf(savedConfig.pw) }
    var ready by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<JoinError?>(null) }
    var serverMenu by remember { mutableStateOf(false) }

    fun validate(): JoinError? = when {
        username.isBlank() -> JoinError.Username
        room.isBlank() -> JoinError.Room
        mode == null -> JoinError.ServerChoice
        mode == ServerMode.Custom -> when {
            address.isBlank() -> JoinError.Address
            port.isBlank() -> JoinError.Port
            parsePort(port) == null -> JoinError.PortRange
            else -> null
        }
        else -> null
    }
    fun currentConfig() = when (mode) {
        ServerMode.Host -> JoinConfig(username, room, LOCAL_HOST, parsePort(port.ifBlank { "8999" }) ?: 8999, password)
        else -> JoinConfig(username, room, address, parsePort(port) ?: 8997, password)
    }.sanitised()
    fun doJoin() {
        error = validate()
        if (error == null) globalViewmodel.viewModelScope.launch(Dispatchers.Default) { viewmodel.joinRoom(currentConfig()) }
    }

    val serverText = when (mode) {
        ServerMode.Host -> "$LOCAL_HOST:$port"
        else -> "$address:$port"
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(DD.bg)
            .windowInsetsPadding(WindowInsets.systemBars)
            .imePadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
                .clip(DD.cardShape)
                .background(DD.card)
                .border(1.dp, DD.line.copy(alpha = 0.6f), DD.cardShape)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // The cat on its gradient tile.
            Box(Modifier.size(92.dp).clip(DD.tileShape).background(DD.grad), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(Res.drawable.synkplay_fg),
                    contentDescription = null,
                    modifier = Modifier.size(66.dp),
                )
            }
            Spacer(Modifier.height(14.dp))
            Text("Ding Dong Start Screen", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Watch Party & MPV Player", color = DD.inkDim, fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))

            // Server address: a field-styled row; a tap opens the server sheet.
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Server Address", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(DD.fieldShape)
                        .background(DD.field)
                        .border(1.dp, DD.line, DD.fieldShape)
                        .clickable { serverMenu = true }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(serverText, color = Color.White, fontSize = 15.sp, modifier = Modifier.weight(1f))
                    Text("⌄", color = DD.inkDim, fontSize = 14.sp)
                }
                error?.takeIf { it == JoinError.ServerChoice || it == JoinError.Address || it == JoinError.Port || it == JoinError.PortRange }?.let {
                    Text(it.message(strings), color = Color(0xFFFF6E6E), fontSize = 11.sp)
                }
            }

            Spacer(Modifier.height(14.dp))

            // Custom server rows appear only when the sheet picked "someone else's".
            if (mode == ServerMode.Custom) {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DDField("Address", address, "host or IP", { address = it.trim(); error = null })
                    DDField("Port", port, "8995–8999", { port = it.trim(); error = null })
                    DDField("Password", password, "if any", { password = it.trim() })
                }
                Spacer(Modifier.height(14.dp))
            }
            if (mode == ServerMode.Host) {
                ServerHostPanel(Modifier.fillMaxWidth())
                Spacer(Modifier.height(14.dp))
            }

            DDField(strings.connectRoomname, room, "room name", { typed ->
                // An invite link pasted into the room field fills the whole form.
                val invite = InviteLink.parse(typed)
                if (invite != null) {
                    room = invite.room
                    address = invite.ip
                    port = invite.port.toString()
                    password = invite.pw
                    mode = if (officialServers.contains("${invite.ip}:${invite.port}")) ServerMode.Official else ServerMode.Custom
                } else {
                    room = typed
                }
                error = null
            })
            error?.takeIf { it == JoinError.Room }?.let {
                Text(it.message(strings), color = Color(0xFFFF6E6E), fontSize = 11.sp, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
            }

            Spacer(Modifier.height(14.dp))

            DDField(strings.connectUsername, username, "your name", { username = it; error = null })
            error?.takeIf { it == JoinError.Username }?.let {
                Text(it.message(strings), color = Color(0xFFFF6E6E), fontSize = 11.sp, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
            }

            Spacer(Modifier.height(18.dp))

            // Ready check.
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Ready to watch upon join", color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
                Box(
                    Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (ready) DD.grad else SolidColor(Color.Transparent))
                        .border(1.dp, if (ready) Color.Transparent else DD.line, RoundedCornerShape(6.dp))
                        .clickable { ready = !ready },
                    contentAlignment = Alignment.Center,
                ) {
                    if (ready) Text("✓", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))

            // The gradient join key.
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DD.grad)
                    .clickable { doJoin() },
                contentAlignment = Alignment.Center,
            ) {
                Text(strings.connectButtonJoin, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }

        // The server sheet: dims the page and lists the five official rooms plus the two extras.
        if (serverMenu) {
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)).clickable { serverMenu = false },
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    Modifier
                        .widthIn(max = 360.dp)
                        .fillMaxWidth()
                        .padding(32.dp)
                        .clip(DD.cardShape)
                        .background(DD.card)
                        .border(1.dp, DD.line, DD.cardShape)
                        .padding(10.dp),
                ) {
                    officialServers.forEach { srv ->
                        Text(
                            srv,
                            color = if (serverText == srv) DD.pink else Color.White,
                            fontSize = 13.sp,
                            modifier = Modifier.fillMaxWidth().clickable {
                                mode = ServerMode.Official
                                address = OFFICIAL_HOST
                                port = srv.substringAfter(':')
                                password = ""
                                error = null
                                serverMenu = false
                            }.padding(horizontal = 12.dp, vertical = 10.dp),
                        )
                    }
                    Text(
                        "Someone else's server…",
                        color = DD.orchid,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth().clickable {
                            mode = ServerMode.Custom
                            address = ""
                            port = ""
                            password = ""
                            serverMenu = false
                        }.padding(horizontal = 12.dp, vertical = 10.dp),
                    )
                    Text(
                        "Host on this device",
                        color = DD.orchid,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth().clickable {
                            mode = ServerMode.Host
                            address = LOCAL_HOST
                            serverMenu = false
                        }.padding(horizontal = 12.dp, vertical = 10.dp),
                    )
                }
            }
        }

        NoticeHost(
            queue = viewmodel.notices,
            overVideo = false,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .imePadding()
                .padding(16.dp),
        )
    }
}

/** A labelled field of the start card: bold label over a dark bordered well. */
@Composable
private fun DDField(label: String, value: String, placeholder: String, onValueChange: (String) -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Box(
            Modifier
                .fillMaxWidth()
                .clip(DD.fieldShape)
                .background(DD.field)
                .border(1.dp, DD.line, DD.fieldShape)
                .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
                modifier = Modifier.fillMaxWidth(),
            )
            if (value.isEmpty()) Text(placeholder, color = DD.inkDim, fontSize = 15.sp)
        }
    }
}

/**
 * Backslashes out, trimmed, capped: the same on the form and on a shortcut. A room pasted as
 * the whole `+name:HASH:PASSWORD` string the app prints on creation is split, so it joins the
 * managed room and identifies as its operator instead of creating a room by that literal name.
 */
private fun JoinConfig.sanitised(): JoinConfig {
    val (roomName, operator) = InviteLink.splitOperatorRoom(room)
    return copy(
        user = user.replace("\\", "").trim().substringSafely(0, 149),
        room = roomName.replace("\\", "").trim().substringSafely(0, Session.MAX_ROOM_NAME_CHARS),
        operatorPassword = operator.ifEmpty { operatorPassword },
    )
}

/** Which field the join form is complaining about. The wording comes from the current language. */
private enum class JoinError { Username, Room, ServerChoice, Address, Port, PortRange }

private fun JoinError.message(s: AppStrings): String = when (this) {
    JoinError.Username -> s.connectUsernameEmptyError
    JoinError.Room -> s.connectRoomnameEmptyError
    JoinError.ServerChoice -> s.connectServerPickError
    JoinError.Address -> s.connectAddressEmptyError
    JoinError.Port -> s.connectPortEmptyError
    JoinError.PortRange -> s.connectPortRangeError
}
