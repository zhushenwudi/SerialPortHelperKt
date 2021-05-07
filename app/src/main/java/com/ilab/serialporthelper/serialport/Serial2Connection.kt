package com.ilab.serialporthelper.serialport

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log

class Serial2Connection(
    private var listener: ((serial2Service: Serial2Service) -> Unit)? = {}
) : ServiceConnection {
    private lateinit var serial2Service: Serial2Service

    // 是否绑定服务
    private var isBind = false

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        Log.e(TAG, CONNECTED)
        try {
            serial2Service = (service as Serial2Service.SerialBinder).getService()
            listener?.invoke(serial2Service)
            isBind = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        Log.e(TAG, DISCONNECTED)
        listener = null
        isBind = false
    }

    fun getIsBind(): Boolean {
        return isBind
    }

    companion object {
        val TAG: String = Serial2Connection::class.java.simpleName
        const val CONNECTED = "onSerial2Connected"
        const val DISCONNECTED = "onSerial2Disconnected"
    }
}