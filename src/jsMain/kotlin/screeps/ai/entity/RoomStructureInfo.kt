package screeps.ai.entity

import screeps.api.ConstructionSite
import screeps.api.Source
import screeps.api.structures.Structure
import screeps.api.structures.StructureController
import screeps.api.structures.StructureSpawn
import screeps.api.structures.StructureTower

class RoomStructureInfo(
    val controller: StructureController?,
    val spawnList: List<StructureSpawn>,
    val sourceList: List<Source>,
    val myConstructionList: List<ConstructionSite>,
    val selfNeedRepairBuildList: List<Structure>,
    val publicNeedRepairBuildList: List<Structure>,
    val towerList: List<StructureTower>,
)
