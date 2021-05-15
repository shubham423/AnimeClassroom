package divyansh.tech.animeclassroom.animeDetail

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import divyansh.tech.animeclassroom.R
import divyansh.tech.animeclassroom.ResultWrapper
import divyansh.tech.animeclassroom.animeDetail.callbacks.EpisodeItemClickCallback
import divyansh.tech.animeclassroom.animeDetail.epoxy.EpoxyEpisodeController
import divyansh.tech.animeclassroom.databinding.FragmentAnimeDetailsBinding
import divyansh.tech.animeclassroom.models.home.AnimeDetailModel

@AndroidEntryPoint
class AnimeDetailFragment: Fragment() {

    private lateinit var binding: FragmentAnimeDetailsBinding
    private val animeUrl: AnimeDetailFragmentArgs by navArgs()
    private val viewModel by viewModels<AnimeDetailViewModel>()
    private val episodeController by lazy { EpoxyEpisodeController(EpisodeItemClickCallback(viewModel)) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAnimeDetailsBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupRecyclerView()
    }

    private fun setupObservers() {
        viewModel.animeDetailLiveData.observe(
            viewLifecycleOwner,
            Observer {
                when (it) {
                    is ResultWrapper.Success -> {
                        Log.i("HOME", it.data.toString())
                        updateViews(it.data!!)
//                        binding.anime = it.data
                        binding.motionLayout.visibility = View.VISIBLE
                    }
                    is ResultWrapper.Error -> {}
                    is ResultWrapper.Loading -> {
                        binding.motionLayout.visibility = View.GONE
                    }
                }
            }
        )

        viewModel.episodeListLiveData.observe(
            viewLifecycleOwner,
            Observer {
                when (it) {
                    is ResultWrapper.Success -> episodeController.setData(it.data)
                    is ResultWrapper.Loading -> {}
                    is ResultWrapper.Error -> {}
                }
            }
        )

        viewModel.navigation.observe(
            viewLifecycleOwner,
            Observer {
                findNavController().navigate(it)
            }
        )
    }

    // TODO:: Find a more efficient way to update views
    private fun updateViews(animeDetailModel: AnimeDetailModel) {
        binding.apply {
            Glide.with(requireContext()).load(animeDetailModel.imageUrl).into(binding.animeInfoImage)
            expandableText.text = animeDetailModel.name
            animeInfoTitle.text = animeDetailModel.name
            animeInfoType.text = animeDetailModel.type
            animeInfoStatus.text = animeDetailModel.status
            animeInfoReleased.text = animeDetailModel.releaseDate
            animeInfoSummary.text = animeDetailModel.plotSummary
            back.setOnClickListener { onBackPressed() }
        }
    }

    private fun setupRecyclerView() {
        binding.episodeRv.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = episodeController.adapter
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getAnimeDetails(animeUrl.animeUrl)
    }

    //TODO: Fix the navigation issue. Fragmetn getting recreating on popBackstack()
    private fun onBackPressed() {
        findNavController().popBackStack()
    }


}