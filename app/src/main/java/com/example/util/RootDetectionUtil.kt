package com.example.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * Result of comprehensive device root and integrity inspection.
 */
data class RootCheckResult(
    val isRooted: Boolean = false,
    val reasons: List<String> = emptyList(),
    val unsupportedChecks: List<String> = emptyList(),
    val testKeysFound: Boolean = false,
    val suBinaryFound: Boolean = false,
    val rootAppFound: Boolean = false,
    val dangerousPropsFound: Boolean = false,
    val suExecutionSucceeded: Boolean = false,
    val rwMountsFound: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

object RootDetectionUtil {
    private const val TAG = "RootDetectionUtil"

    private val KNOWN_SU_PATHS = listOf(
        "/system/app/Superuser.apk",
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su",
        "/su/bin/su",
        "/system/su",
        "/system/bin/.ext/.su",
        "/system/usr/we-need-root/su-backup",
        "/system/xbin/mu"
    )

    private val KNOWN_ROOT_PACKAGES = listOf(
        "com.noshufou.android.su",
        "com.noshufou.android.su.elite",
        "eu.chainfire.supersu",
        "com.koushikdutta.superuser",
        "com.thirdparty.superuser",
        "com.yellowes.su",
        "com.topjohnwu.magisk",
        "de.robv.android.xposed.installer",
        "org.meowcat.edxposed.manager",
        "com.kingroot.kinguser",
        "com.kingo.root",
        "com.chelpus.lackypatch",
        "com.ramdroid.appquarantine",
        "com.device.root"
    )

    /**
     * Check if build tags indicate a test or unofficial build (e.g. test-keys).
     */
    fun checkBuildTags(): Boolean {
        val buildTags = Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }

    /**
     * Check if su or root binaries exist in standard system binary paths.
     */
    fun checkSuBinaryPaths(): Boolean {
        for (path in KNOWN_SU_PATHS) {
            try {
                val file = File(path)
                if (file.exists()) {
                    Log.w(TAG, "Root binary detected at: $path")
                    return true
                }
            } catch (_: Exception) {
                // Ignore security exceptions
            }
        }
        return false
    }

    /**
     * Check if common root management apps or superuser APKs are installed.
     */
    fun checkRootApps(context: Context): Boolean {
        val pm = context.packageManager
        for (pkg in KNOWN_ROOT_PACKAGES) {
            try {
                pm.getPackageInfo(pkg, PackageManager.GET_ACTIVITIES)
                Log.w(TAG, "Root management app detected: $pkg")
                return true
            } catch (_: PackageManager.NameNotFoundException) {
                // Not found, continue
            } catch (_: Exception) {
                // Ignore
            }
        }
        return false
    }

    /**
     * Attempt to execute su binary via Runtime process.
     */
    fun checkSuExecution(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val line = reader.readLine()
            line != null
        } catch (_: Throwable) {
            false
        } finally {
            process?.destroy()
        }
    }

    /**
     * Check if /system or /vendor mounts have read-write (rw) flags.
     * Returns Pair(detectedRw, isSupported)
     */
    fun checkDangerousMounts(): Pair<Boolean, Boolean> {
        return try {
            val file = File("/proc/mounts")
            if (file.canRead()) {
                val hasRw = file.useLines { lines ->
                    lines.any { line ->
                        val parts = line.split(" ")
                        if (parts.size >= 4) {
                            val mountPoint = parts[1]
                            val mountOptions = parts[3].split(",")
                            (mountPoint == "/system" || mountPoint == "/vendor") && mountOptions.contains("rw")
                        } else false
                    }
                }
                Pair(hasRw, true)
            } else {
                Pair(false, false) // Not readable on this Android OS version
            }
        } catch (_: Exception) {
            Pair(false, false)
        }
    }

    /**
     * Comprehensive scan running all checks.
     */
    fun performFullCheck(context: Context): RootCheckResult {
        val reasons = mutableListOf<String>()
        val unsupported = mutableListOf<String>()

        val testKeys = checkBuildTags()
        if (testKeys) {
            reasons.add("Build Tags เป็น test-keys (ไม่ใช่ official release build)")
        }

        val suBin = checkSuBinaryPaths()
        if (suBin) {
            reasons.add("ตรวจพบไฟล์ Binary ของ Root (su/superuser) ในระบบ")
        }

        val rootApp = checkRootApps(context)
        if (rootApp) {
            reasons.add("ตรวจพบแอปพลิเคชันสำหรับจัดการ Root (SuperSU/Magisk/Xposed)")
        }

        val suExec = checkSuExecution()
        if (suExec) {
            reasons.add("สามารถเรียกใช้งานคำสั่ง su ในระดับ System ได้สำเร็จ")
        }

        val (rwMounts, isMountCheckSupported) = checkDangerousMounts()
        if (isMountCheckSupported) {
            if (rwMounts) {
                reasons.add("พาร์ติชัน /system ถูก Mount ในโหมด Read-Write (rw)")
            }
        } else {
            unsupported.add("การเข้าถึง /proc/mounts ไม่รองรับบน Android เวอร์ชันนี้ (Sandbox Restriction)")
        }

        val isRooted = suBin || rootApp || suExec || rwMounts

        return RootCheckResult(
            isRooted = isRooted,
            reasons = reasons,
            unsupportedChecks = unsupported,
            testKeysFound = testKeys,
            suBinaryFound = suBin,
            rootAppFound = rootApp,
            dangerousPropsFound = false,
            suExecutionSucceeded = suExec,
            rwMountsFound = rwMounts
        )
    }
}

/**
 * Singleton manager that keeps root detection actively running.
 */
object RootSecurityManager {
    private val _rootState = MutableStateFlow<RootCheckResult?>(null)
    val rootState: StateFlow<RootCheckResult?> = _rootState.asStateFlow()

    private val _isWarningAcknowledged = MutableStateFlow(false)
    val isWarningAcknowledged: StateFlow<Boolean> = _isWarningAcknowledged.asStateFlow()

    private val backgroundScope = kotlinx.coroutines.CoroutineScope(Dispatchers.IO + kotlinx.coroutines.SupervisorJob())

    fun verifyDeviceIntegrity(context: Context): RootCheckResult {
        backgroundScope.launch {
            val result = RootDetectionUtil.performFullCheck(context)
            _rootState.value = result
        }
        return _rootState.value ?: RootCheckResult(
            isRooted = false,
            reasons = emptyList(),
            unsupportedChecks = emptyList()
        )
    }

    fun verifyDeviceIntegrityAsync(context: Context, scope: kotlinx.coroutines.CoroutineScope = backgroundScope) {
        scope.launch(Dispatchers.IO) {
            val result = RootDetectionUtil.performFullCheck(context)
            _rootState.value = result
        }
    }

    fun acknowledgeWarning() {
        _isWarningAcknowledged.value = true
    }

    fun resetWarningAcknowledgement() {
        _isWarningAcknowledged.value = false
    }
}
