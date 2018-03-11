package nz.bradcampbell.uselesssilence

import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.media.AudioManager
import android.view.KeyEvent

class MusicIntentReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    when (intent.action) {
      AudioManager.ACTION_AUDIO_BECOMING_NOISY -> {
        // E.g. headphones disconnected
        context.startService(UselessSilenceService.stopIntent(context))
      }
      Intent.ACTION_MEDIA_BUTTON -> {
        val keyEvent = intent.extras?.get(Intent.EXTRA_KEY_EVENT) as KeyEvent?
        if (keyEvent?.action != KeyEvent.ACTION_DOWN) {
          return
        }
        when (keyEvent.keyCode) {
          KeyEvent.KEYCODE_MEDIA_PLAY -> {
            context.startService(UselessSilenceService.startIntent(context))
          }
          KeyEvent.KEYCODE_MEDIA_STOP -> {
            context.startService(UselessSilenceService.stopIntent(context))
          }
        }
      }
    }
  }
}
