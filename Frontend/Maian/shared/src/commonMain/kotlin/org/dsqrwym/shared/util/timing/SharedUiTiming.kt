package org.dsqrwym.shared.util.timing

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

object SharedUiTiming {
    val searchDebounce: Duration = 600.milliseconds
    val localSearchDelay: Duration = 500.milliseconds
    val formStateResetDelay: Duration = 500.milliseconds
    val loginStateHoldDelay: Duration = 300.milliseconds
    val availabilityCheckDelay: Duration = 500.milliseconds
}
