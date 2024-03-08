package screeps.sdk.extensions

import screeps.api.Source

fun Source.getFreeCapacity(): Int {
    return this.energyCapacity - this.energy
}