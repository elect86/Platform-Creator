package be.catvert.plateformcreator.ecs

import com.badlogic.ashley.core.Entity

/**
 * Created by Catvert on 12/06/17.
 */

/**
 * Cette classe permet d'ajouter une méthode d'ajout et de suppression d'une entité spécifique
 * Elle est spécifiquement utilisée pour les entités ayant le besoin d'ajouter ou supprimer une entité et d'ajouter des points de score au joueur
 * Méthode (ré)implémentée par gameScene permettant de supprimer ou d'ajouter une entité et d'ajouter des points de score au joueur
 */
class EntityEvent private constructor() {
    companion object {
        var onEntityRemoved: (entity: Entity) -> Unit = {
            throw Exception("Méthode onEntityRemoved non implémentée !")
        }
        var onEntityAdded: (entity: Entity) -> Unit = {
            throw Exception("Méthode onEntityAdded non implémentée !")
        }
        var onAddScore: (addScore: Int) -> Unit = {
            throw Exception("Méthode onAddScore non implémentée !")
        }
        var onEndLevel: (levelSuccess: Boolean) -> Unit = {
            throw Exception("Méthode onEndLevel non implémentée !")
        }

        /**
         * Permet de supprimer une entité du niveau(dynamiquement)
         * @param entity L'entité à supprimer dynamiquement du niveau
         */
        fun removeEntity(entity: Entity) {
            onEntityRemoved(entity)
        }

        /**
         * Permet d'ajouter une entité au niveau(dynamiquement)
         * @param entity L'entité à ajouter dynamiquement au niveau
         */
        fun addEntity(entity: Entity) {
            onEntityAdded(entity)
        }

        /**
         * Permet d'ajouter des points de score au joueur
         * @param score Le score à ajouter au joueur
         */
        fun addScore(score: Int) {
            onAddScore(score)
        }

        /**
         * Permet de finir le niveau
         * @param success Permet de spécifié si le niveau est réussi ou perdu
         */
        fun endLevel(success: Boolean) {
            onEndLevel(success)
        }
    }
}

