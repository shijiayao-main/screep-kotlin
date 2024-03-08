package screeps.ai.roles

import screeps.ai.entity.RoomInfo
import screeps.api.CARRY
import screeps.api.Creep
import screeps.api.ERR_NOT_IN_RANGE
import screeps.api.OK
import screeps.api.RESOURCE_ENERGY
import screeps.api.Source
import screeps.api.StoreOwner
import screeps.api.structures.StructureSpawn
import screeps.sdk.ScreepsLog
import screeps.sdk.extensions.calculateWeights
import screeps.sdk.extensions.energyIsFull
import screeps.sdk.extensions.getState
import screeps.sdk.extensions.getTargetId

/**
 * 收集者, 用于采集资源
 */
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

    override fun startWork() {
        val energySourceMap = roomInfo.roomStructureInfo.sourceMap

        if (energySourceMap.isEmpty()) {
            return
        }

        val sourceCountMap: MutableMap<String, Int> = getSourceCountMap()

        val energySourceList: MutableList<Source> = ArrayList()
        roomInfo.roomStructureInfo.sourceMap.forEach {
            val key = it.key
            val value = it.value
            val count = sourceCountMap[key] ?: 0
            if (count < 3) {
                energySourceList.add(value)
            }
        }

        val spawnMap = roomInfo.roomStructureInfo.spawnMap.filter {
            it.value.store.getFreeCapacity() > 0
        }
        val spawnKey = spawnMap.keys.first()
        val spawn: StructureSpawn? = spawnMap[spawnKey]

        val structureStorage = roomInfo.roomStructureInfo.structureStorage
        val structureLinkMap = roomInfo.roomStructureInfo.linkStructureList
        val structureContainerMap = roomInfo.roomStructureInfo.containerStructureList
        val structureExtensionMap = roomInfo.roomStructureInfo.extensionStructureList

        creepList.forEach { creep: Creep ->
            val findCarry = creep.body.find {
                it.type == CARRY
            } != null
            if (creep.getState() == CreepState.GetEnergy || findCarry.not()) {
                if (energySourceList.isEmpty()) {
                    return@forEach
                }

                val energySource = getSuitableSource(
                    creep = creep,
                    energySourceList = energySourceList,
                    energySourceMap = energySourceMap
                ) ?: return@forEach

                val id = energySource.id
                sourceCountMap[id]?.let {
                    val count = it + 1
                    sourceCountMap[id] = count
                    if (count >= 3) {
                        energySourceList.remove(energySource)
                    }
                } ?: let {
                    sourceCountMap[id] = 1
                    1
                }

                harvestSource(
                    creep = creep,
                    energySource = energySource
                )
            } else {
                // todo: 运送资源

                val storeOwner = getStoreOwner(
                    creep = creep,
                    spawn = spawn,
                ) ?: return@forEach

                val status = creep.transfer(storeOwner, RESOURCE_ENERGY)
            }
        }
    }

    private fun getStoreOwner(
        creep: Creep,
        spawn: StructureSpawn?,
    ): StoreOwner? {
        if (spawn != null && spawn.energyIsFull().not()) {
            return spawn
        }

        return null
    }

    private fun getSuitableSource(
        creep: Creep,
        energySourceList: List<Source>,
        energySourceMap: Map<String, Source>
    ): Source? {

        val targetId = creep.getTargetId()
        if (targetId.isNullOrBlank()) {
            val source = energySourceList.minByOrNull {
                creep.calculateWeights(it)
            }
            return source
        }

        val targetSource = energySourceMap[targetId]
        if (targetSource == null) {
            val source = energySourceList.minByOrNull {
                creep.calculateWeights(it)
            }
            return source
        }
        return targetSource
    }

    private fun getSourceCountMap(): MutableMap<String, Int> {
        val energyHarvestCountMap: MutableMap<String, Int> = HashMap()
        creepList.forEach { creep ->
            val targetId = creep.getTargetId()
            if (targetId.isNullOrBlank()) {
                return@forEach
            }
            energyHarvestCountMap[targetId]?.let {
                energyHarvestCountMap[targetId] = it + 1
            } ?: let {
                energyHarvestCountMap[targetId] = 1
            }
        }
        return energyHarvestCountMap
    }

    private fun harvestSource(
        creep: Creep,
        energySource: Source
    ) {
        val status = creep.harvest(energySource)

        if (status == ERR_NOT_IN_RANGE) {
            // { visualizePathStyle: { stroke: '#ffaa00' } }
            creep.moveTo(
                energySource.pos.x,
                energySource.pos.y,
            )
        } else if (status != OK) {
            ScreepsLog.d(TAG, "Gather failed with code $status")
        }
    }
}
