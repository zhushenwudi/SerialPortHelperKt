package top.keepempty.sph.library

import java.util.*

/**
 * CRC16校验码计算
 *
 * (1)．预置1个16位的寄存器为十六进制FFFF（即全为1），称此寄存器为CRC寄存器；
 * (2)．把第一个8位二进制数据（既通讯信息帧的第一个字节）与16位的CRC寄存器的低
 * 8位相异或，把结果放于CRC寄存器；
 * (3)．把CRC寄存器的内容右移一位（朝低位）用0填补最高位，并检查右移后的移出位；
 * (4)．如果移出位为0：重复第3步（再次右移一位）；如果移出位为1：CRC寄存器与多项式A001（1010 0000 0000 0001）进行异或；
 * (5)．重复步骤3和4，直到右移8次，这样整个8位数据全部进行了处理；
 * (6)．重复步骤2到步骤5，进行通讯信息帧下一个字节的处理；
 * (7)．将该通讯信息帧所有字节按上述步骤计算完成后，得到的16位CRC寄存器的高、低
 * 字节进行交换；
 * (8)．最后得到的CRC寄存器内容即为CRC16码。(注意得到的CRC码即为低前高后顺序)
 */
object CRC16 {
    // 初始值
    private const val CRC_INIT = 0x0000ffff

    // 多项式校验值
    private const val POLYNOMIAL = 0x00008408

    /**
     * 计算CRC16校验码
     *
     * @param data 需要校验的字符串
     * @return 校验码
     */
    fun getCRC(rawData: String): String {
        val data = rawData.replace(" ", "")
        val len = data.length
        if (len % 2 != 0) {
            return "0000"
        }
        val num = len / 2
        val para = ByteArray(num)
        for (i in 0 until num) {
            val value = Integer.valueOf(data.substring(i * 2, 2 * (i + 1)), 16)
            para[i] = value.toByte()
        }
        return getCRC(para)
    }

    /**
     * 计算CRC16校验码
     *
     * @param bytes 字节数组
     * @return [String] 校验码
     * @since 1.0
     */
    fun getCRC(bytes: ByteArray): String {
        var crc = CRC_INIT
        var j: Int
        var i = 0
        while (i < bytes.size) {
            crc = crc xor (bytes[i].toInt() and 0x000000ff)
            j = 0
            while (j < 8) {
                if (crc and 0x00000001 != 0) {
                    crc = crc shr 1
                    crc = crc xor POLYNOMIAL
                } else {
                    crc = crc shr 1
                }
                j++
            }
            i++
        }
        //结果转换为16进制
        var result = Integer.toHexString(crc).toUpperCase(Locale.ROOT)
        if (result.length != 4) {
            val sb = StringBuffer("0000")
            result = sb.replace(4 - result.length, 4, result).toString()
        }
        //交换高低位
        return result.substring(2, 4) + result.substring(0, 2)
    }

    /**
     * 检验CRC
     * @param text 待检验字符串
     */
    fun checkCRC(text: String): Boolean {
        return CRC16.getCRC(text.dropLast(4)) == text.takeLast(4)
    }
}