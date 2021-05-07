package top.keepempty.sph.event

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData

/**
 * LiveData 相关的工具类，简化 LiveData 操作
 *
 * @author funnywolf
 * @since 2019-04-22
 */
object LiveDataUtils {
    private var sMainHandler: Handler? = null

    /**
     * 用 setValue 更新 MutableLiveData 的数据，如果在子线程，就切换到主线程
     */
    @JvmStatic
    fun <T> setValue(mld: MutableLiveData<T>?, d: T) {
        if (mld == null) {
            return
        }
        if (Thread.currentThread() === Looper.getMainLooper().thread) {
            mld.setValue(d)
        } else {
            postSetValue(mld, d)
        }
    }

    /**
     * 向主线程的 handler 抛 SetValueRunnable
     */
    fun <T> postSetValue(mld: MutableLiveData<T>, d: T) {
        if (sMainHandler == null) {
            sMainHandler = Handler(Looper.getMainLooper())
        }
        sMainHandler!!.post(SetValueRunnable.create(mld, d))
    }

    private class SetValueRunnable<T> private constructor(
        private val liveData: MutableLiveData<T>,
        private val data: T
    ) : Runnable {
        override fun run() {
            liveData.value = data
        }

        companion object {
            fun <T> create(liveData: MutableLiveData<T>, data: T): SetValueRunnable<T> {
                return SetValueRunnable(liveData, data)
            }
        }
    }
}