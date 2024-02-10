package screeps.sdk.extensions

import screeps.api.ATTACK
import screeps.api.BodyPartConstant
import screeps.api.CARRY
import screeps.api.CLAIM
import screeps.api.HEAL
import screeps.api.MOVE
import screeps.api.RANGED_ATTACK
import screeps.api.TOUGH
import screeps.api.WORK

val BodyPartCost = hashMapOf(
    MOVE to 50,
    WORK to 100,
    ATTACK to 80,
    CARRY to 50,
    HEAL to 250,
    RANGED_ATTACK to 150,
    TOUGH to 10,
    CLAIM to 600
)

fun Array<BodyPartConstant>.getCost(): Int {
    var costSum = 0
    forEach {
        val cost: Int = BodyPartCost[it] ?: 0
        costSum += cost
    }
    return costSum
}
