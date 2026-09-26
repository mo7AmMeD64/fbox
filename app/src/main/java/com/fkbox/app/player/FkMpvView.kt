package com.fkbox.app.player

import android.content.Context
import android.util.AttributeSet
import `is`.xyz.mpv.BaseMPVView
import `is`.xyz.mpv.MPVLib

/** SurfaceView that owns libmpv. Inflated from res/layout/view_mpv.xml so the AttributeSet is provided. */
class FkMpvView(context: Context, attrs: AttributeSet) : BaseMPVView(context, attrs) {
    var hwdec: Boolean = true
    var strictTls: Boolean = true

    override fun initOptions(vo: String) {
        setVo(vo)
        MPVLib.setOptionString("profile", "fast")
        MPVLib.setOptionString("gpu-context", "android")
        MPVLib.setOptionString("opengl-es", "yes")
        MPVLib.setOptionString("hwdec", if (hwdec) "auto" else "no")
        MPVLib.setOptionString("hwdec-codecs", "h264,hevc,mpeg4,mpeg2video,vp8,vp9,av1")
        MPVLib.setOptionString("ao", "audiotrack,opensles")
        MPVLib.setOptionString("vd-lavc-film-grain", "cpu")
        MPVLib.setOptionString("demuxer-max-bytes", (64 * 1024 * 1024).toString())
        MPVLib.setOptionString("demuxer-max-back-bytes", (32 * 1024 * 1024).toString())
        MPVLib.setOptionString("network-timeout", "30")
        MPVLib.setOptionString("sub-auto", "no")
        MPVLib.setOptionString("tls-verify", if (strictTls) "yes" else "no")
        if (strictTls) MPVLib.setOptionString("tls-ca-file", "${context.filesDir.path}/cacert.pem")
    }

    override fun postInitOptions() {
        MPVLib.setOptionString("save-position-on-quit", "no")
    }

    override fun observeProperties() {
        val f = MPVLib.mpvFormat.MPV_FORMAT_DOUBLE
        MPVLib.observeProperty("time-pos", f)
        MPVLib.observeProperty("duration/full", f)
        MPVLib.observeProperty("speed", f)
        MPVLib.observeProperty("pause", MPVLib.mpvFormat.MPV_FORMAT_FLAG)
        MPVLib.observeProperty("paused-for-cache", MPVLib.mpvFormat.MPV_FORMAT_FLAG)
        MPVLib.observeProperty("eof-reached", MPVLib.mpvFormat.MPV_FORMAT_FLAG)
        MPVLib.observeProperty("track-list", MPVLib.mpvFormat.MPV_FORMAT_NONE)
    }
}
