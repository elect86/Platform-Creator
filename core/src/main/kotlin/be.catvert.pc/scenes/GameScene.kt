package be.catvert.pc.scenes

import be.catvert.pc.GameKeys
import be.catvert.pc.GameObjectContainer
import be.catvert.pc.Level
import be.catvert.pc.PCGame
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.MathUtils
import ktx.assets.loadOnDemand
import ktx.assets.toLocalFile

/**
 * Scène du jeu
 */
class GameScene(private val level: Level) : Scene() {
    override val gameObjectContainer: GameObjectContainer = level

    private val cameraMoveSpeed = 10f

    init {
        if (level.backgroundPath != null)
            backgroundTexture = PCGame.assetManager.loadOnDemand<Texture>(level.backgroundPath!!.toLocalFile().path()).asset
    }

    override fun postBatchRender() {
        super.postBatchRender()
        level.drawDebug()
    }

    override fun update() {
        super.update()

        updateCamera(true)

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
            PCGame.setScene(PauseScene(this), false)
        if(Gdx.input.isKeyJustPressed(GameKeys.GAME_SWITCH_GRAVITY.key))
            level.applyGravity = !level.applyGravity
    }

    private fun updateCamera(lerp: Boolean) {
        if (Gdx.input.isKeyPressed(GameKeys.CAMERA_ZOOM_UP.key)) {
            camera.zoom -= 0.02f
        }
        if (Gdx.input.isKeyPressed(GameKeys.CAMERA_ZOOM_DOWN.key)) {
            camera.zoom += 0.02f
        }
        if (Gdx.input.isKeyPressed(GameKeys.CAMERA_ZOOM_RESET.key)) {
            camera.zoom = 1f
        }

        if (level.followGameObject != null) {
            val go = level.followGameObject!!

            val posX = Math.max(0f + camera.viewportWidth / 2f, go.position().x + go.size().width / 2f)
            val posY = Math.max(0f + camera.viewportHeight / 2f, go.position().y + go.size().height / 2f)

            val lerpX = MathUtils.lerp(camera.position.x, posX, 0.1f)
            val lerpY = MathUtils.lerp(camera.position.y, posY, 0.1f)
            camera.position.set(if (lerp) lerpX else posX, if (lerp) lerpY else posY, 0f)
        } else {
            if (Gdx.input.isKeyPressed(GameKeys.GAME_CAMERA_LEFT.key)) {
                camera.position.x -= cameraMoveSpeed
            }
            if (Gdx.input.isKeyPressed(GameKeys.GAME_CAMERA_RIGHT.key)) {
                camera.position.x += cameraMoveSpeed
            }
            if (Gdx.input.isKeyPressed(GameKeys.GAME_CAMERA_DOWN.key)) {
                camera.position.y -= cameraMoveSpeed
            }
            if (Gdx.input.isKeyPressed(GameKeys.GAME_CAMERA_UP.key)) {
                camera.position.y += cameraMoveSpeed
            }
        }

        camera.update()
    }
}