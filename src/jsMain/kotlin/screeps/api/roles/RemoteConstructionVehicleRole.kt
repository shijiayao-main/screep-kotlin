package screeps.api.roles

import screeps.api.ConstructionSite
import screeps.api.Creep
import screeps.api.ERR_FULL
import screeps.api.ERR_NOT_ENOUGH_ENERGY
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.FIND_CONSTRUCTION_SITES
import screeps.api.FIND_MY_SPAWNS
import screeps.api.FIND_SOURCES
import screeps.api.Flag
import screeps.api.FlagMemory
import screeps.api.Game
import screeps.api.OK
import screeps.api.RESOURCE_ENERGY
import screeps.api.compareTo
import screeps.api.entity.RoomInfo
import screeps.api.get
import screeps.sdk.ScreepsLog
import screeps.sdk.extensions.getState
import screeps.sdk.extensions.setState
import screeps.sdk.utils.getPathToTarget
import screeps.utils.memory.memory
import kotlin.math.abs

var FlagMemory.complete: Boolean by memory { false }

class RemoteConstructionVehicleRole(
    creepList: List<Creep>,
    roomInfo: RoomInfo,
) : AbstractRole(
    creepList = creepList,
    roomInfo = roomInfo
) {

    companion object {
        private const val TAG = "RemoteConstructionVehicleRole"
    }

    private val targetFlag: Flag? = Game.flags["NextRoom"]

    override fun startWork() {
    }

    fun run(creep: Creep) {
        if (targetFlag == null) {
            ScreepsLog.d(TAG, "No target to work with!")
        } else {
            if (targetFlag.room?.find(FIND_MY_SPAWNS)?.firstOrNull() != null) {
                ScreepsLog.d(TAG, "Spawn construction completed!")
                targetFlag.memory.complete = true
            }
            // Move to target room if not in room
            if (creep.room != targetFlag.room) {
                creep.move(creep.pos.getDirectionTo(getPathToTarget(creep.pos, targetFlag.pos)[0]))
                return
            }
        }
        val state = creep.getState()

        if (state == CreepState.GetEnergy) {
            getEnergy(creep = creep)
        } else if (state == CreepState.DoWork) {
            buildBuildings(creep = creep)
        }
    }

    private fun getEnergy(creep: Creep) {
        val storage = creep.room.storage

        if (storage == null || (storage.store.getUsedCapacity(RESOURCE_ENERGY) ?: 0) <= 0) {
            ScreepsLog.d(TAG, "No storage in room, going to gather instead")
            gatherEnergy(creep = creep)
            return
        }

        val code = creep.withdraw(storage, RESOURCE_ENERGY)
        if (code == ERR_NOT_IN_RANGE) {
            creep.moveTo(storage)
        } else if (code != OK) {
            ScreepsLog.d(TAG, "Couldn't withdraw from storage due to error: $code")
        }
    }

    private fun gatherEnergy(creep: Creep) {
        val energySource =
            creep.room.find(FIND_SOURCES).sortedBy { abs(it.pos.x - creep.pos.x) + abs(it.pos.y - creep.pos.y) }
                .firstOrNull { it.energy > 0 } ?: let {
                creep.say("No energy available!")
                return
            }

        val code = creep.harvest(energySource)

        if (code == ERR_NOT_IN_RANGE) {
            if (creep.store.getUsedCapacity(RESOURCE_ENERGY) > 50) {
                creep.setState(CreepState.DoWork)
            } else {
                creep.moveTo(energySource)
            }
        } else if (code != OK) {
            ScreepsLog.d(TAG, "Gather failed with code $code")
        }

        if ((creep.store.getFreeCapacity(RESOURCE_ENERGY) ?: 0) <= 0) {
            creep.setState(CreepState.DoWork)
        }
    }

    private fun buildBuildings(creep: Creep) {
        val constructionSite = findConstructionSite(creep)

        if (constructionSite == null) {
            creep.say("No available construction site!")
            depositEnergy(creep)
            return
        }

        val status = creep.build(constructionSite)

        if (status == ERR_NOT_IN_RANGE) {
            if (creep.room != constructionSite.room) {
                creep.move(creep.pos.getDirectionTo(getPathToTarget(creep.pos, constructionSite.pos)[0]))
            } else {
                creep.moveTo(constructionSite)
            }
        } else if (status == ERR_NOT_ENOUGH_ENERGY) {
            creep.say("Out of energy")
            creep.setState(CreepState.GetEnergy)
            return
        } else if (status != OK) {
            creep.say("Build failed with code $status")
        }

        if (creep.store.getCapacity(RESOURCE_ENERGY) <= 0) {
            creep.setState(CreepState.GetEnergy)
        }
    }

    private fun findConstructionSite(
        creep: Creep
    ): ConstructionSite? {
        return if (targetFlag != null && creep.room != targetFlag.room) {
            targetFlag.room?.find(FIND_CONSTRUCTION_SITES)?.firstOrNull()
        } else {
            creep.room.find(FIND_CONSTRUCTION_SITES)
                .minByOrNull { abs(it.pos.x - creep.pos.x) + abs(it.pos.y - creep.pos.y) }
        }
    }

    private fun depositEnergy(
        creep: Creep
    ) {
        val spawner = creep.room.find(FIND_MY_SPAWNS).firstOrNull() ?: let {
            ScreepsLog.d(TAG, "No spawner to deposit energy into")
            return
        }

        val code = creep.transfer(spawner, RESOURCE_ENERGY)

        if (code == ERR_NOT_IN_RANGE) {
            creep.moveTo(spawner)
        } else if (code == ERR_NOT_ENOUGH_ENERGY) {
            creep.say("Out of energy")
            creep.setState(CreepState.GetEnergy)
            return
        } else if (code == ERR_FULL) {
            ScreepsLog.d(TAG, "Spawner full of energy, dropping energy for other creeps to use")
            creep.drop(RESOURCE_ENERGY)
            creep.setState(CreepState.GetEnergy)
        } else if (code != OK) {
            creep.say("Transfer failed with code $code")
        }

        if (creep.store.getUsedCapacity(RESOURCE_ENERGY) <= 0) {
            creep.setState(CreepState.GetEnergy)
        }
    }
}
