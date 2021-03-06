package be.catvert.pc.components.logics

import be.catvert.pc.GameObject
import be.catvert.pc.containers.GameObjectMatrixContainer
import be.catvert.pc.actions.Action
import be.catvert.pc.actions.EmptyAction
import be.catvert.pc.actions.PhysicsAction
import be.catvert.pc.components.UpdeatableComponent
import be.catvert.pc.containers.Level
import be.catvert.pc.utility.*
import com.badlogic.gdx.math.MathUtils
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import be.catvert.pc.actions.PhysicsAction.PhysicsActions

/**
 * Enmu permettant de définir le type de mouvement de l'entité (fluide ou linéaire)
 */
enum class MovementType {
    SMOOTH, LINEAR
}

/**
 * Classe de données permettant de gérer les sauts notament en définissant la hauteur du saut
 * @property isJumping : Permet de savoir si l'entité est entrain de sauté
 * @property targetHeight : La hauteur en y à atteindre
 * @property startJumping : Débute le saut de l'entité
 * @property forceJumping : Permet de forcer le saut de l'entité
 */
data class JumpData(var isJumping: Boolean = false, var targetHeight: Int = 0, var startJumping: Boolean = false, var forceJumping: Boolean = false)

/**
 * Enum permettant de connaître le côté toucher lors d'une collision
 */
enum class CollisionSide {
    OnLeft, OnRight, OnUp, OnDown, Unknow;

    operator fun unaryMinus(): CollisionSide = when (this) {
        CollisionSide.OnLeft -> OnRight
        CollisionSide.OnRight -> OnLeft
        CollisionSide.OnUp -> OnDown
        CollisionSide.OnDown -> OnUp
        CollisionSide.Unknow -> Unknow
    }
}


/**
 * Permet de déterminer quel mask l'entité doit utiliser pour les collisions
 */
enum class MaskCollision {
    ALL, ONLY_PLAYER, ONLY_ENEMY
}

/**
 * Listener lors d'une collision avec une autre entité pour le mask correspondant
 */
data class CollisionListener(val gameObject: GameObject, val collideGameObject: GameObject, val side: CollisionSide)

/**
 * Ce component permet d'ajouter à l'entité des propriétés physique tel que la gravité, vitesse de déplacement ...
 * Une entité contenant ce component ne pourra pas être traversé par une autre entité ayant également ce component
 * @param isStatic : Permet de spécifier si l'entité est sensé bougé ou non
 * @param moveSpeed : La vitesse de déplacement de l'entité qui sera utilisée avec les NextActions
 * @param movementType : Permet de définir le type de déplacement de l'entité
 * @param gravity : Permet de spécifier si la gravité est appliquée à l'entité
 */
class PhysicsComponent(@ExposeEditor var isStatic: Boolean,
                       @ExposeEditor(maxInt = 100) var moveSpeed: Int = 0,
                       @ExposeEditor var movementType: MovementType = MovementType.LINEAR,
                       @ExposeEditor var gravity: Boolean = !isStatic,
                       @ExposeEditor var maskCollision: MaskCollision = MaskCollision.ALL,
                       @ExposeEditor(maxInt = 1000) var jumpHeight: Int = 0,
                       @ExposeEditor var jumpAction: Action = EmptyAction()) : UpdeatableComponent() {
    @JsonCreator private constructor(): this(true)

    /**
     * Les nextActions représentes les actions que doit faire l'entité pendant le prochain frame
     */
    @JsonIgnore
    val nextActions = mutableSetOf<PhysicsActions>()

    @JsonIgnore private val jumpData: JumpData = JumpData()

    /**
     * La vitesse de déplacement x actuelle de l'entité (à titre d'information)
     */
    @JsonIgnore private var actualMoveSpeedX = 0f

    /**
     * La vitesse de déplacement y actuelle de l'entité (à titre d'information)
     */
    @JsonIgnore private var actualMoveSpeedY = 0f

    /**
     * Signal appelé lorsque une entité ayant ce component touche cette entité
     */
    @JsonIgnore
    val onCollisionWith = Signal<CollisionListener>()

    /**
     * Permet à l'entité de savoir si elle est sur le sol ou non
     */
    @JsonIgnore private var isOnGround = false

    @JsonIgnore private val gravitySpeed = 15

    override fun update(gameObject: GameObject) {
        if (isStatic) return

        if (gravity && (gameObject.container as? Level)?.applyGravity == true)
            nextActions += PhysicsAction.PhysicsActions.GRAVITY

        if (jumpData.forceJumping) {
            nextActions += PhysicsAction.PhysicsActions.JUMP
        }

        var moveSpeedX = 0f
        var moveSpeedY = 0f
        var addJumpAfterClear = false

        nextActions.forEach action@ {
            when (it) {
                PhysicsActions.GO_LEFT -> moveSpeedX -= moveSpeed
                PhysicsActions.GO_RIGHT -> moveSpeedX += moveSpeed
                PhysicsActions.GO_UP -> moveSpeedY += moveSpeed
                PhysicsActions.GO_DOWN -> moveSpeedY -= moveSpeed
                PhysicsActions.GRAVITY -> if (gravity) moveSpeedY -= gravitySpeed
                PhysicsActions.JUMP -> {
                    if (!jumpData.isJumping) {
                        if (!jumpData.forceJumping) {
                            if (!checkIsOnGround(gameObject)) {
                                return@action
                            }
                        } else {
                            jumpData.forceJumping = false
                        }
                        jumpData.isJumping = true
                        jumpData.targetHeight = gameObject.rectangle.y + jumpHeight
                        jumpData.startJumping = true

                        gravity = false

                        moveSpeedY = gravitySpeed.toFloat()
                        addJumpAfterClear = true
                         jumpAction?.invoke(gameObject)
                    } else {
                        // Vérifie si le go est arrivé à la bonne hauteur de saut ou s'il rencontre un obstacle au dessus de lui
                        if (gameObject.rectangle.y >= jumpData.targetHeight || collideOnMove(0, gravitySpeed, gameObject)) {
                            gravity = true
                            jumpData.isJumping = false
                        } else {
                            moveSpeedY = gravitySpeed.toFloat()
                            addJumpAfterClear = true
                        }
                        jumpData.startJumping = false
                    }
                }
            }
        }

        if (movementType == MovementType.SMOOTH) {
            moveSpeedX = MathUtils.lerp(actualMoveSpeedX, moveSpeedX, 0.2f)
            moveSpeedY = MathUtils.lerp(actualMoveSpeedY, moveSpeedY, 0.2f)
        }

        actualMoveSpeedX = moveSpeedX
        actualMoveSpeedY = moveSpeedY

        tryMove(moveSpeedX.toInt(), moveSpeedY.toInt(), gameObject) // move l'entité

        nextActions.clear()

        if (addJumpAfterClear)
            nextActions += PhysicsActions.JUMP

        isOnGround = checkIsOnGround(gameObject)
    }
}

