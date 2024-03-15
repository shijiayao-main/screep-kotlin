package screeps.ai.entity

import screeps.api.ConstructionSite
import screeps.api.Source
import screeps.api.structures.Structure
import screeps.api.structures.StructureContainer
import screeps.api.structures.StructureController
import screeps.api.structures.StructureExtension
import screeps.api.structures.StructureLink
import screeps.api.structures.StructureSpawn
import screeps.api.structures.StructureStorage
import screeps.api.structures.StructureTower

/**
 * @param myStructureMap 自己的建筑
 * @param extensionStructureList 用于建造更大的creep
 * @param linkStructureList
 * @param structureStorage 资源容器
 */
class RoomStructureInfo(
    val controller: StructureController?,
    val spawnMap: Map<String, StructureSpawn>,
    val structureStorage: StructureStorage?,
    val sourceMap: Map<String, Source>,
    val myStructureMap: Map<String, Structure>,
    val extensionStructureList: List<StructureExtension>,
    val containerStructureList: List<StructureContainer>,
    val linkStructureList: List<StructureLink>,
    val myConstructionList: List<ConstructionSite>,
    val selfNeedRepairBuildList: List<Structure>,
    val publicNeedRepairBuildList: List<Structure>,
    val towerMap: Map<String, StructureTower>,
)
