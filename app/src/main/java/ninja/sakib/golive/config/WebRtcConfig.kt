package ninja.sakib.golive.config

import ninja.sakib.golive.utils.isListener
import org.webrtc.SessionDescription
import java.util.*

/**
 * := Coded with love by Sakib Sami on 6/17/17.
 * := s4kibs4mi@gmail.com
 * := www.sakib.ninja
 * := Coffee : Dream : Code
 */

private var streamChannel = ""
private var userChannel = ""
private var videoStream: Boolean = true
private var streamSessionDescription: SessionDescription? = null

fun getMqttUri(): String {
    return "tcp://128.199.184.221:1883"
}

fun setStreamChannelName(streamChannelName: String) {
    streamChannel = "$streamChannel$streamChannelName"
}

fun getStreamChannelName(): String {
    return streamChannel
}

fun getUserChannelName(): String {
    return userChannel
}

fun setUserChannelName(userChannelName: String) {
    userChannel = "$userChannel$userChannelName"
}

fun getPreferredChannelName(): String {
    return if (isListener()) getUserChannelName() else getStreamChannelName()
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

fun getRandomBroadcastId(): String {
    val randUID = UUID.randomUUID().toString().replace("-", "").substring(0, 5)
    return if (isListener()) "/w/u/$randUID" else "/w/ch/$randUID"
}
