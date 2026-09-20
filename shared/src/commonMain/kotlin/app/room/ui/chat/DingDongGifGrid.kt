package app.room.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import app.uicomponents.AnimatedImage

/**
 * Curated reaction GIFs straight from Giphy's public media CDN, so the GIF drawer works with
 * no API key. Every URL was verified live before shipping.
 */
private val DD_GIFS = listOf(
    "https://media.giphy.com/media/JIX9t2j0ZTN98/giphy.gif",
    "https://media.giphy.com/media/13HgwGsXF0aiGY/giphy.gif",
    "https://media.giphy.com/media/3o6Zt4860l4LugQ2CY/giphy.gif",
    "https://media.giphy.com/media/l0HlBOv7X0jGS9gFS/giphy.gif",
    "https://media.giphy.com/media/3o7aCTfyhYawdOXcFW/giphy.gif",
    "https://media.giphy.com/media/26ufdipQqU2lhNA4g/giphy.gif",
    "https://media.giphy.com/media/3o7abKhOpu0NwenH3O/giphy.gif",
    "https://media.giphy.com/media/l0MYt5jPR6QX5pnqM/giphy.gif",
    "https://media.giphy.com/media/3o6Zta8bQlTKE9sQrm/giphy.gif",
    "https://media.giphy.com/media/26BRuo6sLetdllPAQ/giphy.gif",
    "https://media.giphy.com/media/3o7abB06u9bNzA8LC8/giphy.gif",
    "https://media.giphy.com/media/l0HlQ7F1X1bYcPWDe/giphy.gif",
    "https://media.giphy.com/media/3o7aCZ7T9GC69ur7EY/giphy.gif",
    "https://media.giphy.com/media/26u4cqiYI30juCOGY/giphy.gif",
    "https://media.giphy.com/media/3o7abAHdYvZdBNnGZq/giphy.gif",
)

/** A three-column wall of reaction GIFs; a tap flings one into the room. */
@Composable
fun DingDongGifGrid(onPick: (String) -> Unit, modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(DD_GIFS) { url ->
            AnimatedImage(
                url = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onPick(url) },
            )
        }
    }
}
