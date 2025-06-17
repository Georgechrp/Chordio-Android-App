package com.chordio.screens.slidemenu.options

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.chordio.R
import com.chordio.viewmodels.user.WeeklyStatsViewModel

@Composable
fun WeeklyStatsScreen(
    userId: String,
    navController: NavController,
    viewModel: WeeklyStatsViewModel = viewModel()
) {
    val weeklyStats by viewModel.weeklyStats

    LaunchedEffect(userId) {
        viewModel.fetchWeeklyStats(userId)
    }

    Column(modifier = Modifier.padding(16.dp).background(MaterialTheme.colorScheme.background)) {
        //  Back Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController.popBackStack() }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBackIosNew,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground
            )


        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.weekly_statistics),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        weeklyStats.forEach { stat ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("🕐 ${stat.day}", color = MaterialTheme.colorScheme.onBackground)
                Text(stat.minutes, color = MaterialTheme.colorScheme.onBackground)

            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
