package screeps.sdk.extensions

import screeps.ai.roles.CreepRole
import screeps.ai.roles.CreepState
import screeps.ai.roles.toCreepState
import screeps.api.Creep
import screeps.api.CreepMemory
import screeps.api.Resource
import screeps.api.RoomPosition
import screeps.api.Source
import screeps.api.structures.Structure
import screeps.api.structures.StructureContainer
import screeps.api.structures.StructureExtension
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
    return calculateWeights(
        creep = this,
        targetPos = resource.pos,
        targetNum = resource.amount
    )
}

fun Creep.calculateWeights(
    source: Source
): Float {
    return calculateWeights(
        creep = this,
        targetPos = source.pos,
        targetNum = source.energyCapacity
    )
}

fun Creep.calculateWeights(
    structureExtension: StructureExtension
): Float {
    return calculateWeights(
        creep = this,
        targetPos = structureExtension.pos,
        targetNum = structureExtension.store.getUsedCapacity()
    )
}

fun Creep.calculateWeights(
    structureContainer: StructureContainer
): Float {
    return calculateWeights(
        creep = this,
        targetPos = structureContainer.pos,
        targetNum = structureContainer.store.getUsedCapacity()
    )
}

fun Creep.calculateDistance(
    structure: Structure
): Float {
    return calculateWeights(
        creep = this,
        targetPos = structure.pos,
        targetNum = 1
    )
}

private fun calculateWeights(
    creep: Creep,
    targetPos: RoomPosition,
    targetNum: Int,
): Float {
    val distance = abs(creep.pos.x - targetPos.x) + abs(creep.pos.y - targetPos.y)
    return distance.toFloat() / targetNum
}
