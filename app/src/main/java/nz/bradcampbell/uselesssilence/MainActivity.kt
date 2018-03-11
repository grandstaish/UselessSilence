package nz.bradcampbell.uselesssilence

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    findViewById<View>(R.id.start).setOnClickListener {
      startService(UselessSilenceService.startIntent(this))
    }

    findViewById<View>(R.id.stop).setOnClickListener {
      startService(UselessSilenceService.stopIntent(this))
    }
  }
}
