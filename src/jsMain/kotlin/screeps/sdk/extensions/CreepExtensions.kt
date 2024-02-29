package screeps.sdk.extensions

import screeps.ai.roles.CreepRole
import screeps.ai.roles.CreepState
import screeps.ai.roles.toCreepState
import screeps.api.Creep
import screeps.api.CreepMemory
import screeps.utils.memory.memory

var CreepMemory.state: Int by memory { CreepState.GetEnergy.creepState }
var CreepMemory.role: Int by memory { CreepRole.Unassigned.role }

fun Creep.getState(): CreepState {
    return memory.state.toCreepState()
}

fun Creep.setState(state: CreepState) {
    memory.state = state.creepState
}

