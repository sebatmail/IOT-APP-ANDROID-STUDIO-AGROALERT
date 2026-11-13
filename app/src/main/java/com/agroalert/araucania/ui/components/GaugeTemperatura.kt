package com.agroalert.araucania.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GaugeTemperatura(
    temperatura: Float,
    umbralHelada: Float,
    modifier: Modifier = Modifier
) {
    val tempAnimada by animateFloatAsState(
        targetValue = temperatura,
        animationSpec = tween(durationMillis = 500),
        label = "tempAnimada"
    )

    val esHelada = temperatura <= umbralHelada

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(240.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f + 16.dp.toPx())
            val radius = (size.width / 2f) - 18.dp.toPx()

            val startAngle = 150f
            val totalSweep = 240f

            // Arco exterior graduado en gradiente amarillo a rojo
            drawArc(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFFFB300), // Amarillo
                        Color(0xFFFF9800), // Naranja
                        Color(0xFFE53935)  // Rojo Helada
                    )
                ),
                startAngle = startAngle,
                sweepAngle = totalSweep,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            )

            // Graduaciones / marcas de escala (ticks)
            val tickCount = 24
            for (i in 0..tickCount) {
                val angleDeg = startAngle + (i.toFloat() / tickCount) * totalSweep
                val angleRad = Math.toRadians(angleDeg.toDouble())
                val isMajor = (i % 4 == 0)
                val innerR = radius - (if (isMajor) 16.dp.toPx() else 9.dp.toPx())
                val outerR = radius - 3.dp.toPx()

                val startX = center.x + (innerR * cos(angleRad)).toFloat()
                val startY = center.y + (innerR * sin(angleRad)).toFloat()
                val endX = center.x + (outerR * cos(angleRad)).toFloat()
                val endY = center.y + (outerR * sin(angleRad)).toFloat()

                drawLine(
                    color = if (isMajor) Color(0xFF1F2937) else Color(0xFF9CA3AF),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = if (isMajor) 2.2.dp.toPx() else 1.1.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        // Contenido central: Termómetro, temperatura en grande y badge de alerta
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Thermostat,
                contentDescription = null,
                tint = if (esHelada) Color(0xFFD32F2F) else Color(0xFF2E7D32),
                modifier = Modifier.size(26.dp)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${String.format("%.1f", tempAnimada)} °C",
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Badge exacto al mockup
            Box(
                modifier = Modifier
                    .background(
                        color = if (esHelada) Color(0xFFFFD54F) else Color(0xFFC8E6C9),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        width = 1.5.dp,
                        color = if (esHelada) Color(0xFFF57F17) else Color(0xFF2E7D32),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (esHelada) "ALERTA HELADA" else "ESTADO SEGURO",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (esHelada) Color(0xFF991B1B) else Color(0xFF1B5E20),
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
