package io.github.e_vent.repo

import io.github.e_vent.vo.Event

/**
 * Common interface shared by the different repository implementations.
 * Note: this only exists for sample purposes - typically an app would implement a repo once, either
 * network+db, or network-only
 */
interface EventPostRepo {
    fun posts(pageSize: Int): Listing<Event>
}