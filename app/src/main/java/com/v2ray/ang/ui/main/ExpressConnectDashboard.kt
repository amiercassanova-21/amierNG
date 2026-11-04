package com.v2ray.ang.ui.main

import android.net.TrafficStats
import android.os.Process
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.v2ray.ang.R
import com.v2ray.ang.dto.entities.ProfileItem
import com.v2ray.ang.extension.isComplexType
import com.v2ray.ang.extension.toSpeedString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * ExpressVPN-inspired hero connection dashboard for amierNG.
 */
@Composable
fun ExpressConnectDashboard(
    isRunning: Boolean,
    isConnecting: Boolean,
    statusOverride: String?,
    selectedGuid: String?,
    selectedProfile: ProfileItem?,
    selectedPing: Long,
    connectedSinceMillis: Long,
    onToggleService: () -> Unit,
    onTestPing: () -> Unit,
    onOpenLocations: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var remainingDataText by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(selectedGuid) {
        if (selectedProfile?.configType == com.v2ray.ang.enums.EConfigType.SATURN) {
            val usage = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                com.v2ray.ang.core.UrnetworkManager.fetchDataCap(context)
            }
            if (usage != null) {
                remainingDataText = usage.formattedRemaining
            }
        }
    }

    // Pulse animation for connected state
    val infiniteTransition = rememberInfiniteTransition(label = "ExpressPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val activeEmerald = Color(0xFF05A870)
    val activeEmeraldDark = Color(0xFF007E54)
    val inactiveRed = Color(0xFFD9383A)
    val connectingYellow = if (com.v2ray.ang.ui.compose.LocalDarkTheme.current) Color(0xFFFFD54F) else Color(0xFF8A6500)
    val statusColor = when { isConnecting -> connectingYellow; isRunning -> activeEmerald; else -> inactiveRed }
    val connectionLabel = stringResource(when {
        isConnecting -> R.string.connection_connecting
        isRunning -> R.string.connection_status_connected
        else -> R.string.connection_not_connected
    })
    val toggleLabel = stringResource(when {
        isConnecting -> R.string.connection_cancel
        isRunning -> R.string.connection_stop
        else -> R.string.connection_start
    })
    val darkSurfaceBorder = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Status Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = statusColor.copy(alpha = 0.15f),
                border = BorderStroke(
                    1.dp,
                    statusColor.copy(alpha = 0.4f)
                ),
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(when {
                            isConnecting -> R.string.connection_connecting
                            isRunning -> R.string.dashboard_vpn_on
                            else -> R.string.dashboard_vpn_off
                        }),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = statusColor
                    )
                }
            }

            Text(
                text = connectionLabel,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                ),
                color = if (isRunning || isConnecting) statusColor else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                // Test progress and results replace the idle hint so they stay visible here.
                text = statusOverride ?: stringResource(when {
                    isConnecting -> R.string.connection_connecting_hint
                    isRunning -> R.string.connection_connected_hint
                    else -> R.string.connection_start_hint
                }),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (isRunning) {
                ConnectionStatsRow(connectedSinceMillis = connectedSinceMillis, color = statusColor)
            }
        }

        if (selectedProfile?.configType == com.v2ray.ang.enums.EConfigType.SATURN) {
            Spacer(modifier = Modifier.height(16.dp))
            val usageText = remainingDataText
                ?: com.v2ray.ang.core.UrnetworkManager.cachedUsage?.formattedRemaining
                ?: stringResource(R.string.saturn_ur_remaining_data)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                shadowElevation = 2.dp,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_shield_24dp),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = usageText,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            Spacer(modifier = Modifier.height(28.dp))
        }

        // ExpressVPN Giant Power Button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(190.dp)
        ) {
            // Pulsing Aura Ring (when connected)
            if (isRunning || isConnecting) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = pulseAlpha))
                )
            }

            // Outer Frame Ring
            Box(
                modifier = Modifier
                    .size(166.dp)
                    .clip(CircleShape)
                    .border(
                        width = 4.dp,
                        color = if (isRunning || isConnecting) statusColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        shape = CircleShape
                    )
            )

            // Inner Button Circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(136.dp)
                    .shadow(
                        elevation = if (isRunning || isConnecting) 14.dp else 4.dp,
                        shape = CircleShape,
                        spotColor = if (isRunning || isConnecting) statusColor else Color.Black
                    )
                    .clip(CircleShape)
                    .background(
                        brush = if (isConnecting) {
                            Brush.verticalGradient(listOf(Color(0xFFFFE082), Color(0xFFFBC02D)))
                        } else if (isRunning) {
                            Brush.verticalGradient(listOf(Color(0xFF07C182), activeEmeraldDark))
                        } else {
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surfaceContainerHighest,
                                    MaterialTheme.colorScheme.surfaceContainer
                                )
                            )
                        }
                    )
                    .clickable {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onToggleService()
                    }
                    .semantics { contentDescription = toggleLabel; stateDescription = connectionLabel }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_power_24dp),
                    contentDescription = null,
                    tint = if (isConnecting) Color(0xFF332800) else if (isRunning) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    modifier = Modifier.size(56.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // ExpressVPN Selected Location / Server Card
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            border = BorderStroke(
                1.dp,
                if (isRunning) activeEmerald.copy(alpha = 0.45f) else darkSurfaceBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(22.dp))
                .clickable(onClick = onOpenLocations)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Globe / Location Icon
                Surface(
                    shape = CircleShape,
                    color = if (isRunning) activeEmerald.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceContainerHighest,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.ic_globe_24dp),
                            contentDescription = null,
                            tint = if (isRunning) activeEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Location / Server Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.dashboard_selected_configuration),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    val serverTitle = selectedProfile?.remarks?.ifBlank { stringResource(R.string.dashboard_default_server) }
                        ?: if (selectedGuid.isNullOrEmpty()) stringResource(R.string.dashboard_no_server_selected) else stringResource(R.string.dashboard_config_short, selectedGuid.take(8))

                    Text(
                        text = serverTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Protocol badge
                        val proto = when {
                            selectedProfile == null -> "v2ray"
                            selectedProfile.configType == com.v2ray.ang.enums.EConfigType.SATURN -> {
                                val carrier = selectedProfile.saturnSettings?.normalizedCarrier()
                                    ?: selectedProfile.network?.ifBlank { null }
                                    ?: "h2"
                                "${selectedProfile.configType.name} / $carrier"
                            }
                            selectedProfile.configType == com.v2ray.ang.enums.EConfigType.AETHER -> {
                                val carrier = selectedProfile.aetherSettings?.carrier?.ifBlank { null } ?: "h2"
                                "${selectedProfile.configType.name} / $carrier"
                            }
                            selectedProfile.configType.isComplexType() -> selectedProfile.configType.name
                            else -> selectedProfile.configType.name
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = proto,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // Ping badge
                        val pingText = when {
                            selectedPing > 0L -> "$selectedPing ms"
                            selectedPing < 0L -> stringResource(R.string.dashboard_ping_timeout)
                            else -> stringResource(R.string.dashboard_ping_untested)
                        }
                        val pingColor = when {
                            selectedPing > 0L -> activeEmerald
                            selectedPing < 0L -> inactiveRed
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Text(
                            text = "● $pingText",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = pingColor
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Ping action button
                IconButton(
                    onClick = onTestPing,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_flash_on_24dp),
                        contentDescription = stringResource(R.string.dashboard_test_ping),
                        tint = activeEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Arrow pointing right
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back_24dp),
                    contentDescription = stringResource(R.string.dashboard_change_location),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(180f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Connection duration and current download/upload speed of this app's UID.
 * In VPN and proxy modes all proxied traffic flows through the core inside this UID.
 */
@Composable
private fun ConnectionStatsRow(connectedSinceMillis: Long, color: Color) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var downSpeed by remember { mutableLongStateOf(-1L) }
    var upSpeed by remember { mutableLongStateOf(-1L) }
    LaunchedEffect(Unit) {
        val uid = Process.myUid()
        // TrafficStats may use binder calls; read it off the main thread.
        suspend fun read() = withContext(Dispatchers.IO) {
            TrafficStats.getUidRxBytes(uid) to TrafficStats.getUidTxBytes(uid)
        }
        var last = read()
        while (true) {
            delay(1000L)
            val current = read()
            now = System.currentTimeMillis()
            val supported = current.first != TrafficStats.UNSUPPORTED.toLong() && last.first != TrafficStats.UNSUPPORTED.toLong()
            downSpeed = if (supported) (current.first - last.first).coerceAtLeast(0L) else -1L
            upSpeed = if (supported) (current.second - last.second).coerceAtLeast(0L) else -1L
            last = current
        }
    }

    val parts = buildList {
        if (connectedSinceMillis in 1..now) {
            val seconds = (now - connectedSinceMillis) / 1000L
            add("⏱ %02d:%02d:%02d".format(seconds / 3600L, seconds / 60L % 60L, seconds % 60L))
        }
        if (downSpeed >= 0L && upSpeed >= 0L) {
            add("↓ ${downSpeed.toSpeedString()}   ↑ ${upSpeed.toSpeedString()}")
        }
    }
    if (parts.isEmpty()) return

    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = parts.joinToString("     "),
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        color = color
    )
}
