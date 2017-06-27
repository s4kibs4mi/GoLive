package ninja.sakib.golive.rtc

/**
 * := Coded with love by Sakib Sami on 6/17/17.
 * := s4kibs4mi@gmail.com
 * := www.sakib.ninja
 * := Coffee : Dream : Code
 */

class RtcPeerConnectionParameter(videoCallEnabled: Boolean, loopback: Boolean,
                                 videoWidth: Int, videoHeight: Int, videoFps: Int, videoStartBitrate: Int,
                                 videoCodec: String, videoCodecHwAcceleration: Boolean,
                                 audioStartBitrate: Int, audioCodec: String,
                                 cpuOveruseDetection: Boolean) {

    val videoCallEnabled: Boolean = videoCallEnabled
    val loopback: Boolean = loopback
    val videoWidth: Int = videoWidth
    val videoHeight: Int = videoHeight
    val videoFps: Int = videoFps
    val videoStartBitrate: Int = videoStartBitrate
    val videoCodec: String = videoCodec
    val videoCodecHwAcceleration: Boolean = videoCodecHwAcceleration
    val audioStartBitrate: Int = audioStartBitrate
    val audioCodec: String = audioCodec
    val cpuOveruseDetection: Boolean = cpuOveruseDetection
}
