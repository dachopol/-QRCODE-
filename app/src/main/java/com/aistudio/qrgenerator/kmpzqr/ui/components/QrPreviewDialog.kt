package com.aistudio.qrgenerator.kmpzqr.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aistudio.qrgenerator.kmpzqr.ActiveQrPreview
import com.aistudio.qrgenerator.kmpzqr.util.ImageExporter
import com.aistudio.qrgenerator.kmpzqr.util.LocationQrUtil
import com.aistudio.qrgenerator.kmpzqr.ui.theme.AppCardShape
import com.aistudio.qrgenerator.kmpzqr.ui.theme.AppPillShape
import com.aistudio.qrgenerator.kmpzqr.ui.theme.AppSectionShape
import com.aistudio.qrgenerator.kmpzqr.ui.theme.GlassBorder
import com.aistudio.qrgenerator.kmpzqr.util.localizedText
import com.aistudio.qrgenerator.kmpzqr.util.localizedNow

@Composable
fun QrPreviewDialog(
    preview: ActiveQrPreview,
    onDismiss: () -> Unit,
    onSaveRequested: (Bitmap, String) -> Unit
) {
    val context = LocalContext.current
    var showStandee by remember { mutableStateOf(preview.standeeBitmap != null) }

    val activeBitmap = if (showStandee && preview.standeeBitmap != null) {
        preview.standeeBitmap
    } else {
        preview.qrBitmap
    }
    val mapPoint = remember(preview.type, preview.rawContent) {
        if (preview.type == "LOCATION") LocationQrUtil.parseGeoOrNull(preview.rawContent) else null
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("qr_preview_dialog"),
            shape = AppCardShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = preview.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = preview.subtitle,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = localizedText("ปิด", "Close"), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mode toggle for PromptPay
                if (preview.standeeBitmap != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        FilterChip(
                            selected = showStandee,
                            onClick = { showStandee = true },
                            label = { Text(localizedText("ป้ายพร้อมเพย์", "PromptPay standee"), fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0B2853),
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        FilterChip(
                            selected = !showStandee,
                            onClick = { showStandee = false },
                            label = { Text(localizedText("คิวอาร์เดี่ยว", "QR code only"), fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0B2853),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // QR Display Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(AppSectionShape)
                        .background(MaterialTheme.colorScheme.background)
                        .border(1.dp, GlassBorder, AppSectionShape)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = activeBitmap.asImageBitmap(),
                        contentDescription = localizedText("คิวอาร์โค้ด", "QR code"),
                        modifier = Modifier
                            .fillMaxWidth(if (showStandee && preview.standeeBitmap != null) 0.95f else 0.8f)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // High resolution quality notice badge
                Surface(
                    shape = AppSectionShape,
                    color = Color(0xFFECFDF5),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = localizedText("บันทึกภาพความคมชัดสูงสำหรับพิมพ์หรือแชร์", "Save a high-resolution image for printing or sharing"),
                            fontSize = 11.sp,
                            color = Color(0xFF065F46)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (mapPoint != null) {
                    OutlinedButton(
                        onClick = { LocationQrUtil.openMap(context, mapPoint) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 44.dp)
                            .testTag("open_generated_location_map_button"),
                        shape = AppPillShape
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            localizedText("เปิดพิกัดในแผนที่", "Open location in map"),
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            onSaveRequested(activeBitmap, preview.title)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("save_qr_button"),
                        shape = AppPillShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(localizedText("บันทึกรูปภาพ", "Save image"), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val shareMsg = "${preview.title}\n${preview.subtitle}"
                            ImageExporter.shareBitmap(context, activeBitmap, shareMsg)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("share_qr_button"),
                        shape = AppPillShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B2853))
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(localizedText("แชร์", "Share"), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("QR Code", preview.rawContent))
                        Toast.makeText(context, localizedNow("คัดลอกข้อมูลแล้ว", "QR text copied"), Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 44.dp)
                        .testTag("copy_payload_button"),
                    shape = AppPillShape
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(localizedText("คัดลอกข้อความในคิวอาร์", "Copy QR text"), fontSize = 13.sp)
                }
            }
        }
    }
}
