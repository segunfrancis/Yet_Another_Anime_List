package com.sanmidev.yetanotheranimelist.feature.airingAnimes

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sanmidev.yetanotheranimelist.MainActivity
import com.sanmidev.yetanotheranimelist.R
import com.sanmidev.yetanotheranimelist.data.local.model.animelist.AnimeEntity
import com.sanmidev.yetanotheranimelist.data.network.model.animelist.AnimeListResult
import com.sanmidev.yetanotheranimelist.databinding.FragmentAiringBinding
import com.sanmidev.yetanotheranimelist.feature.common.recyclerview.AnimeListAdapter
import com.sanmidev.yetanotheranimelist.utils.ui.initEndlessScrollListener
import com.sanmidev.yetanotheranimelist.utils.ui.navigateSafely
import com.sanmidev.yetanotheranimelist.utils.ui.showIf
import io.cabriole.decorator.ColumnProvider
import io.cabriole.decorator.GridMarginDecoration
import timber.log.Timber
import javax.inject.Inject

typealias AnimeDetailOnClick = (AnimeEntity) -> Unit

class AiringFragment : Fragment(R.layout.fragment_airing) {


    private var animeListAdaper: AnimeListAdapter? = null

    @Inject
    lateinit var viewModelFactory: AiringViewModel.VMFactory

    private var fragmentAiringBinding: FragmentAiringBinding? = null


    val binding: FragmentAiringBinding
        get() = fragmentAiringBinding!!


    private val viewModel by viewModels<AiringViewModel> { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentAiringBinding = FragmentAiringBinding.bind(view)

        initRecyclerView()
        initToolBar()
        observeNextAnimeList()

    }

    private fun initToolBar() {
        binding.include.toolbar.title = getString(R.string.trending_anime_txt)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        (activity as MainActivity).activityComponent.inject(this)
    }


    private fun initRecyclerView() {
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)

        //init adapter
        animeListAdaper = AnimeListAdapter(this.requireContext())
        val animePictureClickListener: AnimeDetailOnClick = { animeEntity: AnimeEntity ->
            val directions =
                AiringFragmentDirections.actionTrendingFragmentToAnimeDetailFragment(
                    animeEntity.id,
                    animeEntity.imageUrl,
                    animeEntity.title
                )

            findNavController().navigateSafely(directions, R.id.trendingFragment)
        }
        animeListAdaper?.setAnimeImageClickListener(animePictureClickListener)

        //init recyclerview
        binding.rvAiring.apply {
            this.setHasFixedSize(true)
            this.adapter = animeListAdaper
            this.layoutManager = gridLayoutManager

            this.addItemDecoration(
                GridMarginDecoration(
                    margin = resources.getDimensionPixelSize(R.dimen.offset_size),
                    columnProvider = object : ColumnProvider {
                        override fun getNumberOfColumns(): Int = 2
                    },
                    orientation = RecyclerView.VERTICAL
                )
            )

            initEndlessScrollListener { viewModel.getNextAiringAnime() }
        }

        observerGetUpComingAnimeNetworkState()
    }

    private fun observerGetUpComingAnimeNetworkState() {

        viewModel.airingLiveData.observe(viewLifecycleOwner) { animeListResult ->

            binding.pbAiring.showIf { animeListResult == AnimeListResult.Loading }

            when (animeListResult) {
                is AnimeListResult.Success -> {
                    animeListAdaper?.submitList(viewModel.animeListData)
                }

                is AnimeListResult.APIerror -> {

                    val apiError = animeListResult.jikanErrorRespone
                    Timber.d(apiError.toString())
                }
                is AnimeListResult.Exception -> {

                    Toast.makeText(requireContext(), animeListResult.message, Toast.LENGTH_SHORT)
                        .show()

                }
            }
        }
    }

    private fun observeNextAnimeList() {
        viewModel.nextAiringLiveData.observe(viewLifecycleOwner) { animeListResult ->

            binding.pbAiring.showIf { animeListResult == AnimeListResult.Loading }

            when (animeListResult) {

                is AnimeListResult.Success -> {

                    animeListAdaper?.submitList(viewModel.animeListData)
                }

                is AnimeListResult.APIerror -> {

                    val apiError = animeListResult.jikanErrorRespone
                    if (apiError.message != getString(R.string.res_does_not_exist)) {
                        Timber.d(apiError.message)
                    }
                }
                is AnimeListResult.Exception -> {

                    Toast.makeText(requireContext(), animeListResult.message, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    override fun onDestroyView() {
        animeListAdaper = null
        super.onDestroyView()
    }
}
