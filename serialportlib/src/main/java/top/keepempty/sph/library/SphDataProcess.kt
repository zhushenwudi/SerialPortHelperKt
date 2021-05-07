package top.keepempty.sph.library

import top.keepempty.sph.event.EventMutableLiveData

class SphDataProcess(
    private val maxSize: Int,
    private val mutableData: EventMutableLiveData<ByteArray>,
    private val isReceiveMaxSize: Boolean,  // 是否按最大接收长度进行返回
) {
    /**
     * 记录读取数据的大小
     */
    private var mSerialBufferSize = 0

    /**
     * 串口接收数据保存数组
     */
    private val mSerialBuffer: ByteArray by lazy { ByteArray(maxSize) }

    /**
     * 根据配置对串口数据进行处理
     *
     * @param bytes  当前读取的数据字节数组
     * @param revLen 当前读取的数据长度
     */
    fun processingRecData(bytes: ByteArray, revLen: Int) {
        // 按设置的最大返回长度进行返回
        if (isReceiveMaxSize) {
            reCreateData(bytes, revLen)
            return
        }
        resultCallback(bytes)
    }

    /**
     * 处理数据读取反馈，对读取的数据按maxSize进行处理
     * 如果数据一次没有读取完整，通过数组拷贝将数据补全完整
     *
     * @param bytes  当前读取的数据字节数组
     * @param revLen 当前读取的数据长度
     */
    private fun reCreateData(bytes: ByteArray, revLen: Int) {
        if (hasReadDone(revLen) || mSerialBufferSize + revLen > maxSize) {
            // 截取剩余需要读取的长度
            val copyLength = maxSize - mSerialBufferSize
            arrayCopy(bytes, 0, copyLength)
            mSerialBufferSize += copyLength
            checkReCreate(mSerialBuffer)

            // 对反馈数据剩余的数据进行重新拷贝
            val lastLength = revLen - copyLength
            arrayCopy(bytes, copyLength, lastLength)
            mSerialBufferSize = lastLength
            checkReCreate(mSerialBuffer)
        } else {
            // 没有读取完整的情况，继续进行读取
            arrayCopy(bytes, 0, revLen)
            mSerialBufferSize += revLen
            checkReCreate(mSerialBuffer)
        }
    }

    /**
     * 获取最大读取长度
     *
     * @return
     */
    fun getMaxSize(): Int {
        return maxSize
    }

    /**
     * 判断当前数据是否读取完整
     *
     * @param revLen 读取数据的长度
     * @return
     */
    private fun hasReadDone(revLen: Int): Boolean {
        return revLen >= maxSize && mSerialBufferSize != maxSize
    }

    /**
     * 判断是否完成重组
     *
     * @param resultBytes
     */
    private fun checkReCreate(resultBytes: ByteArray) {
        if (mSerialBufferSize == maxSize) {
            resultCallback(resultBytes)
        }
    }

    /**
     * 判断数据是否读取完成，通过回调输出读取数据
     */
    private fun resultCallback(resultBytes: ByteArray) {
        sendMessage(resultBytes)
        reInit()
    }

    /**
     * 重置数据
     */
    private fun reInit() {
        mSerialBufferSize = 0
    }

    /**
     * 通过数组拷贝，对数据进行重组
     *
     * @param bytes  当前读取的数据字节数组
     * @param srcPos 需要拷贝的源数据位置
     * @param length 拷贝的数据长度
     */
    private fun arrayCopy(bytes: ByteArray, srcPos: Int, length: Int) {
        System.arraycopy(bytes, srcPos, mSerialBuffer, mSerialBufferSize, length)
    }

    fun sendMessage(resultBytes: ByteArray) {
        mutableData.postValue(resultBytes)
    }
}