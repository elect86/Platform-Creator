package be.catvert.pc

import be.catvert.pc.serialization.PostDeserialization
import be.catvert.pc.utility.Rect
import be.catvert.pc.utility.Renderable
import be.catvert.pc.utility.Updeatable
import com.badlogic.gdx.graphics.g2d.Batch
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

abstract class GameObjectContainer : Renderable, Updeatable, PostDeserialization {
    @JsonProperty("objects") protected val gameObjects: MutableSet<GameObject> = mutableSetOf()

    @JsonIgnore var allowRendering = true
    @JsonIgnore var allowUpdating = true

    @JsonIgnore var removeEntityBelowY0 = true

    @JsonIgnore fun getGameObjectsData() = gameObjects.toSet()

    fun findGameObjectByID(id: UUID): GameObject? = gameObjects.firstOrNull { it.id == id }

    fun findGameObjectsByTag(tag: GameObject.Tag): Set<GameObject> = gameObjects.filter { it.tag == tag }.toSet()

    open fun removeGameObject(gameObject: GameObject) {
        onRemoveGameObject(gameObject)
        gameObjects.remove(gameObject)
        gameObject.container = null
    }

    open fun addGameObject(gameObject: GameObject): GameObject {
        gameObjects.add(gameObject)
        gameObject.container = this

        return gameObject
    }

    fun createGameObject(rectangle: Rect = Rect(), tag: GameObject.Tag, prefab: Prefab? = null, init: GameObject.() -> Unit = {}): GameObject {
        val go = GameObject(UUID.randomUUID(), mutableSetOf(), rectangle, tag,this, prefab)
        go.init()

        addGameObject(go)

        return go
    }

    protected open fun onRemoveGameObject(gameObject: GameObject) {}

    override fun render(batch: Batch) {
        if (allowRendering)
            gameObjects.forEach { it.render(batch) }
    }

    override fun update() {
        if (allowUpdating) {
            val iter = gameObjects.iterator()
            while(iter.hasNext()) {
                val it = iter.next()
                it.update()
                if(removeEntityBelowY0 && it.position().y < 0) {
                    it.isRemoving = true
                }
                if(it.isRemoving) {
                    onRemoveGameObject(it)
                    iter.remove()
                }
            }
        }
    }

    override fun onPostDeserialization() {
        gameObjects.forEach {
            it.container = this

        }
    }

    operator fun plusAssign(gameObject: GameObject) {
        addGameObject(gameObject)
    }
}
