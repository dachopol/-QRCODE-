package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppCardShape
import com.example.ui.theme.AppSectionShape
import com.example.ui.theme.AppPillShape
import com.example.ui.theme.GlassAccent
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBlurRadius
import com.example.util.localizedText

// Preset colors for QR code foreground (dots/pattern)
val QrDarkColorPresets = listOf(
    Pair("ดำคลาสสิก", Color(0xFF000000)),
    Pair("น้ำเงินเข้ม", Color(0xFF0B2853)),
    Pair("ฟ้าสดใส", Color(0xFF0284C7)),
    Pair("เขียวมรกต", Color(0xFF059669)),
    Pair("ม่วงเข้ม", Color(0xFF6D28D9)),
    Pair("แดงไวน์", Color(0xFF991B1B)),
    Pair("ส้มอิฐ", Color(0xFFC2410C)),
    Pair("ช็อกโกแลต", Color(0xFF451A03))
)

// Preset colors for QR code background (จุดเปลี่ยนสีพื้นหลัง)
val QrLightColorPresets = listOf(
    Pair("ขาวบริสุทธิ์", Color(0xFFFFFFFF)),
    Pair("ครีมงาช้าง", Color(0xFFFEF9C3)),
    Pair("ฟ้าพาสเทล", Color(0xFFE0F2FE)),
    Pair("เขียวมิ้นต์", Color(0xFFD1FAE5)),
    Pair("ชมพูซากุระ", Color(0xFFFCE7F3)),
    Pair("ส้มพีช", Color(0xFFFFEDD5)),
    Pair("ม่วงลาเวนเดอร์", Color(0xFFEDE9FE)),
    Pair("เหลืองอ่อน", Color(0xFFFEF08A)),
    Pair("เทาเงินโมเดิร์น", Color(0xFFE2E8F0)),
    Pair("ดำมินิมอล", Color(0xFF18181B))
)

@Composable
private fun qrColorName(th: String): String = localizedText(
    th,
    when (th) {
        "ดำคลาสสิก" -> "Classic Black"
        "น้ำเงินเข้ม" -> "Deep Navy"
        "ฟ้าสดใส" -> "Bright Blue"
        "เขียวมรกต" -> "Emerald"
        "ม่วงเข้ม" -> "Deep Purple"
        "แดงไวน์" -> "Wine Red"
        "ส้มอิฐ" -> "Brick Orange"
        "ช็อกโกแลต" -> "Chocolate"
        "ขาวบริสุทธิ์" -> "Pure White"
        "ครีมงาช้าง" -> "Ivory"
        "ฟ้าพาสเทล" -> "Pastel Blue"
        "เขียวมิ้นต์" -> "Mint"
        "ชมพูซากุระ" -> "Sakura Pink"
        "ส้มพีช" -> "Peach"
        "ม่วงลาเวนเดอร์" -> "Lavender"
        "เหลืองอ่อน" -> "Soft Yellow"
        "เทาเงินโมเดิร์น" -> "Silver Gray"
        "ดำมินิมอล" -> "Minimal Black"
        else -> th
    }
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QrColorCustomizerCard(
    darkColor: Color,
    lightColor: Color,
    onDarkColorChange: (Color) -> Unit,
    onLightColorChange: (Color) -> Unit,
    includeCenterLogo: Boolean = true,
    onIncludeCenterLogoChange: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isBgDark = lightColor == Color(0xFF18181B)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("qr_color_customizer_card"),
        shape = AppCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header with title and reset button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .blur(GlassBlurRadius)
                                .background(GlassAccent, CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(GlassAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = localizedText("ปรับแต่งสี", "Customize colors"),
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = localizedText("ปรับแต่ง QR", "Customize QR"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Reset Button
                IconButton(
                    onClick = {
                        onDarkColorChange(Color.Black)
                        onLightColorChange(Color.White)
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("reset_qr_colors_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = localizedText("รีเซ็ตสีเริ่มต้น", "Reset colors"),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (onIncludeCenterLogoChange != null) {
                Surface(
                    shape = AppSectionShape,
                    color = GlassAccent,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = localizedText("ตรากลาง QR", "Center logo"),
                            modifier = Modifier.weight(1f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = includeCenterLogo,
                            onCheckedChange = { onIncludeCenterLogoChange(it) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // LIVE PREVIEW BADGE
            Surface(
                shape = AppSectionShape,
                color = lightColor,
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (isBgDark) Color(0xFF3F3F46) else Color(0xFFCBD5E1)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(darkColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                tint = lightColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = localizedText("ตัวอย่าง QR", "QR preview"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isBgDark) Color.White else Color(0xFF0F172A)
                        )
                    }

                    Surface(
                        shape = AppPillShape,
                        color = (if (isBgDark) Color.White else Color(0xFF0F172A)).copy(alpha = 0.08f)
                    ) {
                        Text(
                            text = localizedText("พร้อมใช้งาน", "Ready"),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isBgDark) Color.White else Color(0xFF0F172A),
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // SECTION 1: Background (Light) Color Selection - "จุดเปลี่ยนสีพื้นหลัง"
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FormatColorFill,
                    contentDescription = null,
                    tint = Color(0xFF0284C7),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = localizedText("สีพื้นหลัง QR", "QR background color"),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                QrLightColorPresets.forEach { (name, color) ->
                    val displayName = qrColorName(name)
                    val isSelected = lightColor == color
                    val isDotWhite = color == Color.White || color == Color(0xFFFEF9C3)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable {
                                onLightColorChange(color)
                                // If user picked dark background and current darkColor is black, automatically switch darkColor to white for contrast
                                if (color == Color(0xFF18181B) && darkColor == Color.Black) {
                                    onDarkColorChange(Color.White)
                                } else if (color != Color(0xFF18181B) && darkColor == Color.White) {
                                    onDarkColorChange(Color.Black)
                                }
                            }
                            .testTag("color_light_${color.toArgb()}")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .shadow(2.dp, CircleShape)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.5.dp,
                                    color = if (isSelected) Color(0xFF0284C7) else (if (isDotWhite) Color(0xFFCBD5E1) else color.copy(alpha = 0.5f)),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = displayName,
                                    tint = if (color == Color(0xFF18181B)) Color.White else Color(0xFF0B2853),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = displayName,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFF0284C7) else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // SECTION 2: Foreground (Dark) Color Selection - "สีลวดลาย QR"
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = null,
                    tint = Color(0xFF0B2853),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = localizedText("สีลวดลาย QR", "QR pattern color"),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                QrDarkColorPresets.forEach { (name, color) ->
                    val displayName = qrColorName(name)
                    val isSelected = darkColor == color
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onDarkColorChange(color) }
                            .testTag("color_dark_${color.toArgb()}")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .shadow(2.dp, CircleShape)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF0284C7) else Color(0xFFCBD5E1),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = displayName,
                                    tint = if (color == Color(0xFF000000) || color == Color(0xFF0B2853) || color == Color(0xFF451A03) || color == Color(0xFF991B1B) || color == Color(0xFF6D28D9)) Color.White else Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = displayName,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFF0284C7) else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}
