package ninja.sakib.golive.listeners

import org.webrtc.MediaStream

/**
 * := Coded with love by Sakib Sami on 6/17/17.
 * := s4kibs4mi@gmail.com
 * := www.sakib.ninja
 * := Coffee : Dream : Code
 */

interface RtcListener {
    fun onStatusChanged(newStatus: String)

    fun onLocalStream(localStream: MediaStream)

    fun onAddRemoteStream(remoteStream: MediaStream)

    fun onRemoteDisconnected()

    fun onRemoteConnected()

    fun onSdpReady()
}
