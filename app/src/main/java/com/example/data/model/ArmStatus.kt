package com.example.data.model

enum class ArmState {
    IDLE,
    SEARCHING,
    MOVING,
    TARGETING,
    DRILLING,
    RETURNING,
    HOME,
    SAFE,
    FAULT
}

data class ArmStatus(
    val state: ArmState = ArmState.HOME,
    val servo1CurrentAngle: Int = 90,
    val servo1TargetAngle: Int = 90,
    val servo2CurrentAngle: Int = 45,
    val servo2TargetAngle: Int = 45,
    val servo3CurrentAngle: Int = 90,
    val servo3TargetAngle: Int = 90,
    val servo4Angle: Int = 0, // Enforced 0..45 degrees
    val targetX: Float? = null,
    val targetY: Float? = null,
    val armX: Float = 0.0f,
    val armY: Float = 15.0f,
    val armZ: Float = 5.0f,
    val drillOn: Boolean = false,
    val drillTimeSeconds: Int = 0,
    val isCameraLocked: Boolean = false
) {
    val isBusy: Boolean
        get() = state == ArmState.MOVING || state == ArmState.TARGETING ||
                state == ArmState.DRILLING || state == ArmState.RETURNING

    val isHome: Boolean
        get() = state == ArmState.HOME

    val isSafe: Boolean
        get() = state == ArmState.SAFE
}
