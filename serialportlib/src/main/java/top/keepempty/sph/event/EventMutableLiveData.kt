package top.keepempty.sph.event

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import top.keepempty.sph.event.LiveDataUtils.setValue
import top.keepempty.sph.event.LiveEventObserver.Companion.bind

/**
 * 用作事件总线的 [MutableLiveData]
 *
 */
class EventMutableLiveData<T> : MutableLiveData<T>() {
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        bind(this, owner, observer)
    }

    override fun postValue(value: T) {
        setValue(this, value)
    }
}