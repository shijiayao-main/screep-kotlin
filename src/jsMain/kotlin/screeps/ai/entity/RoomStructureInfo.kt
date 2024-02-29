package screeps.ai.entity

import screeps.api.ConstructionSite
import screeps.api.Source
import screeps.api.structures.Structure
import screeps.api.structures.StructureController
import screeps.api.structures.StructureSpawn
import screeps.api.structures.StructureTower

class RoomStructureInfo(
    val controller: StructureController?,
    val spawnMap: Map<String, StructureSpawn>,
    val sourceMap: Map<String, Source>,
    val myConstructionMap: Map<String, ConstructionSite>,
    val selfNeedRepairBuildList: List<Structure>,
    val publicNeedRepairBuildList: List<Structure>,
    val towerMap: Map<String, StructureTower>,
)
