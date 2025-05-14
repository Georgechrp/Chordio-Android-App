    package com.unipi.george.chordshub.utils

    import android.graphics.Bitmap
    import androidx.compose.ui.graphics.Color
    import android.graphics.Color as AndroidColor
    import androidx.activity.compose.rememberLauncherForActivityResult
    import androidx.compose.foundation.Image
    import androidx.compose.foundation.clickable
    import androidx.compose.foundation.layout.*
    import androidx.compose.material3.AlertDialog
    import androidx.compose.material3.Button
    import androidx.compose.material3.Icon
    import androidx.compose.material3.IconButton
    import androidx.compose.material3.Text
    import androidx.compose.runtime.*
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.ColorFilter
    import androidx.compose.ui.graphics.asImageBitmap
    import androidx.compose.ui.res.painterResource
    import androidx.compose.ui.res.stringResource
    import androidx.compose.ui.text.TextStyle
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import com.google.zxing.BarcodeFormat
    import com.google.zxing.qrcode.QRCodeWriter
    import com.journeyapps.barcodescanner.ScanContract
    import com.journeyapps.barcodescanner.ScanOptions
    import com.unipi.george.chordshub.R
    import com.unipi.george.chordshub.viewmodels.main.SearchViewModel


    fun generateQRCode(content: String): Bitmap? {
        return try {
            val qrCodeWriter = QRCodeWriter()
            val bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 512, 512)
            val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)
            for (x in 0 until 512) {
                for (y in 0 until 512) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }


    @Composable
    fun QRCodeDialog(showDialog: MutableState<Boolean>, songId: String) {
        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = { showDialog.value = false },
                confirmButton = {
                    // Κουμπί κλεισίματος
                    Button(onClick = { showDialog.value = false }) {
                        Text("Κλείσιμο")
                    }
                },
                title = {
                    // Τίτλος του Dialog
                    Text(
                        text = stringResource(R.string.generate_qr_code_text),
                        style = TextStyle(fontSize = 18.sp),
                        modifier = Modifier.padding(bottom = 8.dp) // Προσθέτουμε padding στον τίτλο
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp) // Padding γύρω από το κείμενο και την εικόνα
                    ) {
                        // Δημιουργία και εμφάνιση του QR Code
                        val qrBitmap = generateQRCode(songId)
                        qrBitmap?.let {
                            Image(bitmap = it.asImageBitmap(), contentDescription = "QR Code", modifier = Modifier.size(200.dp))
                        } ?: Text("Σφάλμα δημιουργίας QR")
                    }
                }
            )
        }
    }



    @Composable
    fun QRCodeButton(showQRCodeDialog: MutableState<Boolean>) {
        IconButton(onClick = { showQRCodeDialog.value = true }) {
            Icon(
                painter = painterResource(id = R.drawable.generateqrcode),
                contentDescription = "Share via QR"
            )
        }
    }

    @Composable
    fun QRCodeScannerButton(viewModel: SearchViewModel) {
        val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
            result.contents?.let { scannedId ->
                viewModel.selectSong(scannedId)
            }
        }

        Box(
            modifier = Modifier
                .size(30.dp)
                .clickable {
                    val options = ScanOptions()
                    options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                    options.setPrompt("Scan a QR Code")
                    options.setCameraId(0)
                    options.setOrientationLocked(true)
                    options.setBeepEnabled(true)
                    options.setBarcodeImageEnabled(true)
                    scanLauncher.launch(options)
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.scanner),
                contentDescription = "Σκανάρισμα QR",
                modifier = Modifier.fillMaxSize(),
                colorFilter = ColorFilter.tint(color = Color.Black)
            )

        }
    }
