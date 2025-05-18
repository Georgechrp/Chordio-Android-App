package com.unipi.george.chordshub.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


/*
*   My custom Top Bar used by 3 main screens(Home, Search, Library)
*   and 2 functions(FilterButton, FilterRow)
*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppTopBar(
    imageUrl: String?,
    onMenuClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
            ) {
                UserProfileImage(
                    imageUrl = imageUrl,
                    size = 36.dp,
                    border = true,
                    onClick = onMenuClick
                )

                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    this@Row.content()
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
            scrolledContainerColor = Color.Transparent

        )
    )
}


@Composable
fun FilterButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    selectedColor: Color = MaterialTheme.colorScheme.secondary ,
    defaultColor: Color = MaterialTheme.colorScheme.primary
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) selectedColor else defaultColor
        ),
        modifier = Modifier
            .height(30.dp)
            .padding(horizontal = 4.dp)
    ) {
        Text(
            text,
            maxLines = 1,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun FilterRow(
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    filters: List<String> = listOf("All", "Artists", "Downloads")
) {
    val scrollState = rememberScrollState()

    Row(modifier = Modifier.horizontalScroll(scrollState)) {
        filters.forEach { filter ->
            FilterButton(
                text = filter,
                isSelected = filter == selectedFilter,
                onClick = { onFilterChange(filter) }
            )
        }
    }
}

