package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RobotMode
import com.example.ui.components.SafetyBanner
import com.example.ui.components.Servo4SoilControl
import com.example.ui.theme.CardBorderLight
import com.example.ui.theme.CardSurfaceWhite
import com.example.ui.theme.LocalRobotModeColors
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalWhite
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusSafe
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.viewmodel.RobotViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArmScreen(
    viewModel: RobotViewModel,
    modifier: Modifier = Modifier
) {
    val modeColors = LocalRobotModeColors.current
    val robotMode by viewModel.robotMode.collectAsState()
    val robotStatus by viewModel.robotStatus.collectAsState()
    val armStatus by viewModel.armStatus.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RoyalWhite)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("arm_screen")
    ) {
        // Critical Safety Banner
        SafetyBanner(robotStatus = robotStatus)

        // AUTOMATIC ARM HEADER & STATE
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("arm_state_card"),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderLight),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PrecisionManufacturing,
                            contentDescription = null,
                            tint = modeColors.primaryAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AUTOMATIC ARM",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = modeColors.headingColor
                        )
                    }

                    val stateName = robotStatus?.armState ?: armStatus.state.name
                    val stateColor = when (stateName) {
                        "HOME", "IDLE" -> StatusSafe
                        "DRILLING" -> StatusCritical
                        "MOVING", "TARGETING" -> modeColors.primaryAccent
                        "FAULT", "SAFE" -> StatusWarning
                        else -> TextMuted
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(stateColor.copy(alpha = 0.15f))
                            .border(1.dp, stateColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "STATE: $stateName",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = stateColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Drill Status Alert
                val drillActive = robotStatus?.drillOn == true || armStatus.drillOn
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (drillActive) StatusCritical.copy(alpha = 0.15f) else modeColors.containerColor.copy(alpha = 0.5f))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = if (drillActive) StatusCritical else modeColors.primaryAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (drillActive) "RELAY 1 AUTO DRILL: ACTIVE (${armStatus.drillTimeSeconds}s / 7s)" else "RELAY 1 AUTO DRILL: OFF",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (drillActive) StatusCritical else modeColors.headingColor
                        )
                    }

                    Text(
                        text = "Hardware Limit: 7s",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2D Arm Kinematics Visualizer
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("arm_kinematics_canvas_card"),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderLight),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "KINEMATICS & ARM TRAJECTORY",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = modeColors.headingColor
                )
                Text(
                    text = "Real-time servo joint angles and end-effector coordinates",
                    fontSize = 10.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Canvas drawing arm links
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF0F4F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val base = Offset(size.width / 2f, size.height * 0.85f)
                        val shoulderAngleRad = Math.toRadians(armStatus.servo2CurrentAngle.toDouble())
                        val wristAngleRad = Math.toRadians(armStatus.servo3CurrentAngle.toDouble())

                        val l1 = 80f
                        val l2 = 70f

                        val elbow = Offset(
                            x = base.x + (l1 * cos(shoulderAngleRad - Math.PI / 2)).toFloat(),
                            y = base.y - (l1 * sin(shoulderAngleRad - Math.PI / 2)).toFloat()
                        )

                        val toolTip = Offset(
                            x = elbow.x + (l2 * cos(shoulderAngleRad + wristAngleRad - Math.PI)).toFloat(),
                            y = elbow.y - (l2 * sin(shoulderAngleRad + wristAngleRad - Math.PI)).toFloat()
                        )

                        // Ground line
                        drawLine(
                            color = Color.Gray,
                            start = Offset(0f, base.y),
                            end = Offset(size.width, base.y),
                            strokeWidth = 2.dp.toPx()
                        )

                        // Base point
                        drawCircle(color = Color.DarkGray, radius = 10.dp.toPx(), center = base)

                        // Upper arm link (L1)
                        drawLine(
                            color = Color(0xFF137333),
                            start = base,
                            end = elbow,
                            strokeWidth = 8.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // Elbow joint
                        drawCircle(color = Color(0xFF0F5229), radius = 7.dp.toPx(), center = elbow)

                        // Forearm link (L2)
                        drawLine(
                            color = Color(0xFF1558D6),
                            start = elbow,
                            end = toolTip,
                            strokeWidth = 6.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // Drill / tool tip
                        val toolColor = if (armStatus.drillOn) Color.Red else Color(0xFF0C3C96)
                        drawCircle(color = toolColor, radius = 8.dp.toPx(), center = toolTip)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // AUTOMATIC SERVOS 1 - 3 (NO arbitrary manual sliders permitted) (Section 26)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderLight),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SERVO 1 - 3 ANGLES (AUTOMATIC ONLY)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = modeColors.headingColor
                )
                Text(
                    text = "Positioning controlled exclusively by Inverse Kinematics",
                    fontSize = 10.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(12.dp))

                ServoAngleRow(
                    name = "SERVO 1 (Base Rotation)",
                    current = "${armStatus.servo1CurrentAngle}°",
                    target = "${armStatus.servo1TargetAngle}°"
                )
                Spacer(modifier = Modifier.height(8.dp))
                ServoAngleRow(
                    name = "SERVO 2 (Elbow Reach)",
                    current = "${armStatus.servo2CurrentAngle}°",
                    target = "${armStatus.servo2TargetAngle}°"
                )
                Spacer(modifier = Modifier.height(8.dp))
                ServoAngleRow(
                    name = "SERVO 3 (Tool / Drill Wrist)",
                    current = "${armStatus.servo3CurrentAngle}°",
                    target = "${armStatus.servo3TargetAngle}°"
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // TARGET & CARTESIAN ARM COORDINATES (Section 26)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderLight),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SPATIAL COORDINATES",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = modeColors.headingColor
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("TARGET PIXELS:", fontSize = 11.sp, color = TextMuted)
                        val tx = armStatus.targetX?.toInt()?.toString() ?: "NONE"
                        val ty = armStatus.targetY?.toInt()?.toString() ?: "NONE"
                        Text("X: $tx • Y: $ty", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }

                    Column {
                        Text("ARM CARTESIAN (cm):", fontSize = 11.sp, color = TextMuted)
                        val ax = String.format(java.util.Locale.US, "%.1f", armStatus.armX)
                        val ay = String.format(java.util.Locale.US, "%.1f", armStatus.armY)
                        val az = String.format(java.util.Locale.US, "%.1f", armStatus.armZ)
                        Text("X: $ax • Y: $ay • Z: $az", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = modeColors.primaryAccent)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // MANUAL SERVO 4 (SOIL SERVO, 0 - 45) (Section 26)
        Servo4SoilControl(
            robotMode = robotMode,
            currentAngle = armStatus.servo4Angle,
            onAngleChange = { angle -> viewModel.setServo4Angle(angle) }
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun ServoAngleRow(name: String, current: String, target: String) {
    val modeColors = LocalRobotModeColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(modeColors.containerColor.copy(alpha = 0.35f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = modeColors.headingColor)
        Row {
            Text(text = "Current: ", fontSize = 11.sp, color = TextMuted)
            Text(text = current, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = "Target: ", fontSize = 11.sp, color = TextMuted)
            Text(text = target, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = modeColors.primaryAccent)
        }
    }
}
