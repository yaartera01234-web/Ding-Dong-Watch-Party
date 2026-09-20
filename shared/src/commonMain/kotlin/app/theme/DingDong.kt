package app.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * The Ding Dong night palette, taken straight from the design screenshots: a near-black purple
 * ground, dark purple cards and fields, and the hot-pink to violet brand gradient on every
 * primary surface.
 */
object DD {
    val bg = Color(0xFF0B0714)      // page ground
    val card = Color(0xFF161027)    // raised cards
    val field = Color(0xFF0E0A1C)   // input wells
    val line = Color(0xFF3A2B57)    // hairlines and borders
    val pink = Color(0xFFFF2E9A)    // gradient start
    val orchid = Color(0xFFC13FD8)  // gradient mid
    val violet = Color(0xFF7A2FE0)  // gradient end
    val inkDim = Color(0xFF9A8FB5)  // placeholder / secondary text

    val grad = Brush.horizontalGradient(listOf(pink, orchid, violet))

    val tileShape = RoundedCornerShape(22.dp)
    val cardShape = RoundedCornerShape(24.dp)
    val fieldShape = RoundedCornerShape(12.dp)
    val pillShape = RoundedCornerShape(50.dp)
}
