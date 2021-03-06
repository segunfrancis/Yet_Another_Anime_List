package com.sanmidev.yetanotheranimelist.feature.animeDetail

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.chip.Chip
import com.sanmidev.yetanotheranimelist.MainActivity
import com.sanmidev.yetanotheranimelist.R
import com.sanmidev.yetanotheranimelist.data.local.model.favourite.FavouriteAnimeResult
import com.sanmidev.yetanotheranimelist.data.network.model.animedetail.AnimeDetailResult
import com.sanmidev.yetanotheranimelist.databinding.AnimeDetailFragmentBinding
import com.sanmidev.yetanotheranimelist.di.module.GlideApp
import com.sanmidev.yetanotheranimelist.utils.ui.fireToast
import com.sanmidev.yetanotheranimelist.utils.ui.initToolbarButton
import com.sanmidev.yetanotheranimelist.utils.ui.showIf
import javax.inject.Inject


class AnimeDetailFragment : Fragment(R.layout.anime_detail_fragment) {

    private var detailFragmentBinding: AnimeDetailFragmentBinding? = null


    val binding: AnimeDetailFragmentBinding
        get() = detailFragmentBinding!!

    @Inject
    lateinit var savedStateViewModelFactory: AnimeDetailViewModel.VmFactory.Factory

    private val args by navArgs<AnimeDetailFragmentArgs>()

    private val viewModel by lazy {
        //GET ARGUMENT FROM PREVIOUS FRAGMENT
        val bundle = Bundle()
        val malId = args.malId
        bundle.putInt(DETAIL_ANIME_ID_KEY, malId)

        //GET VIEWMODEL
        ViewModelProvider(this, savedStateViewModelFactory.createFactory(this, bundle)).get(
            AnimeDetailViewModel::class.java
        )
    }




    override fun onAttach(context: Context) {
        super.onAttach(context)

        (activity as MainActivity).activityComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        detailFragmentBinding = AnimeDetailFragmentBinding.bind(view)

        GlideApp.with(this).load(args.imageUrl)
            .into(binding.imgAnimePicture)
        tweakCollaspingToolbar()

        initObserveGetDetailState()
        initObserveIsFavouriteState()
        initOnclickListeners()

    }

    /***
     *  OBserves the network state of get anime detail from the api.
     */
    private fun initObserveGetDetailState() {
        viewModel.animeDetailResultState.observe(viewLifecycleOwner) { animeDetailResult ->

            binding.pbAnimeDetail.showIf { animeDetailResult is AnimeDetailResult.Loading }
            binding.floatingActionButton.showIf { animeDetailResult is AnimeDetailResult.Success }

            when (animeDetailResult) {
                is AnimeDetailResult.Success -> {

                    bindSuccessData(animeDetailResult)
                    viewModel.hasBeenFavourited()
                }
                is AnimeDetailResult.APIerror -> {

                    fireToast(
                        requireContext(),
                        animeDetailResult.jikanErrorRespone.message
                    )
                }
                is AnimeDetailResult.Exception -> {
                    fireToast(
                        requireContext(),
                        animeDetailResult.message
                    )
                }
            }
        }
    }

    private fun initOnclickListeners() {
        binding.floatingActionButton.setOnClickListener {

            viewModel.favouriteAnime()
        }
    }


    private fun initObserveIsFavouriteState() {
        viewModel.isFavourited.observe(viewLifecycleOwner) { result ->

            binding.floatingActionButton.setImageDrawable(
                when (result) {
                    FavouriteAnimeResult.Favourited -> {
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_favorite_24dp
                        )
                    }
                    FavouriteAnimeResult.UnFavourited -> {
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_favorite_unfav_24dp
                        )
                    }
                    is FavouriteAnimeResult.Error -> {
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_favorite_unfav_24dp
                        )
                    }
                }
            )

        }
    }

    /***
     * Used to tweak collasping toolbar so the title of the anime only shows when the toolbar is collasped.
     */
    private fun tweakCollaspingToolbar(){
        var isShow = true
        var scrollRange = -1
        binding.appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { barLayout, verticalOffset ->
            if (scrollRange == -1){
                scrollRange = barLayout?.totalScrollRange!!
            }
            if (scrollRange + verticalOffset == 0){
                binding.collapsingToolBar.title = args.title
                binding.AppBar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
                isShow = true
            } else if (isShow) {
                binding.AppBar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
                binding.collapsingToolBar.title =
                    " " //careful there should a space between double quote otherwise it wont work
                isShow = false
            }
        })

        initToolbarButton(
            requireActivity(),
            binding.AppBar
        )
    }

    /***
     * Binds the success data recieved from the Jikan API
     * [animeDetailResult] is the success result from the API.
     */
    private fun bindSuccessData(animeDetailResult: AnimeDetailResult.Success) {
        binding.txtSynopsis.text = animeDetailResult.data.synopsis

       val genres =  viewModel.processGenre(animeDetailResult.data.genreEntity)

        genres.forEach { title ->
            createChip(title)
        }
    }

    private fun createChip(tag: String) {
        val chip =
            this.layoutInflater.inflate(R.layout.genre_chip, null, false) as Chip
        chip.text = tag
        val paddingDp = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 10f,
            resources.displayMetrics
        ).toInt()

        val generatedId = View.generateViewId()
        // All tag should be pre selected.
        chip.id = generatedId

        chip.setPadding(paddingDp, 0, paddingDp, 0)
        binding.genreChipGroup.addView(chip)


    }


    override fun onDestroyView() {
        detailFragmentBinding = null
        super.onDestroyView()
    }

    companion object {
        const val DETAIL_ANIME_ID_KEY =
            "com.sanmidev.yetanotheranimelist.animeDetailFragment.anime_detail_key"
    }
}
