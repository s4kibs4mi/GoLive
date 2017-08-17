package ninja.sakib.golive.rtc

import android.opengl.EGLContext
import ninja.sakib.golive.listeners.RtcListener
import ninja.sakib.golive.utils.getNameOfFrontFacingCamera
import ninja.sakib.golive.utils.isListener
import org.webrtc.*

/**
 * := Coded with love by Sakib Sami on 6/17/17.
 * := s4kibs4mi@gmail.com
 * := www.sakib.ninja
 * := Coffee : Dream : Code
 */

class WebRtcClient(rtcListener: RtcListener, connectionParameter: RtcPeerConnectionParameter, eglContext: EGLContext) {
    var peerConnectionFactory: PeerConnectionFactory
    var iceServers = mutableListOf<PeerConnection.IceServer>()
    var rtcMediaConstraints: MediaConstraints

    var localMediaStream: MediaStream
    lateinit var localVideoSource: VideoSource
    lateinit var localVideoTrack: VideoTrack
    var localAudioSource: AudioSource
    var localAudioTrack: AudioTrack

    var rtcPeer: RtcPeer

    init {
        PeerConnectionFactory.initializeAndroidGlobals(rtcListener, true, true,
                connectionParameter.videoCodecHwAcceleration, eglContext)

        peerConnectionFactory = PeerConnectionFactory()

        iceServers.add(PeerConnection.IceServer("turn:numb.viagenie.ca", "webrtc@live.com", "muazkh"))

        rtcMediaConstraints = MediaConstraints()
        rtcMediaConstraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", isListener().toString()))
        rtcMediaConstraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", isListener().toString()))
        rtcMediaConstraints.optional.add(MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"))

        localMediaStream = peerConnectionFactory.createLocalMediaStream("ARDAMS")

        if (connectionParameter.videoCallEnabled) {
            val videoConstraints = MediaConstraints()
            videoConstraints.mandatory.add(MediaConstraints.KeyValuePair("maxHeight", Integer.toString(connectionParameter.videoHeight)))
            videoConstraints.mandatory.add(MediaConstraints.KeyValuePair("maxWidth", Integer.toString(connectionParameter.videoWidth)))
            videoConstraints.mandatory.add(MediaConstraints.KeyValuePair("maxFrameRate", Integer.toString(connectionParameter.videoFps)))
            videoConstraints.mandatory.add(MediaConstraints.KeyValuePair("minFrameRate", Integer.toString(connectionParameter.videoFps)))

            localVideoSource = peerConnectionFactory.createVideoSource(getVideoCapturer(), videoConstraints)
            localVideoTrack = peerConnectionFactory.createVideoTrack("ARDAMSv0", localVideoSource)
            localMediaStream.addTrack(localVideoTrack)
        }

        localAudioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
        localAudioTrack = peerConnectionFactory.createAudioTrack("ARDAMSa0", localAudioSource)
        localMediaStream.addTrack(localAudioTrack)

        rtcListener.onLocalStream(localMediaStream)

        rtcPeer = RtcPeer(peerConnectionFactory, iceServers, rtcMediaConstraints, localMediaStream, rtcListener)
    }

    private fun getVideoCapturer(): VideoCapturer {
        val frontCameraDeviceName = getNameOfFrontFacingCamera()
        return VideoCapturer.create(frontCameraDeviceName)
    }

    fun createAnswer(sessionDescription: SessionDescription) {
        rtcPeer.peerConnection.setRemoteDescription(rtcPeer.getSdpObserver(), sessionDescription)
        rtcPeer.peerConnection.createAnswer(rtcPeer.getSdpObserver(), rtcMediaConstraints)
    }

    fun completeSignaling(sessionDescription: SessionDescription) {
        rtcPeer.peerConnection.setRemoteDescription(rtcPeer.getSdpObserver(), sessionDescription)
    }
}
