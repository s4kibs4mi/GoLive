package ninja.sakib.golive.rtc

import android.util.Log
import ninja.sakib.golive.listeners.RtcActionListener
import com.eclipsesource.json.Json
import ninja.sakib.golive.config.*
import ninja.sakib.golive.utils.*
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

/**
 * := Coded with love by Sakib Sami on 6/17/17.
 * := s4kibs4mi@gmail.com
 * := www.sakib.ninja
 * := Coffee : Dream : Code
 */

class MqttPeer : MqttCallbackExtended, IMqttActionListener {
    private var TAG = this.javaClass.simpleName
    private var mqttClient: MqttAsyncClient? = null

    private var rtcActionListener: RtcActionListener? = null

    override fun onSuccess(asyncActionToken: IMqttToken?) {
        Log.d(TAG, "MQTT connection success")
    }

    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
        Log.d(TAG, "MQTT connection failed. ${exception!!.message}")
    }

    fun connect() {
        if (mqttClient == null) {
            val mqttOptions = MqttConnectOptions()
            mqttOptions.isAutomaticReconnect = true
            mqttOptions.isCleanSession = true

            mqttClient = MqttAsyncClient(getMqttUri(), MqttAsyncClient.generateClientId(), MemoryPersistence())
            mqttClient!!.setCallback(this)
            mqttClient!!.connect(mqttOptions, null, this)
            Log.d(TAG, "MQTT connection requested")
        }
    }

    override fun connectComplete(reconnect: Boolean, serverURI: String?) {
        Log.d(TAG, "Connected to MQTT. IsReconnect ? $reconnect")
    }

    fun onSubscribe() {
        if (isListener()) {
            logD(TAG, "IAmListener")
            mqttClient!!.subscribe(getUserChannelName(), 0)
        } else {
            logD(TAG, "IAmPublisher")
            mqttClient!!.subscribe(getUserChannelName(), 0)
        }
    }

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        if (message != null) {
            val packet = Json.parse(String(message.payload)).asObject()
            logD("Received Raw Packet", packet.toString())

            val rtcAction = RtcAction.valueOf(packet.getString("action", "").toUpperCase())
            val subscriber = packet.getString("subscriber", "")

            when (rtcAction) {
                RtcAction.PING -> {
                    val pingResult = getPongPacket(getStreamSessionDescription()!!)
                    mqttClient!!.publish(subscriber, pingResult.toByteArray(), 0, false)
                    if (rtcActionListener != null) {
                        rtcActionListener!!.onPing()
                    }
                }
                RtcAction.PONG -> {
                    val pongResult = parsePong(packet)
                    if (rtcActionListener != null) {
                        rtcActionListener!!.onPong(pongResult)
                    }
                }
                RtcAction.ANSWER -> {
                    val answerResult = parseAnswer(packet)
                    if (rtcActionListener != null) {
                        rtcActionListener!!.onAnswer(answerResult)
                    }
                }
                else -> {
                    Log.d(TAG, "Unknown Action")
                }
            }
        }
    }

    fun addRtcActionListener(rtcActionListener: RtcActionListener) {
        this.rtcActionListener = rtcActionListener
    }

    override fun connectionLost(cause: Throwable?) {
        Log.d(TAG, "Connection lost. ${cause!!.message}")
        mqttClient!!.reconnect()
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {

    }

    fun publish(topic: String?, message: String?) {
        if (mqttClient != null && topic != null && message != null) {
            mqttClient!!.publish(topic, message.toByteArray(), 0, false)
            Log.d(TAG, "Request Published")
        }
    }
}
