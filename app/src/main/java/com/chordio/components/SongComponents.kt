package com.chordio.components

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.chordio.viewmodels.main.HomeViewModel
import com.chordio.screens.FavoritePlaylistCard
import com.chordio.utils.ArtistImageOnly

@Composable
fun CardsView(
    songList: List<Pair<String?, String>>,
    homeViewModel: HomeViewModel,
    selectedTitle: MutableState<String?>,
    columns: Int = 2,
    cardHeight: Dp? = null,
    cardElevation: Dp = 6.dp,
    cardPadding: Dp = 12.dp,
    gridPadding: Dp = 16.dp,
    fontSize: TextUnit = 15.sp,
    onSongClick: ((songId: String) -> Unit)? = null
) {
    val colors = listOf(MaterialTheme.colorScheme.surfaceVariant)

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier
            .fillMaxSize()
            .padding(gridPadding),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(songList) { index, (title, songId) ->
            if (title != null) {
                val parts = title.split(" - ")
                val titleText = parts.first()
                val artistText = if (parts.size > 1) parts[1] else null

                when {
                    title == "For you" || title == "Hits" -> {
                        FavoritePlaylistCard(
                            title = title,
                            onClick = { onSongClick?.invoke(songId) }
                        )
                    }

                    songId.startsWith("artist:") -> {
                        ArtistCardWithImage(
                            artistName = titleText,
                            onClick = { onSongClick?.invoke(songId) }
                        )
                    }

                    else -> {
                        SongCardModern(
                            title = titleText,
                            artistName = artistText,
                            backgroundColor = colors[index % colors.size],
                            cardHeight = cardHeight ?: 88.dp,
                            cardElevation = cardElevation,
                            cardPadding = cardPadding,
                            fontSize = fontSize,
                            onClick = {
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
}

@Composable
fun SongCardModern(
    title: String,
    artistName: String? = null,
    backgroundColor: Color,
    cardHeight: Dp = 88.dp,
    cardElevation: Dp = 6.dp,
    cardPadding: Dp = 12.dp,
    fontSize: TextUnit = 15.sp,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_media_play),
                contentDescription = "Play icon",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = fontSize,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!artistName.isNullOrBlank()) {
                    Text(
                        text = artistName,
                        fontSize = 13.sp,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun ArtistCardWithImage(
    artistName: String,
    cardHeight: Dp = 72.dp,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                    .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
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
