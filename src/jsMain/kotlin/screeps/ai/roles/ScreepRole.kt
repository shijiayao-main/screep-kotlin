package screeps.ai.roles

enum class CreepRole(
    val role: Int,
    val namePrefix: String
) {
    // 未指定的
    Unassigned(role = 0, namePrefix = ""),

    // 收集, 用于收集资源
    Harvester(role = 1, namePrefix = "harvester"),

    // 运输, 用于运输资源
    Transporter(role = 2, namePrefix = "transporter"),

    // 升级, 用于升级
    Updater(role = 3, namePrefix = "updater"),

    // 建造, 用于建造
    Builder(role = 4, namePrefix = "builder"),

    // 维护, 用于维护
    Maintainer(role = 5, namePrefix = "maintainer"),

    // 用于防守敌人的进攻
    Defender(role = 6, namePrefix = "defender"),
    Claimer(role = 7, namePrefix = "claimer"),
    RemoteConstruction(role = 8, namePrefix = "remove_construction")
}

fun String?.getCreepRoleByName(): CreepRole {
    this ?: return CreepRole.Unassigned
    return when {
        startsWith(CreepRole.Harvester.namePrefix) -> CreepRole.Harvester
        startsWith(CreepRole.Transporter.namePrefix) -> CreepRole.Transporter
        startsWith(CreepRole.Updater.namePrefix) || startsWith("upgrader") -> {
            CreepRole.Updater
        }

        startsWith(CreepRole.Builder.namePrefix) -> CreepRole.Builder
        startsWith(CreepRole.Maintainer.namePrefix) -> CreepRole.Maintainer
        startsWith(CreepRole.Defender.namePrefix) -> CreepRole.Defender
        startsWith(CreepRole.Claimer.namePrefix) -> CreepRole.Claimer
        startsWith(CreepRole.RemoteConstruction.namePrefix) -> CreepRole.RemoteConstruction
        else -> CreepRole.Unassigned
    }
}
