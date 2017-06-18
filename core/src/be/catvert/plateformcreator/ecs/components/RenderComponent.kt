package be.catvert.plateformcreator.ecs.components

import be.catvert.plateformcreator.TextureInfo
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Vector2

/**
 * Created by Catvert on 05/06/17.
 */

/**
 * Enum permettant de spécifier comment la taille de l'entité doit changer
 */
enum class ResizeMode {
    NO_RESIZE, ACTUAL_REGION, FIXED_SIZE
}

/**
 * Ce component permet d'ajouter une/des textures et/ou une/des animations à l'entité
 * textureInfoList : Liste des textures que dispose l'entité
 * animationList : Liste des animations que dispose l'entité
 * useAnimation : Permet de spécifier si l'entité doit utiliser une animation ou une texture
 * flipX : Permet de retourner horizontalement la texture
 * flipY : Permet de retourner verticalement la texture
 * renderLayer : Permet de spécifier à quel priorité il faut dessiner l'entité, plus il est élevé, plus l'entité sera en avant-plan
 * resizeMode : Permet de spécifier comment la taille de l'entité doit changer
 */
class RenderComponent(val textureInfoList: List<TextureInfo> = listOf(),
                      val animationList: List<Animation<TextureAtlas.AtlasRegion>> = listOf(),
                      var useAnimation: Boolean = false,
                      var flipX: Boolean = false,
                      var flipY: Boolean = false,
                      var renderLayer: Int = 0,
                      var resizeMode: ResizeMode = ResizeMode.NO_RESIZE) : BaseComponent<RenderComponent>() {
    override fun copy(): RenderComponent {
        return RenderComponent(textureInfoList, animationList, useAnimation, initialFlipX, initialFlipY, renderLayer, resizeMode)
    }

    private var stateTime = 0f

    private val initialFlipX = flipX
    private val initialFlipY = flipY

    /**
     * Permet au RenderSystem de savoir qu'elle texture il doit utiliser
     */
    var actualTextureInfoIndex: Int = 0
        set(value) {
            field = value

            useAnimation = false
        }

    /**
     * Permet au RenderSystem de savoir qu'elle animation il doit utiliser
     */
    var actualAnimationIndex: Int = 0

    /**
     * Permet de spécifier une taille fixe à l'entité
     * Le resizeMode doit être en FIXED_RESIZE
     */
    var fixedResize: Vector2? = null

    /**
     * Permet de retourner la region à dessiner
     * En appelant cette fonction, l'animation actuelle se met à jour (si useAnimation est vrai).
     */
    fun getActualAtlasRegion(): TextureAtlas.AtlasRegion {
        if (useAnimation) {
            stateTime += Gdx.graphics.deltaTime
            return animationList[actualAnimationIndex].getKeyFrame(stateTime, true)
        } else {
            return textureInfoList[actualTextureInfoIndex].texture
        }
    }
}