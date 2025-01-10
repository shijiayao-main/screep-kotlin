package screeps.api.roles

import screeps.api.Creep
import screeps.api.ERR_NOT_ENOUGH_ENERGY
import screeps.api.ERR_NOT_FOUND
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.OK
import screeps.api.RESOURCE_ENERGY
import screeps.api.compareTo
import screeps.api.entity.RoomInfo
import screeps.sdk.ScreepsLog
import screeps.sdk.extensions.getState
import screeps.sdk.extensions.setState

class UpdaterRole(
    creepList: List<Creep>,
    roomInfo: RoomInfo,
) : AbstractRole(
    creepList = creepList,
    roomInfo = roomInfo
) {

    companion object {
        private const val TAG = "UpgraderRole"
    }

    override fun startWork() {
    }

    fun run(creep: Creep) {
        when (creep.getState()) {
            CreepState.GetEnergy -> {
                getEnergy(creep = creep)
                if (creep.store.getFreeCapacity() == 0) {
                    creep.say("Energy full")
                    creep.setState(CreepState.DoWork)
                }
            }

            CreepState.DoWork -> {
                upgradeController(creep = creep)
            }
        }
    }

    private fun getEnergy(creep: Creep) {
        val storage = creep.room.storage

        if (storage == null || (storage.store.getUsedCapacity(RESOURCE_ENERGY) ?: 0) <= 0) {
            val code = pickupEnergy(creep = creep)
            // If no energy could be found, try and use whatever energy we do have
            if (code == ERR_NOT_FOUND && creep.store.getUsedCapacity(RESOURCE_ENERGY) > 0) {
                creep.setState(CreepState.DoWork)
            }
            return
        }

        val code = creep.withdraw(storage, RESOURCE_ENERGY)
        if (code == ERR_NOT_IN_RANGE) {
            creep.moveTo(storage)
        } else if (code != OK) {
            ScreepsLog.d(TAG, "Couldn't withdraw from storage due to error: $code")
        }
    }

    private fun upgradeController(creep: Creep) {
        val controller = creep.room.controller

        if (controller == null) {
            ScreepsLog.d(TAG, "No controller!")
            return
        }

        val status = creep.upgradeController(controller)

        if (status == ERR_NOT_IN_RANGE) {
            creep.moveTo(controller)
        } else if (status == ERR_NOT_ENOUGH_ENERGY) {
            creep.say("Out of energy")
            creep.setState(CreepState.GetEnergy)
            return
        } else if (status != OK) {
            creep.say("Upgrade failed with code $status")
        }

        if (creep.store.getCapacity(RESOURCE_ENERGY) <= 0) {
            creep.setState(CreepState.GetEnergy)
        }
    }
}
