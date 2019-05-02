package com.example.kotlin_application_2

class Item(private val product: String,
           private val priority: Int,
           private val quantity: Int, private val price: String) :
    Comparable<Item> {

    override fun toString(): String {
        return "$product\t\tx$quantity\n\nTotal price for items = $$price\t\t\t\tPriority = $priority"
    }

    fun getProduct(): String {
        return product
    }

    fun getPriority(): Int? {
        return priority
    }

    fun getPrice(): String {
        return price
    }

    override fun compareTo(other: Item): Int {
        return when {
            this.priority < other.priority -> -1
            this.priority == other.priority -> 0
            else -> 1
        }
    }
}
