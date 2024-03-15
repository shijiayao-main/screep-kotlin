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
import screeps.sdk.extensions.calculateDistance
import screeps.sdk.extensions.calculateWeights
import screeps.sdk.extensions.energyIsFull
import screeps.sdk.extensions.getState
import screeps.sdk.extensions.getTargetId
import screeps.sdk.extensions.setTargetPos
import screeps.sdk.extensions.tryToStoreOwner

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
                // 运送资源
                val storeOwner = getTargetStoreOwner(creep = creep) ?: return@forEach
                val status = creep.transfer(storeOwner, RESOURCE_ENERGY)
                if (status == ERR_NOT_IN_RANGE) {
                    // { visualizePathStyle: { stroke: '#ffaa00' } }
                    creep.moveTo(
                        storeOwner.pos.x,
                        storeOwner.pos.y,
                    )
                } else if (status != OK) {
                    ScreepsLog.d(TAG, "Gather failed with code $status")
                }
            }
        }
    }

    private fun getTargetStoreOwner(
        creep: Creep
    ): StoreOwner? {
        val targetId = creep.getTargetId()
        val storeOwner = if (targetId.isNullOrBlank()) {
            null
        } else {
            roomInfo.roomStructureInfo.myStructureMap[targetId] .tryToStoreOwner()
        }

        if (storeOwner != null) {
            return storeOwner
        } else {
            val findStoreOwner = getNeedEnergyStoreOwner (
                creep = creep,
            ) ?: return null

            val id = findStoreOwner.id
            creep.setTargetPos(id)
            return findStoreOwner
        }
    }

    private fun getNeedEnergyStoreOwner(
        creep: Creep,
    ): StoreOwner? {

        val spawnMap = roomInfo.roomStructureInfo.spawnMap.filter {
            it.value.store.getFreeCapacity() > 0
        }
        val spawnKey = spawnMap.keys.first()
        val spawn: StructureSpawn? = spawnMap[spawnKey]

        if (spawn != null && spawn.energyIsFull().not()) {
            return spawn
        }

        val structureLink = roomInfo.roomStructureInfo.linkStructureList.minByOrNull {
            creep.calculateDistance(it)
        }

        if (structureLink != null && structureLink.energyIsFull().not()) {
            return structureLink
        }

        val structureExtension = roomInfo.roomStructureInfo.extensionStructureList.filter {
            it.energyIsFull().not()
        }.minByOrNull {
            creep.calculateWeights(it)
        }

        if (structureExtension != null && structureExtension.energyIsFull().not()) {
            return structureExtension
        }

        val structureContainer = roomInfo.roomStructureInfo.containerStructureList.filter {
            it.energyIsFull().not()
        }.minByOrNull {
            creep.calculateWeights(it)
        }

        if (structureContainer != null && structureContainer.energyIsFull().not()) {
            return structureContainer
        }

        val structureStorage = roomInfo.roomStructureInfo.structureStorage

        if (structureStorage != null && structureStorage.energyIsFull().not()) {
            return structureStorage
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
