package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DetectionResult
import com.example.data.model.TargetClass
import com.example.ui.theme.PureWhite
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusSafe
import com.example.ui.theme.StatusUnknown
import com.example.ui.theme.StatusWarning

@Composable
fun AiDetectionOverlay(
    detections: List<DetectionResult>,
    frameWidth: Float = 640f,
    frameHeight: Float = 480f,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val scaleX = size.width / frameWidth.coerceAtLeast(1f)
            val scaleY = size.height / frameHeight.coerceAtLeast(1f)

            for (detection in detections) {
                val boxColor = when (detection.targetClass) {
                    TargetClass.WEED -> StatusCritical // Red/Orange for weed to eliminate
                    TargetClass.ONION -> StatusSafe    // Emerald Green for protected onion
                    TargetClass.UNKNOWN -> StatusUnknown
                }

                val left = detection.x * scaleX
                val top = detection.y * scaleY
                val boxWidth = detection.width * scaleX
                val boxHeight = detection.height * scaleY
                val cx = detection.centerX * scaleX
                val cy = detection.centerY * scaleY

                // Draw bounding box
                drawRect(
                    color = boxColor,
                    topLeft = Offset(left, top),
                    size = Size(boxWidth, boxHeight),
                    style = Stroke(width = 3.dp.toPx())
                )

                // Draw center crosshairs
                val crossSize = 10.dp.toPx()
                drawLine(
                    color = boxColor,
                    start = Offset(cx - crossSize, cy),
                    end = Offset(cx + crossSize, cy),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    color = boxColor,
                    start = Offset(cx, cy - crossSize),
                    end = Offset(cx, cy + crossSize),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }

        // Target Info Badges
        val firstDetection = detections.firstOrNull()
        if (firstDetection != null) {
            val badgeColor = when (firstDetection.targetClass) {
                TargetClass.WEED -> StatusCritical
                TargetClass.ONION -> StatusSafe
                TargetClass.UNKNOWN -> StatusUnknown
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${firstDetection.label} (${(firstDetection.confidence * 100).toInt()}%)\n" +
                            "X:${firstDetection.x.toInt()} Y:${firstDetection.y.toInt()} W:${firstDetection.width.toInt()} H:${firstDetection.height.toInt()}\n" +
                            "Center: (${firstDetection.centerX.toInt()}, ${firstDetection.centerY.toInt()})\n" +
                            firstDetection.actionDescription,
                    color = badgeColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
