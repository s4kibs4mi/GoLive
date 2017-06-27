package ninja.sakib.golive.listeners

import org.webrtc.SessionDescription

/**
 * := Coded with love by Sakib Sami on 6/18/17.
 * := s4kibs4mi@gmail.com
 * := www.sakib.ninja
 * := Coffee : Dream : Code
 */

interface RtcActionListener {
    fun onPing()
    fun onPong(sessionDescription: SessionDescription?)
    fun onAnswer(sessionDescription: SessionDescription?)
}
