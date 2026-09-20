package app.room.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.theme.DD
import app.uicomponents.AnimatedImage
import app.utils.httpClient
import app.utils.ioDispatcher
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.encodeURLParameter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Fallback wall of reaction GIFs on Giphy's public CDN, used before/without a search. */
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

@Serializable
private data class RedditResp(val data: RedditData = RedditData())

@Serializable
private data class RedditData(val children: List<RedditChild> = emptyList())

@Serializable
private data class RedditChild(val data: RedditPost = RedditPost())

@Serializable
private data class RedditPost(val url: String = "")

private val gifJson = Json { ignoreUnknownKeys = true; isLenient = true }

/**
 * The Ding Dong GIF drawer: a search bar over a three-column wall. Results come from the
 * public r/gifs JSON API (no key needed); the curated Giphy wall is the fallback and the
 * starting view. A tap flings the chosen GIF into the room.
 */
@Composable
fun DingDongGifGrid(onPick: (String) -> Unit, modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }
    var urls by remember { mutableStateOf(DD_GIFS) }
    val scope = rememberCoroutineScope()

    fun load(q: String) {
        scope.launch {
            urls = withContext(ioDispatcher) {
                try {
                    val endpoint = if (q.isBlank()) {
                        "https://www.reddit.com/r/gifs/hot.json?limit=30&raw_json=1"
                    } else {
                        "https://www.reddit.com/r/gifs/search.json?q=${q.encodeURLParameter()}&restrict_sr=1&limit=30&raw_json=1"
                    }
                    val body = httpClient.get(endpoint) {
                        header("User-Agent", "DingDongWatchParty/1.0")
                    }.bodyAsText()
                    val found = gifJson.decodeFromString<RedditResp>(body)
                        .data.children.map { it.data.url }
                        .filter { u ->
                            u.startsWith("https://") && (
                                u.endsWith(".gif") || u.contains("i.redd.it") ||
                                    u.contains("giphy.com") || u.contains("tenor.com")
                                )
                        }
                        .distinct()
                        .take(24)
                    found.ifEmpty { DD_GIFS }
                } catch (_: Exception) {
                    DD_GIFS
                }
            }
        }
    }

    Column(modifier) {
        // The search bar.
        Row(
            Modifier
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .clip(DD.pillShape)
                .background(DD.field)
                .border(1.dp, DD.line, DD.pillShape)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                modifier = Modifier.weight(1f).padding(vertical = 6.dp),
                decorationBox = { inner ->
                    Box {
                        if (query.isEmpty()) Text("Search GIFs…", color = DD.inkDim, fontSize = 13.sp)
                        inner()
                    }
                },
            )
            Box(
                Modifier
                    .padding(start = 6.dp)
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(DD.grad)
                    .clickable { load(query) },
                contentAlignment = Alignment.Center,
            ) {
                Text("🔍", fontSize = 13.sp)
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(urls) { url ->
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
}
