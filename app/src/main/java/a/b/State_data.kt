package a.b

import java.lang.Thread.State
import java.util.Optional

data class State_data(
    val speed : Optional<Double>,
    val rpm : Optional<Double>,
    val engine_load : Optional<Double>, // %
    val throttle : Optional<Double>, // %
    val heart_beat: Optional<Int>
)

fun empty_state_data() : State_data {
    return State_data(
        Optional.ofNullable(null),
        Optional.ofNullable(null),
        Optional.ofNullable(null),
        Optional.ofNullable(null),
        Optional.ofNullable(null),
    )
}
