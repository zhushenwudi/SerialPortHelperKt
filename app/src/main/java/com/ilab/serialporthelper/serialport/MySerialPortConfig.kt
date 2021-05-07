package com.ilab.serialporthelper.serialport

import top.keepempty.sph.library.SerialPortConfig

object MySerialPortConfig : SerialPortConfig() {

    fun getSerial1Config(): MySerialPortConfig {
        mode = 0
        path = "dev/ttyS1"
        baudRate = 38400
        dataBits = 8
        parity = 'e'
        stopBits = 1
        return this
    }

    fun getSerial2Config(): MySerialPortConfig {
        mode = 0
        path = "dev/ttyS3"
        baudRate = 9600
        dataBits = 8
        parity = 'n'
        stopBits = 1
        return this
    }
}