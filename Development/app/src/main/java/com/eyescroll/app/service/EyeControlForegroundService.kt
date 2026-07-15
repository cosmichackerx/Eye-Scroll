package com.eyescroll.app.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.eyescroll.app.EyeScrollApp
import com.eyescroll.app.R
import com.eyescroll.app.data.SettingsRepository
import com.eyescroll.app.domain.gesture.GestureStateMachine
import com.eyescroll.app.domain.model.GestureType
import com.eyescroll.app.presentation.MainActivity
import com.eyescroll.app.vision.FaceLandmarkerHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class EyeControlForegroundService : LifecycleService() {

    private val settings: SettingsRepository by inject()
    private val machine = GestureStateMachine()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private val analysisBusy = AtomicBoolean(false)
    private var helper: FaceLandmarkerHelper? = null
    private var paused = false
    private var settingsJob: Job? = null
    private var lastFrameMs = 0L

    override fun onCreate() {
        super.onCreate()
        startAsForeground(getString(R.string.notification_text))
        helper = FaceLandmarkerHelper(this) { signals ->
            if (paused) return@FaceLandmarkerHelper
            val cmd = machine.process(signals) ?: return@FaceLandmarkerHelper
            when (cmd.type) {
                GestureType.VOLUME_UP -> adjustVolume(1)
                GestureType.VOLUME_DOWN -> adjustVolume(-1)
                else -> Unit
            }
            CommandBus.publish(cmd)
        }
        helper?.start()
        settingsJob = scope.launch {
            settings.settings.collect { s ->
                paused = s.detectionPaused
                machine.updateConfig(
                    thresholds = settings.thresholdsFor(s.sensitivity),
                    toggles = s.toggles,
                    noseBaseline = s.noseBaseline
                )
                updateNotification(
                    if (paused) getString(R.string.notification_paused)
                    else getString(R.string.notification_text)
                )
            }
        }
        bindCamera()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_STOP -> {
                scope.launch { settings.setServiceDesired(false) }
                stopSelf()
            }
            ACTION_PAUSE -> scope.launch { settings.setDetectionPaused(true) }
            ACTION_RESUME -> scope.launch { settings.setDetectionPaused(false) }
            ACTION_TOGGLE_PAUSE -> scope.launch {
                val cur = settings.detectionPaused.first()
                settings.setDetectionPaused(!cur)
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        settingsJob?.cancel()
        scope.cancel()
        runCatching {
            ProcessCameraProvider.getInstance(this).get().unbindAll()
        }
        helper?.clear()
        cameraExecutor.shutdown()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    private fun bindCamera() {
        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            val provider = runCatching { providerFuture.get() }.getOrNull() ?: return@addListener
            val selector = CameraSelector.DEFAULT_FRONT_CAMERA
            val resolutionSelector = ResolutionSelector.Builder()
                .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
                .build()
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setResolutionSelector(resolutionSelector)
                .build()
            analysis.setAnalyzer(cameraExecutor) { proxy ->
                val now = SystemClock.elapsedRealtime()
                if (now - lastFrameMs < 45L) {
                    proxy.close()
                    return@setAnalyzer
                }
                lastFrameMs = now
                if (!analysisBusy.compareAndSet(false, true)) {
                    proxy.close()
                    return@setAnalyzer
                }
                try {
                    helper?.processImageProxy(proxy, isFrontCamera = true)
                } finally {
                    analysisBusy.set(false)
                }
            }
            runCatching {
                provider.unbindAll()
                provider.bindToLifecycle(this, selector, analysis)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun adjustVolume(direction: Int) {
        val am = getSystemService(AUDIO_SERVICE) as AudioManager
        val dir = if (direction > 0) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
        am.adjustStreamVolume(AudioManager.STREAM_MUSIC, dir, AudioManager.FLAG_SHOW_UI)
    }

    private fun startAsForeground(text: String) {
        val notification = buildNotification(text)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification(text: String) {
        val nm = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        nm.notify(NOTIFICATION_ID, buildNotification(text))
    }

    private fun buildNotification(text: String): Notification {
        val open = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val pause = pendingService(ACTION_TOGGLE_PAUSE, 1)
        val stop = pendingService(ACTION_STOP, 2)
        return NotificationCompat.Builder(this, EyeScrollApp.CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(open)
            .setOngoing(true)
            .addAction(0, getString(R.string.action_pause), pause)
            .addAction(0, getString(R.string.action_stop), stop)
            .addAction(0, getString(R.string.action_open), open)
            .build()
    }

    private fun pendingService(action: String, req: Int): PendingIntent {
        val i = Intent(this, EyeControlForegroundService::class.java).setAction(action)
        return PendingIntent.getService(
            this,
            req,
            i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val NOTIFICATION_ID = 42
        const val ACTION_STOP = "com.eyescroll.app.STOP"
        const val ACTION_PAUSE = "com.eyescroll.app.PAUSE"
        const val ACTION_RESUME = "com.eyescroll.app.RESUME"
        const val ACTION_TOGGLE_PAUSE = "com.eyescroll.app.TOGGLE_PAUSE"

        fun start(context: Context) {
            val i = Intent(context, EyeControlForegroundService::class.java)
            ContextCompat.startForegroundService(context, i)
        }

        fun stop(context: Context) {
            val i = Intent(context, EyeControlForegroundService::class.java).setAction(ACTION_STOP)
            context.startService(i)
        }
    }
}
