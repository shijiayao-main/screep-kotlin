package screeps.api.roles

enum class CreepState(val creepState: Int) {
    GetEnergy(0),
    DoWork(1);
}

fun Int.toCreepState(): CreepState {
    return when (this) {
        CreepState.DoWork.creepState -> CreepState.DoWork
        CreepState.GetEnergy.creepState -> CreepState.GetEnergy
        else -> CreepState.DoWork
    }
}
