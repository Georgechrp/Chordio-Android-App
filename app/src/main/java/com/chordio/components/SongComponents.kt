package com.chordio.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import com.chordio.viewmodels.main.HomeViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.chordio.screens.FavoritePlaylistCard
import com.chordio.utils.ArtistImageOnly

/*
*   3 functions about print Cards-Songs and clickable chords
*/

@Composable
fun CardsView(
    songList: List<Pair<String?, String>>,
    homeViewModel: HomeViewModel,
    selectedTitle: MutableState<String?>,
    columns: Int = 2,
    cardHeight: Dp? = null,
    cardElevation: Dp = 8.dp,
    cardPadding: Dp = 16.dp,
    gridPadding: Dp = 16.dp,
    fontSize: TextUnit = 16.sp,
    onSongClick: ((songId: String) -> Unit)? = null
) {
    val colors = listOf(MaterialTheme.colorScheme.surface)

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier
            .fillMaxSize()
            .heightIn(min = 100.dp, max = 600.dp)
            .padding(gridPadding),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(songList) { index, (title, songId) ->
            val backgroundColor = colors[index % colors.size]
            if (title != null) {
                val parts = title.split(" - ")
                val titleText = parts.first()
                val artistText = if (parts.size > 1) parts[1] else null

                if (title == "Αγαπημένα Τραγούδια") {
                    FavoritePlaylistCard(
                        title = "For you ",
                        onClick = {
                            Log.d("CardsView", "▶️ Opening favorite songs")
                            onSongClick?.invoke(songId)
                        }
                    )

                } else if (title == "Hits") {
                    FavoritePlaylistCard(
                        title = "Hits ",
                        onClick = {
                            Log.d("CardsView", "▶️ Opening favorite songs")
                            onSongClick?.invoke(songId)
                        }
                    )
                }

                else if (songId.startsWith("artist:")) {
                    ArtistCardWithImage(
                        artistName = titleText,
                        onClick = {
                            Log.d("CardsView", "Selected artist: $titleText")
                            onSongClick?.invoke(songId)
                        }
                    )
                } else {
                SongCard(
                        title = titleText,
                        artistName = artistText,
                        backgroundColor = backgroundColor,
                        cardHeight = cardHeight ?: 80.dp,
                        cardElevation = cardElevation,
                        cardPadding = cardPadding,
                        fontSize = fontSize,
                        onClick = {
                            Log.d("CardsView", "Selected song ID: $songId")
                            homeViewModel.selectSong(songId)
                            homeViewModel.setSelectedSong(emptyList(), artistText)
                            selectedTitle.value = title
                            onSongClick?.invoke(songId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SongCard(
    title: String,
    artistName: String? = null,
    backgroundColor: Color,
    cardHeight: Dp? = null,
    cardElevation: Dp = 8.dp,
    cardPadding: Dp = 16.dp,
    fontSize: TextUnit = 16.sp,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (cardHeight != null) Modifier.height(cardHeight) else Modifier.aspectRatio(1f))
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(cardPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                fontSize = fontSize,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (!artistName.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = artistName,
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


@Composable
fun ArtistCardWithImage(
    artistName: String,
    cardHeight: Dp = 60.dp,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight)
            .clickable { onClick() },
        shape = RoundedCornerShape(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ArtistImageOnly(
                artistName = artistName,
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = artistName,
                fontSize = 16.sp,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

