package app.room.ui.misc

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import app.LocalRoomViewmodel
import app.theme.DD
import app.theme.palette
import app.uicomponents.SynkplayLogo

/**
 * The room ground before a file loads: the cat inside a slowly spinning pink-violet ring with
 * a soft glow behind it, like the concept art.
 */
@Composable
fun RoomBackgroundArtwork() {
    val viewmodel = LocalRoomViewmodel.current
    val isInPipMode by viewmodel.uiState.hasEnteredPipMode.collectAsState()
    val p = palette
    Box(Modifier.fillMaxSize().background(p.ground), contentAlignment = Alignment.Center) {
        if (!isInPipMode) {
            Box(
                Modifier.size(260.dp)
                    .background(Brush.radialGradient(listOf(DD.pink.copy(0.22f), DD.violet.copy(0.10f), androidx.compose.ui.graphics.Color.Transparent)), CircleShape)
            )
            val spin = rememberInfiniteTransition(label = "ring")
            val angle by spin.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing)),
                label = "ringAngle",
            )
            Box(Modifier.size(190.dp).graphicsLayer { rotationZ = angle }) {
                Box(Modifier.fillMaxSize().background(Brush.sweepGradient(listOf(DD.pink, DD.orchid, DD.violet, DD.pink)), CircleShape))
                Box(Modifier.size(164.dp).align(Alignment.Center).background(p.ground, CircleShape))
            }
        }
        SynkplayLogo(modifier = Modifier.size(if (isInPipMode) 40.dp else 110.dp).alpha(if (isInPipMode) 0.35f else 0.9f))
    }
}
