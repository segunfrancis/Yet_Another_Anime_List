package com.sanmidev.yetanotheranimelist.data.local.dao

import androidx.room.*
import com.sanmidev.yetanotheranimelist.data.local.model.animelist.AnimeEntity
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface FavouriteAnimeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun favouriteAnime(favouriteAnime: AnimeEntity) : Completable

    @Delete
    fun unFavouriteAnime(favouriteAnime: AnimeEntity) : Completable

    @Query("SELECT COUNT (*) FROM anime_entity_table WHERE id = :malID ")
    fun getAnime(malID : Int): Single<Int>

    @Query("SELECT * FROM anime_entity_table")
    fun getAnimeList() : Observable<List<AnimeEntity>>
}
