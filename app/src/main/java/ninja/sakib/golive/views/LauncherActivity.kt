package ninja.sakib.golive.views

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

import ninja.sakib.golive.R
import ninja.sakib.golive.rtc.MqttSubscribeEvent
import ninja.sakib.golive.services.ConnectionService
import ninja.sakib.golive.utils.enableLoudSpeaker
import ninja.sakib.golive.utils.setListener
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.find
import org.jetbrains.anko.sdk25.coroutines.onClick

/**
 * := Coded with love by Sakib Sami on 6/17/17.
 * := s4kibs4mi@gmail.com
 * := www.sakib.ninja
 * := Coffee : Dream : Code
 */

class LauncherActivity : AppCompatActivity() {
    private lateinit var radioStreamBtn: Button
    private lateinit var radioListenBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        radioStreamBtn = find(R.id.radioStreamBtn)
        radioListenBtn = find(R.id.radioListenBtn)

        radioStreamBtn.onClick {
            onDoStream()
        }
        radioListenBtn.onClick {
            onDoListen()
        }

        initConnectionService()
    }

    private fun initConnectionService() {
        val serviceIntent = Intent(applicationContext, ConnectionService::class.java)
        startService(serviceIntent)
    }

    private fun onDoListen() {
        setListener(true)
        enableLoudSpeaker(this)
        startStreamActivity()
    }

    private fun onDoStream() {
        setListener(false)
        startStreamActivity()
    }

    private fun startStreamActivity() {
        EventBus.getDefault().post(MqttSubscribeEvent())

        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
