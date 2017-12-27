package com.example.dkalita.pipedriveapi

import android.arch.core.executor.TaskExecutor
import org.powermock.api.mockito.PowerMockito
import kotlin.reflect.KClass

fun<T: Any?> whenCall(methodCall: T) = PowerMockito.`when`(methodCall)

fun<T: Any> whenNew(clazz: KClass<T>) = PowerMockito.whenNew(clazz.java)

class ImmediateExecutor : TaskExecutor() {

	override fun isMainThread() = true

	override fun executeOnDiskIO(runnable: Runnable?) {
		runnable?.run()
	}

	override fun postToMainThread(runnable: Runnable?) {
		runnable?.run()
	}
}
