package screeps.sdk.extensions

import screeps.ai.roles.CreepRole
import screeps.ai.roles.CreepState
import screeps.ai.roles.toCreepState
import screeps.api.Creep
import screeps.api.CreepMemory
import screeps.api.Resource
import screeps.api.Source
import screeps.utils.memory.memory
import kotlin.math.abs

var CreepMemory.state: Int by memory { CreepState.GetEnergy.creepState }
var CreepMemory.role: Int by memory { CreepRole.Unassigned.role }

fun Creep.getState(): CreepState {
    return memory.state.toCreepState()
}

fun Creep.setState(state: CreepState) {
    memory.state = state.creepState
}

private var CreepMemory.targetId: String? by memory()

fun Creep.getTargetId(): String? {
    return memory.targetId
}

fun Creep.setTargetPos(targetId: String) {
    memory.targetId = targetId
}

fun Creep.calculateWeights(
    resource: Resource
): Float {
    val creep = this
    val distance = abs(creep.pos.x - resource.pos.x) + abs(creep.pos.y - resource.pos.y)
    return distance.toFloat() / resource.amount.toFloat()
}

fun Creep.calculateWeights(
    source: Source
): Float {
    val creep = this
    val distance = abs(creep.pos.x - source.pos.x) + abs(creep.pos.y - source.pos.y)
    return distance.toFloat() / source.energyCapacity
}