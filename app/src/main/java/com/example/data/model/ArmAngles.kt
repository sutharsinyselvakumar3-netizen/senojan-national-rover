package com.example.data.model

data class ArmAngles(
    val servo1BaseAngle: Int,
    val servo2ShoulderAngle: Int,
    val servo3WristAngle: Int,
    val targetGroundX: Float,
    val targetGroundY: Float,
    val targetGroundZ: Float
)
