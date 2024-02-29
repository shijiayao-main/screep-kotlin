package screeps.ai.roles

import screeps.ai.entity.RoomInfo
import screeps.api.Creep
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.FIND_CONSTRUCTION_SITES
import screeps.api.Flag
import screeps.api.FlagMemory
import screeps.api.Game
import screeps.api.OK
import screeps.api.Room
import screeps.api.STRUCTURE_SPAWN
import screeps.api.get
import screeps.api.structures.StructureController
import screeps.sdk.ScreepsLog
import screeps.sdk.utils.getPathToTarget
import screeps.utils.memory.memory

var FlagMemory.spawnerId: String? by memory()

class ClaimerRole(
    creepList: List<Creep>,
    roomInfo: RoomInfo
) : AbstractRole(
    creepList = creepList,
    roomInfo = roomInfo
)  {

    companion object {
        private const val TAG = "ClaimerRole"
    }

    private val targetFlag: Flag? = Game.flags["NextRoom"]
    private val targetController: StructureController? = targetFlag?.room?.controller

    private fun moveToFlag() {
        if (targetFlag == null) {
            ScreepsLog.d(TAG, "No target room flag located!")
            return
        }

//        val path = getPathToTarget(creep.pos, targetFlag.pos)
//        creep.move(creep.pos.getDirectionTo(path[0]))
    }

    private fun claimRoom() {
//        if (targetController == null || targetController.room != creep.room) {
//            ScreepsLog.d(TAG, "Not in room with controller, navigating to $targetFlag instead")
//            moveToFlag()
//            return
//        }
//
//        if (targetController.my) {
//            ScreepsLog.d(TAG, "$targetController already claimed!")
//            setupRoom(targetController.room)
//            return
//        }

//        when (val code = creep.claimController(targetController)) {
//            OK -> {
//                ScreepsLog.d(TAG, "${targetController.room} claimed!")
//            }
//
//            ERR_NOT_IN_RANGE -> {
//                creep.moveTo(targetController)
//            }
//
//            else -> {
//                ScreepsLog.d(TAG, "Claiming ${targetController.room} failed: $code")
//            }
//        }
    }

    private fun setupRoom(room: Room) {
        val spawnPos = targetFlag?.pos ?: let {
            ScreepsLog.d(TAG, "Could not find flag to create initial spawn")
            return
        }

        val code = room.createConstructionSite(spawnPos, STRUCTURE_SPAWN)

        val spawner = room.find(FIND_CONSTRUCTION_SITES).first()
        targetFlag.memory.spawnerId = spawner.id

        if (code == OK) {
            ScreepsLog.d(TAG, "$room successfully initialized, construction may begin!")
        }
    }

    override fun startWork() {
        claimRoom()
    }
}
