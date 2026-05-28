package com.operit.wechatcontact.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.operit.wechatcontact.R
import com.operit.wechatcontact.databinding.FragmentStatsBinding
import com.operit.wechatcontact.ui.viewmodel.ContactViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ContactViewModel by viewModels<MainActivity>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.stats.collectLatest { stats ->
                val total = stats.total ?: 0
                val pending = stats.pending ?: 0
                val applied = stats.applied ?: 0
                val accepted = stats.accepted ?: 0
                val rejected = stats.rejected ?: 0
                val failed = stats.failed ?: 0

                binding.tvTotal.text = total.toString()
                binding.tvPending.text = pending.toString()
                binding.tvApplied.text = applied.toString()
                binding.tvAccepted.text = accepted.toString()
                binding.tvRejected.text = rejected.toString()
                binding.tvFailed.text = failed.toString()

                val rate = if (total > 0) "${(accepted * 100 / total)}%" else "0%"
                binding.tvRate.text = rate
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}