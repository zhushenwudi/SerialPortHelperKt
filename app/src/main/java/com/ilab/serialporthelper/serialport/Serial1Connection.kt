package com.ilab.serialporthelper.serialport

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log

class Serial1Connection(
    private var listener: ((serial1Service: Serial1Service) -> Unit)? = {}
) : ServiceConnection {
    private lateinit var serial1Service: Serial1Service

    // 是否绑定服务
    private var isBind = false

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        Log.e(TAG, CONNECTED)
        try {
            serial1Service = (service as Serial1Service.SerialBinder).getService()
            listener?.invoke(serial1Service)
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
        val TAG: String = Serial1Connection::class.java.simpleName
        const val CONNECTED = "onSerial1Connected"
        const val DISCONNECTED = "onSerial1Disconnected"
    }
}