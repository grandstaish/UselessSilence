package nz.bradcampbell.uselesssilence

import android.app.Notification
import android.app.NotificationChannel
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.os.PowerManager
import android.media.AudioManager
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.content.Context
import android.app.PendingIntent
import android.graphics.drawable.Icon
import android.media.AudioAttributes

class UselessSilenceService : Service() {
  enum class State {
    PLAYING,
    STOPPED
  }

  companion object {
    private const val NOTIFICATION_ID = 1
    private const val ACTION_START = "nz.bradcampbell.uselessbluetoothapp.action.START"
    private const val ACTION_STOP = "nz.bradcampbell.uselessbluetoothapp.action.STOP"

    @JvmStatic fun startIntent(context: Context): Intent {
      val intent = Intent(context, UselessSilenceService::class.java)
      intent.action = UselessSilenceService.ACTION_START
      return intent
    }

    @JvmStatic fun stopIntent(context: Context): Intent {
      val intent = Intent(context, UselessSilenceService::class.java)
      intent.action = UselessSilenceService.ACTION_STOP
      return intent
    }
  }

  private var player: MediaPlayer? = null
  private var notification: Notification? = null

  private lateinit var notificationManager: NotificationManager
  private lateinit var audioManager: AudioManager

  private var state: State = State.STOPPED

  override fun onCreate() {
    notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
  }

  override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
    when (intent.action) {
      ACTION_START -> processStartRequest()
      ACTION_STOP -> processStopRequest()
    }
    return Service.START_NOT_STICKY
  }

  override fun onDestroy() {
    state = State.STOPPED
    relaxResources()
  }

  override fun onBind(intent: Intent?): IBinder? {
    return null
  }

  private fun createMediaPlayer() {
    player = MediaPlayer.create(applicationContext, R.raw.two_seconds_of_silence).apply {
      val attributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_UNKNOWN).build()
      setAudioAttributes(attributes)
      isLooping = true
      setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
    }
  }

  private fun processStartRequest() {
    if (state == State.STOPPED) {
      state = State.PLAYING
      createMediaPlayer()
      setUpAsForeground()
      player?.start()
    }
  }

  private fun processStopRequest() {
    if (state == State.PLAYING) {
      state = State.STOPPED
      relaxResources()
      stopSelf()
    }
  }

  private fun setUpAsForeground() {
    val pi = PendingIntent.getActivity(applicationContext, 0,
        Intent(applicationContext, MainActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT)

    val channelTitle = getString(R.string.notification_channel_main_title)
    val channel = NotificationChannel("main", channelTitle, IMPORTANCE_DEFAULT)

    notificationManager.createNotificationChannel(channel)

    val notificationTitle = getString(R.string.app_name)
    val notificationText = getString(R.string.app_description)

    val stopIcon = Icon.createWithResource(applicationContext, R.drawable.ic_stop_black)
    val stopTitle = getString(R.string.stop)
    val stopIntent = PendingIntent.getService(applicationContext, 0,
        stopIntent(applicationContext), 0)

    notification = Notification.Builder(applicationContext, channel.id)
        .setSmallIcon(R.drawable.ic_play_arrow)
        .setOngoing(true)
        .setContentIntent(pi)
        .setContentTitle(notificationTitle)
        .setContentText(notificationText)
        .setVisibility(Notification.VISIBILITY_PUBLIC)
        .addAction(Notification.Action.Builder(stopIcon, stopTitle, stopIntent).build())
        .build()

    startForeground(NOTIFICATION_ID, notification)
  }

  private fun relaxResources() {
    stopForeground(true)

    player?.reset()
    player?.release()
    player = null
  }
}
