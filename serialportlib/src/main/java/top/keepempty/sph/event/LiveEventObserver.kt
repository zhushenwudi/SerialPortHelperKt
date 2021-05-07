package top.keepempty.sph.event

import androidx.lifecycle.*
import androidx.lifecycle.Observer
import java.util.*

/**
 * LiveData 用作事件传递时的观察者
 * 保证所有事件不丢失，保存非**状态的事件，并能够在**状态回调，且没有内存泄漏
 *
 *
 * @author funnywolf
 * @since 2019-05-18
 */
class LiveEventObserver<T>(
    private var mLiveData: LiveData<T>?,
    private var mOwner: LifecycleOwner?,
    private var mObserver: Observer<in T>?
) : LifecycleObserver, Observer<T?> {
    private val mPendingData: MutableList<T?> = ArrayList()

    /**
     * 在生命周期结束前的任何时候都可能会调用
     */
    override fun onChanged(t: T?) {
        if (isActive) {
            // 如果是**状态，就直接更新
            mObserver!!.onChanged(t)
        } else {
            // 非**状态先把数据存起来
            mPendingData.add(t)
        }
    }

    /**
     * @return 是否是**状态，即 onStart 之后到 onPause 之前
     */
    private val isActive: Boolean
        private get() = mOwner!!.lifecycle.currentState
            .isAtLeast(Lifecycle.State.STARTED)

    /**
     * onStart 之后就是**状态了，如果之前存的有数据，就发送出去
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    private fun onEvent(owner: LifecycleOwner, event: Lifecycle.Event) {
        if (owner !== mOwner) {
            return
        }
        if (event == Lifecycle.Event.ON_START || event == Lifecycle.Event.ON_RESUME) {
            for (i in mPendingData.indices) {
                mObserver!!.onChanged(mPendingData[i])
            }
            mPendingData.clear()
        }
    }

    /**
     * onDestroy 时解除各方的观察和绑定，并清空数据
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onDestroy() {
        mLiveData!!.removeObserver(this)
        mLiveData = null
        mOwner!!.lifecycle.removeObserver(this)
        mOwner = null
        mPendingData.clear()
        mObserver = null
    }

    companion object {
        @JvmStatic
        fun <T> bind(
            liveData: LiveData<T>,
            owner: LifecycleOwner,
            observer: Observer<in T>
        ) {
            if (owner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
                return
            }
            LiveEventObserver(liveData, owner, observer)
        }
    }

    init {
        mOwner!!.lifecycle.addObserver(this)
        mLiveData!!.observeForever(this)
    }
}