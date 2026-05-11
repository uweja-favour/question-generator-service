package com.xapps.question_generator

import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

abstract class BasePersistableEntity(
    @field:Transient
    private var newEntity: Boolean = false
) : Persistable<String> {

    override fun isNew(): Boolean = newEntity

    abstract fun getTheId(): String

    override fun getId(): String? = getTheId()

    fun markNew(): BasePersistableEntity {
        newEntity = true
        return this
    }
}


suspend fun <T : BasePersistableEntity>
        CoroutineCrudRepository<T, String>.saveAllUpserting(
    items: List<T>
) {
    items.forEach { item ->
        save(
            if (existsById(item.getTheId()))
                item
            else
                item.markNew() as T
        )
    }
}

suspend fun <T : BasePersistableEntity>
        CoroutineCrudRepository<T, String>.saveUpserting(
    item: T
) {
    save(
        if (existsById(item.getTheId()))
            item
        else
            item.markNew() as T
    )
}

