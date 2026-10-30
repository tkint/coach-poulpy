package ch.coachpoulpy.android

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import ch.coachpoulpy.shared.Cancellable
import ch.coachpoulpy.shared.LiveMetric
import ch.coachpoulpy.shared.LiveModel
import ch.coachpoulpy.shared.LiveUiState
import java.util.Locale

/**
 * Keeps a Live recording going with the screen off or another app in front: a connected-device foreground service,
 * from the moment Live records until the recording finishes or is discarded. Its ongoing notification says what is
 * recorded, for how long and how far, and opens Live.
 */
class LiveRecordingService : Service() {
    private var watch: Cancellable? = null

    /** What the notification shows, so it is only posted again when that changes. */
    private var shown: Any? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val live = (application as CoachPoulpyApp).graph.liveIfCreated
        // Android expects the service in the foreground at once, even when the recording has just ended.
        val first = notification(live?.state?.value)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, first, ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)
            } else {
                startForeground(NOTIFICATION_ID, first)
            }
        } catch (e: RuntimeException) {
            // Refused by the system: the recording goes on, only less sure to survive in the background.
            stopSelf()
            return START_NOT_STICKY
        }
        if (live == null) return finish()
        watch?.cancel()
        watch = live.watch(::onState)
        return START_NOT_STICKY
    }

    private fun onState(state: LiveUiState) {
        if (state.recording == null) {
            finish()
            return
        }
        val key = key(state)
        if (key == shown) return
        shown = key
        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, notification(state))
    }

    private fun finish(): Int {
        watch?.cancel()
        watch = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        watch?.cancel()
        super.onDestroy()
    }

    /** Running, the system clock counts the time; paused, the time stands still in the text. Distance to the 10 m. */
    private fun key(state: LiveUiState): Any? {
        val recording = state.recording ?: return null
        val distance = distance(state)?.let { (value, _) -> (value * 100).toLong() }
        return listOf(recording.title, recording.paused, recording.elapsedSeconds.takeIf { recording.paused }, distance)
    }

    private fun distance(state: LiveUiState): Pair<Double, String>? =
        state.tiles.firstOrNull { it.metric == LiveMetric.DISTANCE }?.let { tile -> tile.value?.let { it to tile.unit } }

    private fun notification(state: LiveUiState?): Notification {
        channel()
        val recording = state?.recording
        val open = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(MainActivity.OPEN_LIVE, true),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val text = when {
            recording == null -> getString(R.string.live_notificationRecording)
            recording.paused -> getString(R.string.live_notificationPaused, clock(recording.elapsedSeconds))
            else -> state?.let(::distance)?.let { (value, unit) -> "%.2f %s".format(Locale.getDefault(), value, unit) }
                ?: getString(R.string.live_notificationRecording)
        }
        val running = recording != null && !recording.paused
        return Notification.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(recording?.title?.ifEmpty { null } ?: getString(R.string.tab_live))
            .setContentText(text)
            .setContentIntent(open)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(Notification.CATEGORY_WORKOUT)
            .setShowWhen(running)
            .setUsesChronometer(running)
            .setWhen(System.currentTimeMillis() - (recording?.elapsedSeconds ?: 0) * 1000)
            .build()
    }

    private fun channel() {
        val channel = NotificationChannel(CHANNEL, getString(R.string.live_notificationChannel), NotificationManager.IMPORTANCE_LOW)
        channel.description = getString(R.string.live_notificationChannelInfo)
        channel.setShowBadge(false)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL = "live-recording"
        private const val NOTIFICATION_ID = 1

        private var following: Cancellable? = null

        /** Starts the service each time [live] starts recording, for as long as the app runs. Called once Live exists. */
        fun follow(context: Context, live: LiveModel) {
            if (following != null) return
            val app = context.applicationContext
            var recording = false
            following = live.watch { state ->
                val now = state.recording != null
                if (now && !recording) start(app)
                recording = now
            }
        }

        private fun start(context: Context) {
            // Android 14 runs a connected-device service only with a Bluetooth permission: a timer-only
            // recording without one goes on in the app alone.
            if (Build.VERSION.SDK_INT >= 34 &&
                context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            try {
                context.startForegroundService(Intent(context, LiveRecordingService::class.java))
            } catch (e: IllegalStateException) {
                // Android 12 refuses a start from the background, as when the belt starts Live while the app is
                // hidden: the recording goes on without the service.
            }
        }

        private fun clock(seconds: Long): String {
            val h = seconds / 3600
            val m = (seconds % 3600) / 60
            val s = seconds % 60
            return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
        }
    }
}