/**
 * Permet d'essayer de déplacer une entité ayant un physicsComponent
 * @param moveX : Le déplacement x
 * @param moveY : Le déplacement y
 */
private fun tryMove(moveX: Int, moveY: Int, gameObject: GameObject) {
    if (moveX != 0 || moveY != 0) {
        var newMoveX = moveX
        var newMoveY = moveY

        if (!collideOnMove(moveX, 0, gameObject)) {
            gameObject.rectangle.x = Math.max(0, gameObject.rectangle.x + moveX)
            newMoveX = 0
        }
        if (!collideOnMove(0, moveY, gameObject)) {
            gameObject.rectangle.y += moveY
            newMoveY = 0
        }

        if (newMoveX > 0)
            newMoveX -= 1
        else if (newMoveX < 0)
            newMoveX += 1

        if (newMoveY > 0)
            newMoveY -= 1
        else if (newMoveY < 0)
            newMoveY += 1
        tryMove(newMoveX, newMoveY, gameObject)
    }
}

/**
 * Permet de vérifier si l'entité est sur le sol ou pas
 */
private fun checkIsOnGround(gameObject: GameObject) = collideOnMove(0, -1, gameObject)

/**
 * Permet de tester si un déplacement est possible ou non
 * @param moveX : Le déplacement x
 * @param moveY : Le déplacement y
 * @param gameObject : Le gameObject a testé
 */
private fun collideOnMove(moveX: Int, moveY: Int, gameObject: GameObject): Boolean {
    val newRect = Rect(gameObject.rectangle)
    newRect.position = Point(newRect.x + moveX, newRect.y + moveY)

    if (gameObject.container != null && gameObject.container is GameObjectMatrixContainer) {
        val container = gameObject.container!! as GameObjectMatrixContainer
        container.getRectCells(newRect).forEach {
            container.matrixGrid[it.x][it.y].first.filter {
                it.id != gameObject.id
                        && it.getCurrentState().hasComponent<PhysicsComponent>()
                        && when (it.getCurrentState().getComponent<PhysicsComponent>()?.maskCollision) {
                    MaskCollision.ALL -> true
                    MaskCollision.ONLY_PLAYER -> gameObject.tag == GameObject.Tag.Player
                    MaskCollision.ONLY_ENEMY -> gameObject.tag == GameObject.Tag.Enemy
                    null -> false
                }
            }.forEach {
                if (newRect.overlaps(it.rectangle)) {
                    val side = if (moveX > 0) CollisionSide.OnRight else if (moveX < 0) CollisionSide.OnLeft else if (moveY > 0) CollisionSide.OnUp else if (moveY < 0) CollisionSide.OnDown else CollisionSide.Unknow

                    gameObject.getCurrentState().getComponent<PhysicsComponent>()?.onCollisionWith?.invoke(CollisionListener(gameObject, it, side))
                    it.getCurrentState().getComponent<PhysicsComponent>()?.onCollisionWith?.invoke(CollisionListener(it, gameObject, -side))

                    return true
                }
            }
        }
    }
    return false
}