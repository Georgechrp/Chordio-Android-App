package com.unipi.george.chordshub.components

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
import com.unipi.george.chordshub.viewmodels.main.HomeViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.unipi.george.chordshub.R
import com.unipi.george.chordshub.utils.ArtistImageOnly

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
    val colors = listOf(
        MaterialTheme.colorScheme.surface
    )


    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier
            .fillMaxSize()
            .padding(gridPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(songList) { index, (title, songId) ->
            val backgroundColor = colors[index % colors.size]
            if (title != null) {
                if (songId.startsWith("artist:")) {
                    val artistName = title
                    ArtistCardWithImage(
                        artistName = artistName,
                        onClick = {
                            Log.d("CardsView", "Selected artist: $artistName")
                            onSongClick?.invoke(songId)
                        }
                    )
                } else {
                    SongCard(
                        title = title,
                        backgroundColor = backgroundColor,
                        cardHeight = cardHeight ?: 50.dp,
                        cardElevation = cardElevation,
                        cardPadding = cardPadding,
                        fontSize = fontSize,
                        onClick = {
                            Log.d("CardsView", "Selected song ID: $songId")
                            homeViewModel.selectSong(songId)
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
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)

    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(cardPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = fontSize,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}



@Composable
fun ArtistCardWithImage(
    artistName: String,
    cardHeight: Dp = 80.dp,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
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
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
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

