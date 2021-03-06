package com.sanmidev.yetanotheranimelist.data.network.repo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.github.javafaker.Faker
import com.google.common.truth.Truth
import com.sanmidev.yetanotheranimelist.DataUtils
import com.sanmidev.yetanotheranimelist.RepoUtils
import com.sanmidev.yetanotheranimelist.data.local.dao.FavouriteAnimeDao
import com.sanmidev.yetanotheranimelist.data.local.db.FavouriteAnimeDatabase
import com.sanmidev.yetanotheranimelist.data.local.model.animelist.AnimeEntity
import com.sanmidev.yetanotheranimelist.data.local.model.favourite.FavouriteAnimeResult
import com.sanmidev.yetanotheranimelist.data.local.repo.FavouriteAnimeRepostoryImpl
import com.sanmidev.yetanotheranimelist.data.network.model.animedetail.AnimeDetailResult
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FavouriteAnimeRepostoryImplTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testAppScheduler = RepoUtils.ProvideTestScheduler()

    private var db: FavouriteAnimeDatabase? = null

    private var favouriteAnimeDao: FavouriteAnimeDao? = null

    private lateinit var SUT: FavouriteAnimeRepostoryImpl

    private val faker = Faker()

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            FavouriteAnimeDatabase::class.java
        ).build()

        favouriteAnimeDao = db!!.favouriteDao()

        SUT =
            FavouriteAnimeRepostoryImpl(
                (favouriteAnimeDao!!),
                testAppScheduler
            )
    }

    @After
    fun tearDown() {
        db!!.close()
    }

    @Test
    fun favouriteAnime() {
    }

    @Test
    fun isFavourite_shouldReturnTrue_whenAnimeHasBeenFavourited() {

        //GIVEN
        val newFavouriteAnime = AnimeEntity("http:/image.jpg", 3456, "My name is anime")
        favouriteAnimeDao?.favouriteAnime(newFavouriteAnime)!!.test()

        //WHEN
        val isFavourite = SUT.hasBeenSaved(3456).test()

        //THEN
        isFavourite.assertValue(true)


    }

    @Test
    fun isFavourite_shouldReturnFalse_whenAnimeHasNotBeenFavourited() {
        //GIVEN
        val newFavouriteAnime = AnimeEntity("http:/image.jpg", 34568, "My name is anime")
        favouriteAnimeDao?.favouriteAnime(newFavouriteAnime)!!.test()


        //WHEN
        val isFavourite = SUT.hasBeenSaved(3456).test()


        //THEN
        isFavourite.assertValue(false)

    }


    @Test
    fun favouriteAnime_shouldReturnSuccess_whenAnimeHasNotBeenFavourired() {

        val newFavouriteAnime = DataUtils.generateAnimeDetailData(faker).second

        //WHEN
        val isFavourited =
            SUT.favouriteAnime(AnimeDetailResult.Success(newFavouriteAnime), false).test()
        val result = isFavourited.values()[0]
        //THEN
        isFavourited.assertNoErrors()
        Truth.assertThat(result).isInstanceOf(FavouriteAnimeResult.Favourited::class.java)

    }


    @Test
    fun favouriteAnime_shouldReturnUnFavourited_whenAnimeHasBeenFavourited() {

        val newFavouriteAnime = DataUtils.generateAnimeDetailData(faker).second

        //WHEN
        val isFavourited = SUT.favouriteAnime(AnimeDetailResult.Success(newFavouriteAnime), true).test()
        val result = isFavourited.values()[0]
        //THEN
        Truth.assertThat(result).isInstanceOf(FavouriteAnimeResult.UnFavourited::class.java)

    }

    @Test
    fun getFavouriteAnimeList_shouldReturnFavouriteAnimes_whenAnimesHasBeenFavourited(){
        //GIVEN
        val listOfFavouriteAnimes = favouriteAnimeList()

        //WHEN
        val favouriteAnimeListFromDB = SUT.getAnimeList().test().values()[0]

        //THEN
        Truth.assertThat(favouriteAnimeListFromDB).containsExactlyElementsIn(listOfFavouriteAnimes)
    }


    @Test
    fun getFavouriteAnimeList_shouldReturnEmpty_whenAnimesHasBeenFavourited(){

        //WHEN
        val favouriteAnimeListFromDB = SUT.getAnimeList().test().values()[0]

        //THEN
        Truth.assertThat(favouriteAnimeListFromDB).containsExactlyElementsIn(emptyList<AnimeEntity>())
    }
    private fun favouriteAnimeList(): List<AnimeEntity> {

        val newFavouriteAnime1 = AnimeEntity("http:/image.jpg", 34561, "My name is anime")
        favouriteAnimeDao?.favouriteAnime(newFavouriteAnime1)!!.test()

        val newFavouriteAnime2 = AnimeEntity("http:/image1.jpg", 34362, "My name is anime1")
        favouriteAnimeDao?.favouriteAnime(newFavouriteAnime2)!!.test()

        val newFavouriteAnime = AnimeEntity("http:/image2.jpg", 34532, "My name is anime2")
        favouriteAnimeDao?.favouriteAnime(newFavouriteAnime)!!.test()

        val listOfFavouriteAnimes =
            listOf<AnimeEntity>(newFavouriteAnime, newFavouriteAnime1, newFavouriteAnime2)
        return listOfFavouriteAnimes
    }
}