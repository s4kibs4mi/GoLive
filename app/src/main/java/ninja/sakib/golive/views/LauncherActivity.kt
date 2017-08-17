package ninja.sakib.golive.views

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

import ninja.sakib.golive.R
import ninja.sakib.golive.config.getRandomBroadcastId
import ninja.sakib.golive.config.setUserChannelName
import ninja.sakib.golive.config.setStreamChannelName
import ninja.sakib.golive.rtc.MqttSubscribeEvent
import ninja.sakib.golive.services.ConnectionService
import ninja.sakib.golive.utils.enableLoudSpeaker
import ninja.sakib.golive.utils.setListener
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.find
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.toast

/**
 * := Coded with love by Sakib Sami on 6/17/17.
 * := s4kibs4mi@gmail.com
 * := www.sakib.ninja
 * := Coffee : Dream : Code
 */

class LauncherActivity : AppCompatActivity() {
    private lateinit var radioStreamBtn: Button
    private lateinit var radioListenBtn: Button
    private lateinit var channelName: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        radioStreamBtn = find(R.id.radioStreamBtn)
        radioListenBtn = find(R.id.radioListenBtn)
        channelName = find(R.id.channelName)

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
        if (isChannelNameEmpty().not()) {
            setListener(true)
            setStreamChannelName(channelName.text.toString())
            setUserChannelName(getRandomBroadcastId())
            enableLoudSpeaker(this)
            startStreamActivity()
        } else {
            toast("Channel name must be non empty.")
        }
    }

    private fun onDoStream() {
        setListener(false)
        setUserChannelName(getRandomBroadcastId())
        startStreamActivity()
    }

    private fun startStreamActivity() {
        EventBus.getDefault().post(MqttSubscribeEvent())

        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun isChannelNameEmpty(): Boolean {
        return channelName.text.toString().isEmpty()
    }
}
