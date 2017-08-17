package ninja.sakib.golive.utils

import android.content.Context
import android.hardware.Camera
import android.media.AudioManager
import android.util.Log
import com.eclipsesource.json.JsonObject
import ninja.sakib.golive.config.getPreferredChannelName
import ninja.sakib.golive.config.getUserChannelName
import org.webrtc.SessionDescription
import java.lang.Exception
import java.util.*

/**
 * := Coded with love by Sakib Sami on 6/17/17.
 * := s4kibs4mi@gmail.com
 * := www.sakib.ninja
 * := Coffee : Dream : Code
 */

private var listener: Boolean = true

fun isListener(): Boolean {
    return listener
}

fun setListener(isListener: Boolean) {
    listener = isListener
}

fun getNameOfFrontFacingCamera(): String? {
    for (i in 0..Camera.getNumberOfCameras()) {
        val cameraInfo = Camera.CameraInfo()
        try {
            Camera.getCameraInfo(i, cameraInfo)
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return "Camera $i, Facing front, Orientation ${cameraInfo.orientation}"
            }
        } catch (e: Exception) {

        }
    }
    return null
}

fun formatToRelayOnlySdp(sessionDescription: SessionDescription): SessionDescription {
    val formattedSdp = StringBuilder()
    val scanner = Scanner(sessionDescription.description)
    while (scanner.hasNextLine()) {
        val line = scanner.nextLine()
        if (line.startsWith("a=candidate") && line.contains("relay").not()) {
            continue
        }
        formattedSdp.append("${scanner.nextLine()}\n")
    }
    return SessionDescription(sessionDescription.type, formattedSdp.toString())
}

fun getPingPacket(): String {
    return JsonObject()
            .add("subscriber", getUserChannelName())
            .add("action", "ping").toString()
}

fun getPongPacket(sessionDescription: SessionDescription): String {
    return JsonObject()
            .add("subscriber", getUserChannelName())
            .add("action", "pong")
            .add("session", sessionDescription.description)
            .add("type", sessionDescription.type.name).toString()
}

fun getAnswerPacket(sessionDescription: SessionDescription): String {
    return JsonObject()
            .add("subscriber", getUserChannelName())
            .add("action", "answer")
            .add("session", sessionDescription.description)
            .add("type", sessionDescription.type.name).toString()
}

fun parseAnswer(packet: JsonObject): SessionDescription {
    return SessionDescription(SessionDescription.Type.valueOf(packet.getString("type", "")),
            packet.getString("session", ""))
}

fun parsePong(packet: JsonObject): SessionDescription {
    return SessionDescription(SessionDescription.Type.valueOf(packet.getString("type", "")),
            packet.getString("session", ""))
}

fun enableLoudSpeaker(appContext: Context) {
    val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    audioManager.isSpeakerphoneOn = true
}

fun logD(tag: String, value: String) {
    Log.d(tag, value)
}

fun logV(tag: String, value: String) {
    Log.v(tag, value)
}

fun logE(tag: String, value: String) {
    Log.e(tag, value)
}
