package com.example.service

import com.example.data.model.AppSettings
import com.example.data.model.ArmAngles
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

object KinematicsCalculator {

    // Physical arm link dimensions (cm)
    private const val L1_UPPER_ARM_CM = 14.0f
    private const val L2_FOREARM_CM = 13.0f

    /**
     * Converts camera pixel coordinates into ground coordinates (cm),
     * and calculates 3-axis inverse kinematics for Servo 1, 2, 3.
     */
    fun calculateArmSolution(
        pixelCenterX: Float,
        pixelCenterY: Float,
        imageWidth: Float,
        imageHeight: Float,
        settings: AppSettings
    ): ArmAngles {
        val safeWidth = if (imageWidth > 0f) imageWidth else 640f
        val safeHeight = if (imageHeight > 0f) imageHeight else 480f

        // Normalize coordinates relative to camera optical center (-1.0 to 1.0)
        val normX = (pixelCenterX - (safeWidth / 2f)) / (safeWidth / 2f)
        val normY = ((safeHeight / 2f) - pixelCenterY) / (safeHeight / 2f)

        // Ground scale calculation considering camera height
        val scale = settings.groundScaleFactor * settings.cameraHeightCm
        val groundX = normX * scale
        // Ground Y is distance ahead of the robot base
        val groundY = (18.0f + (normY * scale)).coerceAtLeast(10.0f)
        val groundZ = -1.5f // Slight drill depth below soil plane

        // 1. Servo 1: Base rotation angle
        // 90 degrees is straight ahead along the positive Y axis
        val baseAngleRad = atan2(groundX.toDouble(), groundY.toDouble())
        val baseAngleDeg = (90.0 + Math.toDegrees(baseAngleRad)).roundToInt().coerceIn(0, 180)

        // 2. Planar arm inverse kinematics in the vertical plane
        val planarDistance = hypot(groundX.toDouble(), groundY.toDouble()).toFloat()
        // Clamp distance within physical arm reach
        val maxReach = (L1_UPPER_ARM_CM + L2_FOREARM_CM) * 0.95f
        val clampedDistance = planarDistance.coerceIn(8.0f, maxReach)

        // 2-link IK:
        // D = (r^2 + z^2 - L1^2 - L2^2) / (2 * L1 * L2)
        val r = clampedDistance
        val z = groundZ
        val num = (r * r + z * z - L1_UPPER_ARM_CM * L1_UPPER_ARM_CM - L2_FOREARM_CM * L2_FOREARM_CM)
        val den = (2f * L1_UPPER_ARM_CM * L2_FOREARM_CM)
        val cosElbow = (num / den).toDouble().coerceIn(-1.0, 1.0)
        val elbowRad = kotlin.math.acos(cosElbow)

        // Shoulder angle:
        val shoulderAngleRad = atan2(z.toDouble(), r.toDouble()) +
                atan2((L2_FOREARM_CM * sin(elbowRad)), (L1_UPPER_ARM_CM + L2_FOREARM_CM * cos(elbowRad)))

        val shoulderDeg = Math.toDegrees(shoulderAngleRad).roundToInt().coerceIn(10, 160)
        val elbowDeg = Math.toDegrees(elbowRad).roundToInt().coerceIn(15, 165)

        // Servo 3: wrist/tool angle aligned downward for drilling
        val wristDeg = (180 - (shoulderDeg + elbowDeg)).coerceIn(10, 170)

        return ArmAngles(
            servo1BaseAngle = baseAngleDeg,
            servo2ShoulderAngle = shoulderDeg,
            servo3WristAngle = wristDeg,
            targetGroundX = groundX,
            targetGroundY = groundY,
            targetGroundZ = groundZ
        )
    }
}
