package ninja.sakib.golive.config

import org.webrtc.SessionDescription

/**
 * := Coded with love by Sakib Sami on 6/17/17.
 * := s4kibs4mi@gmail.com
 * := www.sakib.ninja
 * := Coffee : Dream : Code
 */

private var streamTopic = "/w/ch/101"
private var videoStream: Boolean = true
private var streamSessionDescription: SessionDescription? = null

fun getMqttUri(): String {
    return "tcp://iot.eclipse.org:1883"
}

fun getStreamTopic(): String {
    return streamTopic
}

fun getSubscriptionTopic(): String {
    return "/w/ch/1234567890"
}

fun setStreamSessionDescription(sessionDescription: SessionDescription) {
    streamSessionDescription = sessionDescription
}

fun getStreamSessionDescription(): SessionDescription? {
    return streamSessionDescription
}

fun isVideoStream(): Boolean {
    return videoStream
}

fun setVideoStream(isVideoStream: Boolean) {
    videoStream = isVideoStream
}
