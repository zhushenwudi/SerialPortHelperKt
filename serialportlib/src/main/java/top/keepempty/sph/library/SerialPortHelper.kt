package top.keepempty.sph.library

import android.util.Log
import top.keepempty.sph.event.EventMutableLiveData

class SerialPortHelper(
    private val maxSize: Int = 0,       // 最大接收数据的长度
    private val mutableData: EventMutableLiveData<ByteArray>,
    private val serialPortConfig: SerialPortConfig = SerialPortConfig(),
    private val isReceiveMaxSize: Boolean = false  // 是否需要返回最大数据接收长度
) {
    private val serialPort: SerialPortJNI by lazy { SerialPortJNI() }
    private var mIsOpen = false
    private lateinit var sphThreads: SphThreads

    // 数据处理
    private lateinit var processingData: SphDataProcess

    fun openDevice(): Boolean {
        if (serialPortConfig.path == null) {
            throw IllegalArgumentException("You not have setting the device path !")
        }
        val isOpenSuccess = serialPort.openPort(
            serialPortConfig.path!!,
            serialPortConfig.baudRate,
            serialPortConfig.dataBits,
            serialPortConfig.stopBits,
            serialPortConfig.parity
        )

        // 是否设置原始模式(Raw Mode)方式来通讯
        if (serialPortConfig.mode != 0) {
            serialPort.setMode(serialPortConfig.mode)
        }

        // 打开串口成功
        // 打开串口成功
        if (isOpenSuccess == 1) {
            mIsOpen = true
            // 创建数据处理
            processingData = SphDataProcess(maxSize, mutableData, isReceiveMaxSize)
            // 开启读写线程
            sphThreads = SphThreads(serialPort, processingData)
        } else {
            mIsOpen = false
        }
        return mIsOpen
    }

    /**
     * 发送Hex数据
     *
     * @param hexCommands
     */
    fun sendHex(hexCommands: String) {
        if (!isOpenDevice()) {
            Log.d(TAG, "You not open device !!!")
            return
        }
        serialPort.writePort(DataConversion.decodeHexString(hexCommands))
    }

    /**
     * 关闭串口
     */
    fun closeDevice() {
        serialPort.closePort()
        if (this::sphThreads.isInitialized) {
            sphThreads.stop()
        }
    }

    /**
     * 判断串口是否打开
     */
    fun isOpenDevice(): Boolean {
        return mIsOpen
    }

    companion object {
        val TAG: String = SerialPortHelper::class.java.simpleName
    }
}