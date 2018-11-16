package com.example.shafayat.testrtp

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.orhanobut.logger.Logger
import kotlinx.android.synthetic.main.activity_main.*
import net.majorkernelpanic.streaming.Session

import net.majorkernelpanic.streaming.SessionBuilder
import net.majorkernelpanic.streaming.video.VideoQuality
import net.majorkernelpanic.streaming.audio.AudioQuality
import java.lang.Exception
import net.majorkernelpanic.streaming.rtsp.RtspServer
import android.content.Intent
import android.preference.PreferenceManager
import android.content.SharedPreferences.Editor
import android.content.pm.ActivityInfo
import android.hardware.Camera


class MainActivity : AppCompatActivity(), Session.Callback, SurfaceHolder.Callback {

    lateinit var session: Session

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO)
                .withListener(object: MultiplePermissionsListener{
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        initializeServer()
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {

                    }

                }).check()

    }

    override fun onBitrateUpdate(bitrate: Long) {

    }

    override fun onSessionError(reason: Int, streamType: Int, e: Exception?) {
        Logger.d("session error")
    }

    override fun onPreviewStarted() {
        Logger.d("preview started")
    }

    override fun onSessionConfigured() {
        Logger.d("Surface configured")
        session.start()
    }

    override fun onSessionStarted() {
        Logger.d("Surface started")
    }

    override fun onSessionStopped() {
        Logger.d("Surface stopped")
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        Logger.d("Surface destroyed")
        session.stop()
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        Logger.d("Surface created")
        session.startPreview()
    }

    private fun initializeServer() {

        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        editor.putString(RtspServer.KEY_PORT, 1234.toString())
        editor.commit()

        val camera = Camera.open()
        val params = camera.parameters
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
        camera.parameters = params

        session = SessionBuilder.getInstance()
                .setCallback(this)
                .setCamera(camera.hashCode())
                .setSurfaceView(mSurfaceView)
                .setContext(applicationContext)
                .setPreviewOrientation(90)
                .setAudioEncoder(SessionBuilder.AUDIO_AAC)
                .setAudioQuality(AudioQuality(16000, 32000))
                .setVideoEncoder(SessionBuilder.VIDEO_H264)
                .setVideoQuality(VideoQuality(1280, 720, 20, 2000000))
                .build()

        mSurfaceView.holder.addCallback(this)
        startService(Intent(this, RtspServer::class.java))

    }
}
