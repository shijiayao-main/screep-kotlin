package screeps.ai.roles

import screeps.ai.entity.RoomInfo
import screeps.api.Creep
import screeps.api.CreepMemory
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.FIND_SOURCES
import screeps.api.Game
import screeps.api.OK
import screeps.api.RoomMemory
import screeps.api.Source
import screeps.sdk.ScreepsLog
import screeps.utils.memory.memory

var CreepMemory.energySource: String? by memory()
var RoomMemory.energySourceAssignments: Array<String?> by memory { arrayOf(null, null) }

class HarvesterRole(
    creepList: List<Creep>,
    roomInfo: RoomInfo
) : AbstractRole(
    creepList = creepList,
    roomInfo = roomInfo
) {
    companion object {
        private const val TAG = "HarvesterRole"
    }

    private fun setAssignedSource(
        creep: Creep,
        source: Source
    ) {
        creep.memory.energySource = source.id
    }

    private fun getAssignedSource(creep: Creep): Source? {
        return Game.getObjectById(creep.memory.energySource)
    }

    override fun startWork() {

    }

    fun run(creep: Creep) {
        val energySource = findEnergySource(creep = creep)
        if (energySource == null) {
            ScreepsLog.d(TAG, "No sources found to gather from!")
            return
        }

        val status = creep.harvest(energySource)

        if (status == ERR_NOT_IN_RANGE) {
            creep.moveTo(energySource.pos.x, energySource.pos.y)
        } else if (status != OK) {
            ScreepsLog.d(TAG, "Gather failed with code $status")
        }
    }

    private fun findEnergySource(creep: Creep): Source? {
        val assignedSource = getAssignedSource(creep)
        if (assignedSource != null) {
            return assignedSource
        }

        val energySources = creep.room.find(FIND_SOURCES).sortedBy { it.id }

        if (energySources.isEmpty()) {
            ScreepsLog.d(TAG, "There are no sources in the room!")
            return null
        }

        for (energySource in energySources.withIndex()) {
            val otherCreep = Game.getObjectById<Creep>(creep.room.memory.energySourceAssignments[energySource.index])
            if (otherCreep == null) {
                ScreepsLog.d(TAG, "Dead creep found, re-assigning energy source")
                setAssignedSource(creep = creep, source = energySource.value)
                creep.room.memory.energySourceAssignments[energySource.index] = creep.id
                return assignedSource
            } else {
                ScreepsLog.d(TAG, "Energy source already taken, checking other source(s)")
            }
        }

        creep.say("All sources in room are taken")
        return null
    }

}
