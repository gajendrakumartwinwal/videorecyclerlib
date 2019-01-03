package com.example.kotlin.recyclerview

open class ItemList(parent: Int, id: Int, @ViewTemplate type: Int) {

    var id: Int = 0
    var parent: Int = 0
    var type: Int = 0

    init {
        this.parent = parent
        this.id = id
        this.type = type
    }

    override fun hashCode(): Int {
        return id
    }

    fun parent(): Int {
        return parent;
    }

    fun id(): Int {
        return id;
    }
}
