package com.operit.wechatcontact.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.operit.wechatcontact.R
import com.operit.wechatcontact.databinding.FragmentContactDetailBinding
import com.operit.wechatcontact.data.model.ContactStatus
import com.operit.wechatcontact.ui.viewmodel.ContactViewModel
import com.operit.wechatcontact.util.IdentifierUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ContactDetailFragment : Fragment() {

    private var _binding: FragmentContactDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ContactViewModel by viewModels<MainActivity>()
    private var contactId: Int = 0

    companion object {
        private const val ARG_CONTACT_ID = "contact_id"
        fun newInstance(contactId: Int): ContactDetailFragment {
            return ContactDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_CONTACT_ID, contactId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contactId = arguments?.getInt(ARG_CONTACT_ID, 0) ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.tvDetailTitle.text = "联系人详情"

        loadDetail()
        setupStatusButtons()
    }

    private fun loadDetail() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.getContactDetail(contactId).collectLatest { withId ->
                withId?.let {
                    val contact = it.contact
                    binding.tvDetailName.text = contact.name.ifBlank { "未命名" }
                    binding.tvDetailSource.text = contact.source.ifBlank { "手动添加" }
                    binding.tvDetailStatus.text = contact.status.label
                    binding.tvDetailTime.text = IdentifierUtil.formatTimestamp(contact.updatedAt)
                    binding.tvDetailRemark.text = contact.remark.ifBlank { "无备注" }

                    val identifiersText = it.identifiers.joinToString("\n") { id ->
                        val typeText = if (id.type == com.operit.wechatcontact.data.model.IdentifierType.PHONE) "📱" else "💬"
                        val displayValue = if (id.type == com.operit.wechatcontact.data.model.IdentifierType.PHONE) {
                            IdentifierUtil.maskPhone(id.value)
                        } else {
                            id.value
                        }
                        "$typeText $displayValue"
                    }
                    binding.tvDetailIdentifiers.text = identifiersText
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.getOperationLogs(contactId).collectLatest { logs ->
                if (logs.isEmpty()) {
                    binding.tvLogList.text = "暂无操作记录"
                } else {
                    val logText = logs.joinToString("\n\n") { log ->
                        val time = IdentifierUtil.formatTimestamp(log.timestamp)
                        val from = if (log.fromStatus.isNotBlank()) log.fromStatus.replace("_", " ") else "新建"
                        val to = log.toStatus.replace("_", " ")
                        "$time\n$from → $to"
                    }
                    binding.tvLogList.text = logText
                }
            }
        }
    }

    private fun setupStatusButtons() {
        binding.btnStatusApplied.setOnClickListener {
            viewModel.updateStatus(contactId, ContactStatus.APPLIED)
        }
        binding.btnStatusAccepted.setOnClickListener {
            viewModel.updateStatus(contactId, ContactStatus.ACCEPTED)
        }
        binding.btnStatusRejected.setOnClickListener {
            viewModel.updateStatus(contactId, ContactStatus.REJECTED)
        }
        binding.btnStatusPending.setOnClickListener {
            viewModel.updateStatus(contactId, ContactStatus.PENDING)
        }
        binding.btnStatusFailed.setOnClickListener {
            viewModel.updateStatus(contactId, ContactStatus.FAILED)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}