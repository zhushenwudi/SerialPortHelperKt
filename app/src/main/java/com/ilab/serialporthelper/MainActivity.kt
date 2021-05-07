package com.ilab.serialporthelper

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ilab.serialporthelper.ext.clickNoRepeat
import com.ilab.serialporthelper.serialport.Serial1Connection
import com.ilab.serialporthelper.serialport.Serial1Service
import com.ilab.serialporthelper.serialport.Serial2Connection
import com.ilab.serialporthelper.serialport.Serial2Service
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var serial1Connection: Serial1Connection
    private lateinit var serial1Service: Serial1Service
    private lateinit var serial2Connection: Serial2Connection
    private lateinit var serial2Service: Serial2Service

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initSerial1()
        initSerial2()

        btnStart1Once.clickNoRepeat {
            if (this::serial1Service.isInitialized) {
                serial1Service.startOnce()
            }
        }

        btnStart1ManyTimes.clickNoRepeat {
            if (this::serial1Service.isInitialized) {
                tie1.text?.let { text ->
                    try {
                        serial1Service.startManyTimes(text.toString().trim().toInt())
                    } catch (e: Exception) {
                        serial1Service.startManyTimes()
                    }
                }
            }
        }

        btnStart2Once.clickNoRepeat {
            if (this::serial2Service.isInitialized) {
                serial2Service.startOnce()
            }
        }

        btnStart2ManyTimes.clickNoRepeat {
            if (this::serial2Service.isInitialized) {
                tie2.text?.let { text ->
                    try {
                        serial2Service.startManyTimes(text.toString().trim().toInt())
                    } catch (e: Exception) {
                        serial2Service.startManyTimes()
                    }
                }
            }
        }
    }

    /**
     * 初始化串口服务1
     */
    private fun initSerial1() {
        serial1Connection = Serial1Connection {
            serial1Service = it
        }
        bindService(
            Intent(this, Serial1Service::class.java),
            serial1Connection,
            Context.BIND_AUTO_CREATE
        )
    }

    /**
     * 初始化串口服务2
     */
    private fun initSerial2() {
        serial2Connection = Serial2Connection {
            serial2Service = it
        }
        bindService(
            Intent(this, Serial2Service::class.java),
            serial2Connection,
            Context.BIND_AUTO_CREATE
        )
    }

    override fun onStop() {
        // 停止串口服务1
        try {
            if (this::serial1Connection.isInitialized && serial1Connection.getIsBind()) {
                unbindService(serial1Connection)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 停止串口服务2
        try {
            if (this::serial2Connection.isInitialized && serial2Connection.getIsBind()) {
                unbindService(serial2Connection)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onStop()
    }
}