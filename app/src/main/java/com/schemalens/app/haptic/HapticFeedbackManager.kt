package com.schemalens.app.haptic

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.schemalens.app.data.RiskSeverity

/**
 * Multi-Modal Haptic Feedback Manager for SchemaLens.
 *
 * Exploits the phone's haptic motor to provide tactile risk verification:
 * - SAFE: 1 gentle buzz (50ms)
 * - RISKY: 2 medium pulses (80ms each, 60ms gap)
 * - BREAKING: 3 harsh rapid pulses (120ms each, 40ms gap)
 *
 * Demonstrates that the app treats the mobile device as real hardware,
 * not just a "small computer monitor."
 */
object HapticFeedbackManager {

    @Suppress("DEPRECATION")
    private fun getVibrator(context: Context): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    /**
     * Plays a tactile risk feedback pattern based on severity.
     * - SAFE: 1 short gentle buzz (50ms)
     * - RISKY: 2 medium pulses (80ms each, 60ms gap)
     * - BREAKING: 3 harsh rapid pulses (120ms each, 40ms gap)
     * - PENDING: single soft tap (30ms)
     */
    fun playRiskFeedback(context: Context, severity: RiskSeverity) {
        val vibrator = getVibrator(context)
        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = when (severity) {
                RiskSeverity.SAFE -> VibrationEffect.createOneShot(50, 80)
                RiskSeverity.RISKY -> VibrationEffect.createWaveform(
                    longArrayOf(0, 80, 60, 80), // delay, on, off, on
                    intArrayOf(0, 140, 0, 140),  // amplitudes
                    -1 // no repeat
                )
                RiskSeverity.BREAKING -> VibrationEffect.createWaveform(
                    longArrayOf(0, 120, 40, 120, 40, 120), // delay, on, off, on, off, on
                    intArrayOf(0, 220, 0, 220, 0, 220),     // amplitudes — harsh
                    -1
                )
                RiskSeverity.PENDING -> VibrationEffect.createOneShot(30, 50)
            }
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            when (severity) {
                RiskSeverity.SAFE -> vibrator.vibrate(50)
                RiskSeverity.RISKY -> vibrator.vibrate(longArrayOf(0, 80, 60, 80), -1)
                RiskSeverity.BREAKING -> vibrator.vibrate(longArrayOf(0, 120, 40, 120, 40, 120), -1)
                RiskSeverity.PENDING -> vibrator.vibrate(30)
            }
        }
    }

    /**
     * Plays an assessment-complete haptic pattern scaled to the overall risk score.
     * - Low risk (0-32): single gentle confirmation tap
     * - Moderate (33-65): double tap pattern
     * - Critical (66-100): triple urgent buzz pattern
     */
    fun playAssessmentComplete(context: Context, score: Int) {
        val severity = when {
            score >= 66 -> RiskSeverity.BREAKING
            score >= 33 -> RiskSeverity.RISKY
            else -> RiskSeverity.SAFE
        }
        playRiskFeedback(context, severity)
    }

    /**
     * Plays a confirmation "click" haptic for UI interactions
     * (e.g., hot-patch sent, clipboard copied).
     */
    fun playConfirmationTap(context: Context) {
        val vibrator = getVibrator(context)
        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(35, 100))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(35)
        }
    }
}
