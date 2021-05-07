package top.keepempty.sph.library

/**
 * 串口操作类
 */
class SerialPortJNI {
    // 保存c++类的地址
    private val nativeSerialPort: Long = 0

    init {
        System.loadLibrary("SerialPortLib")
    }

    /**
     * 打开串口并设置串口数据位，校验位, 速率，停止位
     *
     * @param path     串口地址
     * @param baudRate 波特率
     * @param dataBits 数据位
     * @param stopBits 停止位
     * @param parity   校验类型 取值N ,E, O
     */
    external fun openPort(
        path: String?, baudRate: Int, dataBits: Int,
        stopBits: Int, parity: Char
    ): Int

    /**
     * 设置是否使用原始模式(Raw Mode)方式来通讯 取值0,1,2
     *
     * @param mode 0=nothing
     * 1=Raw mode
     * 2=no raw mode
     */
    external fun setMode(mode: Int): Int

    /**
     * 读取串口数据
     *
     * @param maxSize 数据最大长度
     * @return 串口数据
     */
    external fun readPort(maxSize: Int): ByteArray?

    /**
     * 写入串口数据
     *
     * @param datas 串口数据指令
     */
    external fun writePort(datas: ByteArray?)

    /**
     * 关闭串口
     */
    external fun closePort()

    protected external fun finalize()
}