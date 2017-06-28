package ninja.sakib.golive.views

import android.content.Intent
import android.graphics.Point
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Window
import android.view.WindowManager
import com.afollestad.materialdialogs.MaterialDialog
import ninja.sakib.golive.R
import ninja.sakib.golive.listeners.RtcListener
import ninja.sakib.golive.rtc.RtcAction
import ninja.sakib.golive.rtc.RtcActionEvent
import ninja.sakib.golive.rtc.RtcPeerConnectionParameter
import ninja.sakib.golive.rtc.WebRtcClient
import ninja.sakib.golive.utils.logD
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.find
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
    // Local preview screen position before call is connected.
    private val LOCAL_X_CONNECTING = 0
    private val LOCAL_Y_CONNECTING = 0
    private val LOCAL_WIDTH_CONNECTING = 100
    private val LOCAL_HEIGHT_CONNECTING = 100
    // Local preview screen position after call is connected.
    private val LOCAL_X_CONNECTED = 72
    private val LOCAL_Y_CONNECTED = 72
    private val LOCAL_WIDTH_CONNECTED = 25
    private val LOCAL_HEIGHT_CONNECTED = 25
    // Remote video screen position
    private val REMOTE_X = 0
    private val REMOTE_Y = 0
    private val REMOTE_WIDTH = 100
    private val REMOTE_HEIGHT = 100
    private val scalingType = VideoRendererGui.ScalingType.SCALE_ASPECT_FILL
    private lateinit var remoteStreamView: GLSurfaceView

    private lateinit var mLocalVideoTrack: VideoTrack
    private lateinit var mLocalVideoRenderer: VideoRenderer
    private lateinit var localRenderCallback: VideoRenderer.Callbacks

    private lateinit var remoteRenderCallback: VideoRenderer.Callbacks
    private lateinit var mRemoteVideoTrack: VideoTrack
    private lateinit var mRemoteVideoRenderer: VideoRenderer

    private lateinit var webRtcClient: WebRtcClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        setContentView(R.layout.activity_main)

        EventBus.getDefault().register(this)    // Registering Event Bus

        remoteStreamView = find(R.id.remoteStreamView)
        remoteStreamView.preserveEGLContextOnPause = true
        remoteStreamView.keepScreenOn = true

        VideoRendererGui.setView(remoteStreamView, {
            val displaySize = Point()
            windowManager.defaultDisplay.getSize(displaySize)
            val rtcPeerConnectionParameter = RtcPeerConnectionParameter(
                    true, false, displaySize.x, displaySize.y,
                    30, 1, VIDEO_CODEC_VP9, true, 1, AUDIO_CODEC_OPUS, true)

            webRtcClient = WebRtcClient(this, rtcPeerConnectionParameter, VideoRendererGui.getEGLContext())
        })

        remoteRenderCallback = VideoRendererGui.create(REMOTE_X, REMOTE_Y, REMOTE_WIDTH, REMOTE_HEIGHT, scalingType, false)
        localRenderCallback = VideoRendererGui.create(LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING, LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING, scalingType, true)
    }

    override fun onStatusChanged(newStatus: String) {
        Log.d("OnStatusChanged", newStatus)
    }

    override fun onLocalStream(localStream: MediaStream) {
        Log.d("Rtc", "onLocalStream " + localStream.label())

        if (true) {
            mLocalVideoTrack = localStream.videoTracks[0]
            mLocalVideoTrack.setEnabled(true)
            mLocalVideoRenderer = VideoRenderer(localRenderCallback)
            mLocalVideoTrack.addRenderer(mLocalVideoRenderer)
        }

        VideoRendererGui.update(localRenderCallback,
                LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING,
                LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING,
                scalingType, true)
    }

    override fun onAddRemoteStream(remoteStream: MediaStream) {
        if (true) {
            mRemoteVideoTrack = remoteStream.videoTracks[0]
            mRemoteVideoTrack.setEnabled(true)
            mRemoteVideoRenderer = VideoRenderer(remoteRenderCallback)
            mRemoteVideoTrack.addRenderer(mRemoteVideoRenderer)
        }

        VideoRendererGui.update(remoteRenderCallback,
                REMOTE_X, REMOTE_Y,
                REMOTE_WIDTH, REMOTE_HEIGHT, scalingType, false)
    }

    override fun onRemoteDisconnected() {
        Log.d(TAG, "Peers Disconnected")
    }

    override fun onRemoteConnected() {
        Log.d(TAG, "Peers Connected")
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)  // UnRegistering Event Bus
        super.onDestroy()
    }

    override fun onSdpReady() {

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
