package app.room.ui.misc

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

/** Emojis that float up over the picture when someone reacts. */
val EMOJI_REACTIONS = setOf("❤️", "🔥", "😂", "💡", "👏", "🎉", "😍", "💜", "🥳")

object ReactionBus {
    data class Floater(val id: Long, val emoji: String, val lane: Float)

    private val _flow = MutableStateFlow<List<Floater>>(emptyList())
    val flow = _flow
    private var nextId = 0L
    private val scope = CoroutineScope(Dispatchers.Main)

    fun spawn(emoji: String) {
        val f = Floater(nextId++, emoji, 0.15f + Random.nextFloat() * 0.7f)
        _flow.value = _flow.value.take(14) + f
        scope.launch {
            delay(3400)
            _flow.value = _flow.value.filterNot { it.id == f.id }
        }
    }
}

/** One emoji rising and fading over the video area. */
@Composable
private fun FloatingEmoji(f: ReactionBus.Floater) {
    val density = LocalDensity.current
    var launched by remember { mutableStateOf(false) }
    val t by animateFloatAsState(
        if (launched) 1f else 0f,
        tween(3000, easing = LinearOutSlowInEasing),
        label = "float",
    )
    LaunchedEffect(Unit) { launched = true }
    Text(
        text = f.emoji,
        fontSize = 24.sp,
        modifier = Modifier
            .offset(
                x = with(density) { (f.lane * 320).toDp() },
                y = with(density) { (-t * 260).toDp() },
            )
            .graphicsLayer { alpha = (1f - t).coerceIn(0f, 1f) },
    )
}

/** The overlay itself, laid over the picture. */
@Composable
fun FloatingReactionOverlay(modifier: Modifier = Modifier) {
    val floaters by ReactionBus.flow.collectAsState()
    Box(modifier.fillMaxSize()) {
        floaters.forEach { FloatingEmoji(it) }
    }
}
