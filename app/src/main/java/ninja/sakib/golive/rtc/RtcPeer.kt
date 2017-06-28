package ninja.sakib.golive.rtc

import android.util.Log
import ninja.sakib.golive.config.getChannelName
import ninja.sakib.golive.config.getStreamSessionDescription
import ninja.sakib.golive.config.setStreamSessionDescription
import ninja.sakib.golive.listeners.RtcListener
import ninja.sakib.golive.utils.getAnswerRequest
import ninja.sakib.golive.utils.getPingRequest
import ninja.sakib.golive.utils.isListener
import org.greenrobot.eventbus.EventBus
import org.webrtc.*

/**
 * := Coded with love by Sakib Sami on 6/17/17.
 * := s4kibs4mi@gmail.com
 * := www.sakib.ninja
 * := Coffee : Dream : Code
 */

class RtcPeer(peerConnectionFactory: PeerConnectionFactory, iceServers: MutableList<PeerConnection.IceServer>,
              rtcMediaConstraints: MediaConstraints, localMediaStream: MediaStream, rtcListener: RtcListener) : SdpObserver, PeerConnection.Observer {
    private var TAG = this.javaClass.simpleName
    private var rtcListener: RtcListener
    private var rtcMediaConstraints: MediaConstraints

    var peerConnection: PeerConnection = peerConnectionFactory.createPeerConnection(iceServers, rtcMediaConstraints, this)

    init {
        peerConnection.addStream(localMediaStream)

        this.rtcListener = rtcListener
        this.rtcMediaConstraints = rtcMediaConstraints

        rtcListener.onStatusChanged("CONNECTED")

        if (isListener().not()) {
            peerConnection.createOffer(getSdpObserver(), rtcMediaConstraints)
        } else {
            val mqttPublishEvent = MqttPublishEvent()
            mqttPublishEvent.topic = getChannelName()
            mqttPublishEvent.message = getPingRequest()
            EventBus.getDefault().post(mqttPublishEvent)
        }
    }

    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
        if (p0 != null && p0 == PeerConnection.IceGatheringState.COMPLETE) {
            Log.d(TAG, "ICE Complete")

            if (peerConnection.localDescription.type == SessionDescription.Type.OFFER) {
                Log.d(TAG, "SDP Offer")
                setStreamSessionDescription(peerConnection.localDescription)
                Log.d(TAG, getStreamSessionDescription()!!.description)
            } else if (peerConnection.localDescription.type == SessionDescription.Type.ANSWER) {
                Log.d(TAG, "SDP Answer")
                val mqttPublishEvent = MqttPublishEvent()
                mqttPublishEvent.topic = getChannelName()
                mqttPublishEvent.message = getAnswerRequest(peerConnection.localDescription)
                EventBus.getDefault().post(mqttPublishEvent)
            } else {
                Log.d(TAG, "SDP Unknown")
            }
        }
    }

    override fun onAddStream(p0: MediaStream?) {
        if (p0 != null) {
            rtcListener.onAddRemoteStream(p0)
        }
    }

    override fun onIceCandidate(p0: IceCandidate?) {
        peerConnection.addIceCandidate(p0)
    }

    override fun onDataChannel(p0: DataChannel?) {

    }

    override fun onSignalingChange(p0: PeerConnection.SignalingState?) {

    }

    override fun onRemoveStream(p0: MediaStream?) {

    }

    override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
        if (p0 == PeerConnection.IceConnectionState.CONNECTED) {
            rtcListener.onRemoteConnected()
        } else if (p0 == PeerConnection.IceConnectionState.DISCONNECTED) {
            rtcListener.onRemoteDisconnected()
        }
    }

    override fun onRenegotiationNeeded() {

    }

    override fun onSetFailure(p0: String?) {
        Log.d("IceSet", "Failure : $p0")
    }

    override fun onSetSuccess() {
        Log.d("IceSet", "Success")
    }

    override fun onCreateSuccess(p0: SessionDescription?) {
        Log.d(TAG, "IceCreated")
        peerConnection.setLocalDescription(this, p0)
    }

    override fun onCreateFailure(p0: String?) {
        Log.d("IceCreate", "Failure : $p0")
    }

    fun getSdpObserver(): SdpObserver {
        return this
    }
}
