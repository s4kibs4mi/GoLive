package ninja.sakib.golive.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import ninja.sakib.golive.listeners.RtcActionListener
import ninja.sakib.golive.rtc.*
import ninja.sakib.golive.utils.logD
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.doAsync
import org.webrtc.SessionDescription
import kotlin.properties.Delegates

/**
 * := Coded with love by Sakib Sami on 6/23/17.
 * := s4kibs4mi@gmail.com
 * := www.sakib.ninja
 * := Coffee : Dream : Code
 */

class ConnectionService : Service(), RtcActionListener {
    private var TAG = this.javaClass.simpleName
    private var mqttPeer: MqttPeer by Delegates.notNull<MqttPeer>()

    init {
        if (EventBus.getDefault().isRegistered(this).not()) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        doAsync {
            startNetworkService()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    private fun startNetworkService() {
        mqttPeer = MqttPeer()
        mqttPeer.connect()
        mqttPeer.addRtcActionListener(this)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onPing() {
        val event = RtcActionEvent()
        event.rtcAction = RtcAction.PING
        EventBus.getDefault().post(event)
    }

    override fun onPong(sessionDescription: SessionDescription?) {
        val event = RtcActionEvent()
        event.rtcAction = RtcAction.PONG
        event.rtcData = sessionDescription
        EventBus.getDefault().post(event)
    }

    override fun onAnswer(sessionDescription: SessionDescription?) {
        val event = RtcActionEvent()
        event.rtcAction = RtcAction.ANSWER
        event.rtcData = sessionDescription
        EventBus.getDefault().post(event)
    }

    @Subscribe
    fun onMqttPublishEvent(mqttPublishEvent: MqttPublishEvent) {
        logD(TAG, "OnMqttPublishEventSend")
        mqttPeer.publish(mqttPublishEvent.topic, mqttPublishEvent.message)
    }

    @Subscribe
    fun onMqttSubscribeEvent(mqttSubscribeEvent: MqttSubscribeEvent) {
        logD(TAG, "OnMqttPublishEventSubscribe")
        mqttPeer.onSubscribe()
    }
}
