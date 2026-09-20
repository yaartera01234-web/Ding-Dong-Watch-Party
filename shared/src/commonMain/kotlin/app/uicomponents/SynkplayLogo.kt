package app.uicomponents

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import syncplaymobile.shared.generated.resources.Res
import syncplaymobile.shared.generated.resources.synkplay_fg

/**
 * The Ding Dong app mark: the black-and-white cat with its popcorn bucket. Call sites size it;
 * it sits on the app's dark ground, so the transparent foreground art needs no plate here.
 */
@Composable
fun SynkplayLogo(modifier: Modifier) {
    Image(
        painter = painterResource(Res.drawable.synkplay_fg),
        contentDescription = null,
        modifier = modifier,
    )
}
