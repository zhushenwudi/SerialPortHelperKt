# SerialPortHelperKt

时隔两年，基于Kotlin语言的Android串口通信实例如期而至

#### 项目说明
1.本程序基于不支持创建多实例的原项目 https://github.com/freyskill/SerialPortHelper 根据issue #2中源码修改的 dearchun大神 所提供的lib包制作

2.由于原SerialPortHelper Java程序在数据量巨大时存在anr情况，并且没有通信超时机制、数据控流等串口通信开发所必要的很多情况，在Kotlin语言以及由谷歌带来的Jetpack架构的强力支持下，根据项目需要，制作了一款具备自动感知生命周期、适合单一Activity多个Fragment路由情景的串口库。

3.本程序模拟了单次通信，以及自定义次数通信。使用Service方式实现数据交互，通过bindService方式将Activity与Service绑定，且支持自动感知生命周期等操作，对开发人员带来了极大的遍历，真正做到专注于业务逻辑的编写！！

#### 接入方式
1.下载ZIP
git clone https://github.com/zhushenwudi/SerialPortHelperKt.git

2.依赖moudule包名：serialportlib

#### 示例
1.在Project build.gradle中添加

```groovy
buildscript {
   ...
   dependencies {
      classpath 'com.github.dcendents:android-maven-gradle-plugin:2.1'
   }
}
```

2.配置串口通信协议

	示例采用单例模式，文件位于 com.ilab.serialporthelper.serialport.MySerialPortConfig

```kotlin
singleton.mode = 0;              // 是否使用原始模式(Raw Mode)方式来通讯
singleton.path = "dev/ttyS3";    // 串口地址 [ttyS0 ~ ttyS6, ttyUSB0 ~ ttyUSB4]
singleton.baudRate = 9600;       // 波特率
singleton.dataBits = 8;          // 数据位 [7, 8]
singleton.parity = 'n';          // 检验类型 [N(无校验) ,E(偶校验), O(奇校验)] (大小写随意)
singleton.stopBits = 1;          // 停止位 [1, 2]
```

3.SerialService 串口通信服务，下文均采用 Serial1Service 举例

```kotlin
详看com.ilab.serialporthelper.serialport.Serial1Service

在AndroidManifest.xml 中加入 <service android:name=".serialport.Serial1Service" />

在app包中创建 Serial1Service.kt

// 初始化
serialPortHelper = SerialPortHelper(
            20, // 每次接受数据的最大长度
            mutableByteArray, // 接收消息回调的载体
            MySerialPortConfig.getSerial1Config() // 通信配置
        )

// 打开串口，返回为true表示打开成功
serialPortHelper.openDevice();

// 串口回调
mutableByteArray.observe(this) {
    // 切换子线程做耗时操作
    if (this::getScope.isInitialized && getScope.isActive) {
        getScope.cancel()
    }
    getScope = CoroutineScope(Dispatchers.IO)
    getScope.launch {
        val result = encodeHexString(it)

		   // 校验CRC16
		   // CRC16.checkCRC(result)
		   // 释放锁，允许下一次发送命令
        if (mutex.isLocked) {
            mutex.unlock()
        }
    }
}

// 发送串口数据
serialPortHelper.sendHex("要发送的16进制字符串");

// 判断串口是否打开
serialPortHelper.isOpenDevice();
```

4.Activity调用

```kotlin
详看com.ilab.serialporthelper.MainActivity

// 绑定服务
serial1Connection = Serial1Connection {
            serial1Service = it
        }
bindService(
            Intent(this, Serial1Service::class.java),
            serial1Connection,
            Context.BIND_AUTO_CREATE
        )
        
// 发送一次消息
serial1Service.startOnce()

// 发送多次消息, num 默认值为 5
serial1Service.startManyTimes(num)

// 解绑服务
unbindService(serial1Connection)
```

5.Log截图

![Image text](https://gitee.com/zhushenwudi/serial-port-helper-kt/raw/master/example.png)