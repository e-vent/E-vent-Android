package io.github.e_vent.vo

data class ServerEvent(
        val name: String,
        val desc: String,
        val bg: String
) {
    fun toClientEvent(id: Int): ClientEvent {
        return ClientEvent(id, name, desc, bg)
    }
}
