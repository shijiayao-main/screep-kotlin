package screeps.ai.entity

import screeps.api.Creep

/**
 * @param harvesterList
 * @param builderList
 * @param updaterList
 * @param transporterList
 * @param maintainerList
 * @param defenderList
 * @param enemyList 敌人!!
 */
class RoomCreepInfo(
    val harvesterList: List<Creep>,
    val builderList: List<Creep>,
    val updaterList: List<Creep>,
    val transporterList: List<Creep>,
    val maintainerList: List<Creep>,
    val defenderList: List<Creep>,
    val enemyList: List<Creep>,
)
