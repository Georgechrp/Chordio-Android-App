package com.unipi.george.chordshub.screens.slidemenu.options

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.unipi.george.chordshub.R
import com.unipi.george.chordshub.viewmodels.user.SettingsViewModel
import com.unipi.george.chordshub.components.SettingsHeads
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

@Composable
fun SettingsScreen(navController: NavController, settingsViewModel: SettingsViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = { SettingsTopBar(navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            DarkModeToggle(settingsViewModel)
           // LanguageSelection(settingsViewModel)
            FontSizeSelection(settingsViewModel)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopBar(navController: NavController) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.settings_text),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Πίσω")
            }
        }
    )

}

@Composable
fun DarkModeToggle(settingsViewModel: SettingsViewModel) {
    var showDialog by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        SettingsHeads(
            text = stringResource(R.string.dark_mode_text),
            settingsViewModel = settingsViewModel
        )
        Spacer(modifier = Modifier.weight(1f))
        Switch(
            checked = settingsViewModel.darkMode.value,
            onCheckedChange = {
                settingsViewModel.toggleDarkMode()
                showDialog = true
            }
        )
    }
    Divider(modifier = Modifier.padding(vertical = 4.dp))

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("ΟΚ")
                }
            },
            title = { Text(stringResource(id = R.string.dialog_title_theme_change)) },
            text = { Text(stringResource(id = R.string.dialog_text_restart_required)) }

        )
    }
}



@Composable
fun LanguageSelection(settingsViewModel: SettingsViewModel) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    SettingsHeads("Γλώσσα", settingsViewModel)

    Row {
        Button(
            onClick = {
                settingsViewModel.changeLanguage("el")
                updateLocale(context, "el")
                showDialog = true
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )

        ) {
            Text("Ελληνικά", color = Color.White)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = {
                settingsViewModel.changeLanguage("en")
                updateLocale(context, "en")
                showDialog = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text("English", color = Color.White)
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("ΟΚ", color = MaterialTheme.colorScheme.primary)
                }
            },
            title = {
                Text(
                    text = "Αλλαγή Γλώσσας",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Η αλλαγή γλώσσας θα εφαρμοστεί μετά από επανεκκίνηση της εφαρμογής.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )

    }

    Divider(modifier = Modifier.padding(vertical = 4.dp))
}

@Composable
fun FontSizeSelection(settingsViewModel: SettingsViewModel) {
    var tempFontSize by remember { mutableStateOf(settingsViewModel.fontSize.value) }
    val sampleText = stringResource(R.string.font_sample_text)

    SettingsHeads(
        text = stringResource(R.string.font_size_dynamic, tempFontSize.toInt()),
        settingsViewModel = settingsViewModel
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Slider(
            value = tempFontSize,
            onValueChange = {
                tempFontSize = it
            },
            valueRange = 12f..24f,
            modifier = Modifier.weight(1f)
        )

    }
    Text(
        text = sampleText,
        style = TextStyle(fontSize = tempFontSize.sp),
        color = MaterialTheme.colorScheme.onBackground
    )
    IconButton(onClick = { settingsViewModel.changeFontSize(tempFontSize) }) {
        Icon(Icons.Filled.Save, contentDescription = "Αποθήκευση Μεγέθους Γραμματοσειράς")
    }
}

fun updateLocale(context: Context, language: String) {
    val locale = Locale(language)
    Locale.setDefault(locale)

    val config = context.resources.configuration
    config.setLocale(locale)

    context.resources.updateConfiguration(config, context.resources.displayMetrics)
}
