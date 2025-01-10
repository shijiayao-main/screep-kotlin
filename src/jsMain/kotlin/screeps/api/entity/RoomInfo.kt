package screeps.api.entity

import screeps.api.Room

/**
 * 存储房间内的creep和建筑信息
 */
class RoomInfo(
    val room: Room,
    val roomCreepInfo: RoomCreepInfo,
    val roomStructureInfo: RoomStructureInfo
)
