package com.aryanspatel.grofunds.util

import com.aryanspatel.grofunds.core.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.TestDispatcher

class TestDispatcherProvider(private val d: TestDispatcher) : DispatcherProvider {
    override val iO = d
    override val main = d
    override val default = d
    override val unconfined: CoroutineDispatcher = d
}
