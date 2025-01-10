package screeps.api.roles

import screeps.api.ConstructionSite
import screeps.api.Creep
import screeps.api.ERR_NOT_ENOUGH_ENERGY
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.OK
import screeps.api.RESOURCE_ENERGY
import screeps.api.compareTo
import screeps.api.entity.RoomInfo
import screeps.sdk.ScreepsLog
import screeps.sdk.extensions.getState
import screeps.sdk.extensions.setState

class BuilderRole(
    creepList: List<Creep>,
    roomInfo: RoomInfo
) : AbstractRole(
    creepList = creepList,
    roomInfo = roomInfo
) {

    companion object {
        private const val TAG = "BuilderRole"
    }

    override fun startWork() {
        creepList.forEach {
            run(creep = it)
        }
    }

    private fun run(creep: Creep) {
        val state = creep.getState()
        when (state) {
            CreepState.GetEnergy -> {
                getEnergy(creep = creep)
                if (creep.store.getFreeCapacity() == 0) {
                    creep.say("Energy full")
                    creep.setState(CreepState.DoWork)
                }
            }

            CreepState.DoWork -> {
                buildBuildings(creep = creep)
            }
        }
    }

    private fun getEnergy(creep: Creep) {
        creep.say("🔄 Harvesting")
        val storage = creep.room.storage

        if (storage == null || (storage.store.getUsedCapacity(RESOURCE_ENERGY) ?: 0) <= 0) {
            pickupEnergy(creep = creep)
            return
        }

        val code = creep.withdraw(storage, RESOURCE_ENERGY)
        if (code == ERR_NOT_IN_RANGE) {
            creep.moveTo(storage)
        } else if (code != OK) {
            ScreepsLog.d(TAG, "Couldn't withdraw from storage due to error: $code")
        }
    }

    private fun buildBuildings(creep: Creep) {
        creep.say("🚧 Building")

        val constructionSite: ConstructionSite? = roomInfo.roomStructureInfo.myConstructionList.firstOrNull()

        if (constructionSite == null) {
            // Fall back to repairing buildings if there are none that need to be built
            repairBuildings(creep = creep)
            return
        }

        val status = creep.build(constructionSite)

        if (status == ERR_NOT_IN_RANGE) {
            creep.moveTo(constructionSite)
        } else if (status == ERR_NOT_ENOUGH_ENERGY) {
            creep.say("Out of energy")
            creep.setState(CreepState.GetEnergy)
            return
        } else if (status != OK) {
            ScreepsLog.d(TAG, "Build failed with code $status")
        }

        if (creep.store.getCapacity(RESOURCE_ENERGY) <= 0) {
            creep.setState(CreepState.GetEnergy)
        }
    }

    private fun repairBuildings(creep: Creep) {
        val building = roomInfo.roomStructureInfo.selfNeedRepairBuildList.firstOrNull()

        if (building == null) {
            ScreepsLog.d(TAG, "No available buildings to repair!")
            return
        }

        val status = creep.repair(building)

        if (status == ERR_NOT_IN_RANGE) {
            creep.moveTo(building)
        } else if (status == ERR_NOT_ENOUGH_ENERGY) {
            creep.say("Out of energy")
            creep.setState(CreepState.GetEnergy)
            return
        } else if (status != OK) {
            ScreepsLog.d(TAG, "Repair failed with code $status")
        }

        if (creep.store.getCapacity(RESOURCE_ENERGY) <= 0) {
            creep.setState(CreepState.GetEnergy)
        }
    }
}
