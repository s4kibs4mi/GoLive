package ninja.sakib.golive.views

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Point
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import ninja.sakib.golive.R
import ninja.sakib.golive.config.getUserChannelName
import ninja.sakib.golive.config.isVideoStream
import ninja.sakib.golive.listeners.RtcListener
import ninja.sakib.golive.rtc.RtcAction
import ninja.sakib.golive.rtc.RtcActionEvent
import ninja.sakib.golive.rtc.RtcPeerConnectionParameter
import ninja.sakib.golive.rtc.WebRtcClient
import ninja.sakib.golive.utils.isListener
import ninja.sakib.golive.utils.logD
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.find
import org.jetbrains.anko.toast
import org.webrtc.*

/**
 * := Coded with love by Sakib Sami on 6/17/17.
 * := s4kibs4mi@gmail.com
 * := www.sakib.ninja
 * := Coffee : Dream : Code
 */

class MainActivity : AppCompatActivity(), RtcListener {
    private val TAG = this.javaClass.simpleName

    private val VIDEO_CODEC_VP9 = "VP9"
    private val AUDIO_CODEC_OPUS = "opus"

    // To Hide Video Screen
    private val HIDDEN_VIEW_X = 1
    private val HIDDEN_VIEW_Y = 1
    private val HIDDEN_VIEW_WIDTH = 1
    private val HIDDEN_VIEW_HEIGHT = 1

    // To Show Video Screen
    private val VISIBLE_VIEW_X = 0
    private val VISIBLE_VIEW_Y = 0
    private val VISIBLE_VIEW_WIDTH = 100
    private val VISIBLE_VIEW_HEIGHT = 100

    private val scalingType = VideoRendererGui.ScalingType.SCALE_ASPECT_FILL

    private lateinit var remoteStreamView: GLSurfaceView
    private lateinit var connectingNotification: TextView

    private lateinit var mLocalVideoTrack: VideoTrack
    private lateinit var mLocalVideoRenderer: VideoRenderer
    private lateinit var localRenderCallback: VideoRenderer.Callbacks

    private lateinit var remoteRenderCallback: VideoRenderer.Callbacks
    private lateinit var mRemoteVideoTrack: VideoTrack
    private lateinit var mRemoteVideoRenderer: VideoRenderer

    private lateinit var webRtcClient: WebRtcClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        setContentView(R.layout.activity_main)

        EventBus.getDefault().register(this)    // Registering Event Bus

        onInit()    // UI Initialization

        VideoRendererGui.setView(remoteStreamView, {
            val displaySize = Point()
            windowManager.defaultDisplay.getSize(displaySize)
            val rtcPeerConnectionParameter = RtcPeerConnectionParameter(
                    true, false, displaySize.x, displaySize.y,
                    30, 1, VIDEO_CODEC_VP9, true, 1, AUDIO_CODEC_OPUS, true)

            webRtcClient = WebRtcClient(this, rtcPeerConnectionParameter, VideoRendererGui.getEGLContext())
        })

        remoteRenderCallback = VideoRendererGui.create(VISIBLE_VIEW_X, VISIBLE_VIEW_Y, VISIBLE_VIEW_WIDTH, VISIBLE_VIEW_HEIGHT, scalingType, false)
        localRenderCallback = VideoRendererGui.create(VISIBLE_VIEW_X, VISIBLE_VIEW_Y, VISIBLE_VIEW_WIDTH, VISIBLE_VIEW_HEIGHT, scalingType, true)
    }

    private fun onInit() {
        remoteStreamView = find(R.id.remoteStreamView)
        remoteStreamView.preserveEGLContextOnPause = true
        remoteStreamView.keepScreenOn = true

        connectingNotification = find(R.id.connectingNotification)

        if (isListener().not()) {
            connectingNotification.visibility = View.GONE
        }
    }

    override fun onStatusChanged(newStatus: String) {
        Log.d("OnStatusChanged", newStatus)
    }

    override fun onLocalStream(localStream: MediaStream) {
        Log.d("Rtc", "onLocalStream " + localStream.label())

        if (isVideoStream()) {
            mLocalVideoTrack = localStream.videoTracks[0]
            mLocalVideoTrack.setEnabled(true)
            mLocalVideoRenderer = VideoRenderer(localRenderCallback)
            mLocalVideoTrack.addRenderer(mLocalVideoRenderer)
        }

        VideoRendererGui.update(localRenderCallback,
                VISIBLE_VIEW_X, VISIBLE_VIEW_Y, VISIBLE_VIEW_WIDTH, VISIBLE_VIEW_HEIGHT,
                scalingType, true)
    }

    override fun onAddRemoteStream(remoteStream: MediaStream) {
        logD("VideoChannel", remoteStream.videoTracks.size.toString())

        if (isVideoStream()) {
            mRemoteVideoTrack = remoteStream.videoTracks[0]
            mRemoteVideoTrack.setEnabled(true)
            mRemoteVideoRenderer = VideoRenderer(remoteRenderCallback)
            mRemoteVideoTrack.addRenderer(mRemoteVideoRenderer)
        }

        VideoRendererGui.update(remoteRenderCallback,
                VISIBLE_VIEW_X, VISIBLE_VIEW_Y, VISIBLE_VIEW_WIDTH, VISIBLE_VIEW_HEIGHT, scalingType, true)
    }

    override fun onRemoteDisconnected() {
        Log.d(TAG, "Peers Disconnected")
    }

    override fun onRemoteConnected() {
        Log.d(TAG, "Peers Connected")

        if (isListener()) {
            runOnUiThread {
                connectingNotification.visibility = View.GONE
            }

            VideoRendererGui.update(localRenderCallback,
                    HIDDEN_VIEW_X, HIDDEN_VIEW_Y, HIDDEN_VIEW_WIDTH, HIDDEN_VIEW_HEIGHT,
                    scalingType, true)
        }
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)  // UnRegistering Event Bus
        super.onDestroy()
    }

    override fun onSdpReady() {
        runOnUiThread {
            if (isListener().not()) {
                title = "$title - ${getUserChannelName()}"
                toast("Your stream is ready.")
            }
        }
    }

    @Subscribe
    fun onRtcEvent(rtcActionEvent: RtcActionEvent) {
        logD(TAG, "OnRtcActionEvent")

        when (rtcActionEvent.rtcAction) {
            RtcAction.PING -> {
                onPing()
            }
            RtcAction.PONG -> {
                onPong(rtcActionEvent.rtcData as SessionDescription)
            }
            RtcAction.ANSWER -> {
                onAnswer(rtcActionEvent.rtcData as SessionDescription)
            }
            else -> {
                logD(TAG, "Unknown bus event received")
            }
        }
    }

    private fun onPing() {

    }

    private fun onPong(sessionDescription: SessionDescription?) {
        Log.d(TAG, "Pong Received")

        if (sessionDescription != null) {
            Log.d(TAG, "Session Not NULL")
            webRtcClient.createAnswer(sessionDescription)
        } else {
            Log.d(TAG, "Session NULL")
        }
    }

    private fun onAnswer(sessionDescription: SessionDescription?) {
        Log.d(TAG, "Answer Received")

        if (sessionDescription != null) {
            Log.d(TAG, "Session Not NULL")
            webRtcClient.completeSignaling(sessionDescription)
        } else {
            Log.d(TAG, "Session NULL")
        }
    }

    override fun onBackPressed() {
        val nextIntent = Intent(applicationContext, LauncherActivity::class.java)
        startActivity(nextIntent)
        finish()
    }
}
