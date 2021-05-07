package com.ilab.serialporthelper.serialport

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import top.keepempty.sph.event.EventMutableLiveData
import top.keepempty.sph.library.SerialPortHelper
import java.util.*

class Serial2Service : LifecycleService() {
    // 服务生命周期监听器
    private var serviceObserver: SerialServiceObserver

    // 串口帮助类
    private lateinit var serialPortHelper: SerialPortHelper

    // 接收串口消息的载体
    private val mutableByteArray = EventMutableLiveData<ByteArray>()

    // 发送串口命令的协程
    private lateinit var sendScope: CoroutineScope

    // 处理串口数据的协程
    private lateinit var getScope: CoroutineScope

    // 控制数据发送的协程锁
    private val mutex = Mutex()

    // 监听生命周期 ==> 关闭设备并移除监听
    internal inner class SerialServiceObserver : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun release() {
            stop()
            lifecycle.removeObserver(serviceObserver)
        }
    }

    init {
        // 启动监听
        serviceObserver = SerialServiceObserver()
        lifecycle.addObserver(serviceObserver)

        // 串口回调
        mutableByteArray.observe(this) {
            // 切换子线程做耗时操作
            if (this::getScope.isInitialized && getScope.isActive) {
                getScope.cancel()
            }
            getScope = CoroutineScope(Dispatchers.IO)
            getScope.launch {
                val rawData = Arrays.toString(it)
                Log.d(TAG, "原始串口结果: $rawData")
                val result = parseData(rawData)
                Log.e(TAG, "转换串口结果: $result")
                if (mutex.isLocked) {
                    mutex.unlock()
                }
            }
        }
    }

    /**
     * 发送一次
     */
    fun startOnce() {
        // 开始前先停止，总有人忘了释放资源
        stop()

        // 初始化
        serialPortHelper = SerialPortHelper(
            RECEIVE_MSG_MAX_LENGTH,
            mutableByteArray,
            MySerialPortConfig.getSerial2Config()
        )

        // 在设备处于关闭状态且无法开启时
        if (!serialPortHelper.openDevice()) {
            Log.e(TAG, SERIAL_CANT_OPEN)
        }

        // 切换子线程做耗时操作
        if (this::sendScope.isInitialized && sendScope.isActive) {
            sendScope.cancel()
        }
        sendScope = CoroutineScope(Dispatchers.IO)
        sendScope.launch {
            serialPortHelper.sendHex(SEND_STRING)
        }
        Log.e(TAG, START_LOG)
    }

    /**
     * 发送多次
     * @param times 循环次数
     */
    fun startManyTimes(times: Int = 5) {
        var number = times
        // 开始前先停止，总有人忘了释放资源
        stop()

        // 初始化
        serialPortHelper = SerialPortHelper(
            RECEIVE_MSG_MAX_LENGTH,
            mutableByteArray,
            MySerialPortConfig.getSerial2Config()
        )

        // 在设备处于关闭状态且无法开启时
        if (!serialPortHelper.isOpenDevice() && !serialPortHelper.openDevice()) {
            Log.e(TAG, SERIAL_CANT_OPEN)
        }

        // 切换子线程做耗时操作
        if (this::sendScope.isInitialized && sendScope.isActive) {
            sendScope.cancel()
        }
        sendScope = CoroutineScope(Dispatchers.IO)
        sendScope.launch {
            // 循环times次
            while (number > 0) {
                // 设置超时机制，超时后重新发送当前指令
                val result = withTimeoutOrNull(TIMEOUT) {
                    // 判断上一次指令回调是否处理结束
                    while (mutex.isLocked) {
                        delay(DELAY)
                    }
                    // 加锁
                    mutex.lock()
                    // 发送串口指令
                    serialPortHelper.sendHex(SEND_STRING)
                    // 给个喘息的机会，可以去掉
                    delay(DELAY)
                    "success"
                }
                // 超时返回null，不超时返回success
                result?.apply { number-- }
            }
        }
        Log.e(TAG, START_LOG)
    }

    // 解析字符串
    private fun parseData(str: String): String {
        val sb = StringBuilder()
        str.replace("[" to "", "]" to "", " " to "").split(",").toTypedArray().forEach {
            sb.append(it.toInt().toChar())
        }
        return sb.toString().trim()
    }

    /**
     * 释放资源
     */
    private fun stop() {
        try {
            if (this::sendScope.isInitialized && sendScope.isActive) {
                sendScope.cancel()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            if (this::getScope.isInitialized && getScope.isActive) {
                getScope.cancel()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            if (this::serialPortHelper.isInitialized && serialPortHelper.isOpenDevice()) {
                serialPortHelper.closeDevice()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (mutex.isLocked) {
            mutex.unlock()
        }
        Log.e(TAG, STOP_LOG)
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return SerialBinder(this)
    }

    internal class SerialBinder(private val serial1Service: Serial2Service) : Binder() {
        fun getService(): Serial2Service {
            return serial1Service
        }
    }

    companion object {
        const val TAG: String = "SerialPortHelperKt"
        const val SERIAL_CANT_OPEN = "SERIAL2_CANT_OPEN"
        const val START_LOG = "serial2 start"
        const val STOP_LOG = "serial2 stop"
        const val SEND_STRING = "1B70"
        const val RECEIVE_MSG_MAX_LENGTH = 16
        const val DELAY = 500L
        const val TIMEOUT = 10000L
    }

    private fun String.replace(vararg pairs: Pair<String, String>): String =
        pairs.fold(this) { acc, (old, new) -> acc.replace(old, new, ignoreCase = true) }
}