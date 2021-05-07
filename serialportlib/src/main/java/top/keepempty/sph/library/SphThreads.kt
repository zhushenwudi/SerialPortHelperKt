package top.keepempty.sph.library

import kotlinx.coroutines.*

class SphThreads(
    serialPort: SerialPortJNI,
    processingData: SphDataProcess
) {
    private var scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch(Dispatchers.IO) {
            while (scope.isActive) {
                // 读取数据
                val bytes = serialPort.readPort(processingData.getMaxSize())
                if (bytes != null && bytes.isNotEmpty()) {
                    processingData.processingRecData(bytes, bytes.size)
                }
            }
        }
    }

    fun stop() {
        if (scope.isActive) {
            scope.cancel()
        }
    }
}