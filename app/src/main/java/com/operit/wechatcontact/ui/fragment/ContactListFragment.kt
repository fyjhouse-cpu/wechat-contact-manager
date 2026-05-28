package com.operit.wechatcontact.ui.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.operit.wechatcontact.R
import com.operit.wechatcontact.data.model.Contact
import com.operit.wechatcontact.data.model.ContactStatus
import com.operit.wechatcontact.data.model.ImportResultData
import com.operit.wechatcontact.databinding.FragmentContactListBinding
import com.operit.wechatcontact.ui.adapter.ContactAdapter
import com.operit.wechatcontact.ui.viewmodel.ContactViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale

@AndroidEntryPoint
class ContactListFragment : Fragment() {

    private var _binding: FragmentContactListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ContactViewModel by viewModels<MainActivity>()
    private lateinit var adapter: ContactAdapter

    private val requestContactsPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.importFromPhonebook()
        } else {
            Toast.makeText(requireContext(), "需要通讯录权限才能导入", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        setupFilterChips()
        setupFab()
        observeContacts()
        observeImportResult()
    }

    private fun setupRecyclerView() {
        adapter = ContactAdapter(
            onItemClick = { contact -> showContactDetail(contact) },
            onAddClick = { contact -> viewModel.launchWechat(contact.id) },
            onStatusChange = { id, status -> viewModel.updateStatus(id, status) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.setOnEditorActionListener { _, _, _ ->
            val text = binding.etSearch.text?.toString()?.trim()
            viewModel.setSearchKeyword(text)
            false
        }
    }

    private fun setupFilterChips() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) {
                viewModel.setFilterStatus(null)
            } else {
                val chip = group.findViewById<Chip>(checkedIds[0])
                val status = when (chip.text.toString()) {
                    "全部" -> null
                    "待添加" -> ContactStatus.PENDING.name
                    "已申请" -> ContactStatus.APPLIED.name
                    "已通过" -> ContactStatus.ACCEPTED.name
                    "被拒绝" -> ContactStatus.REJECTED.name
                    "失败" -> ContactStatus.FAILED.name
                    else -> null
                }
                viewModel.setFilterStatus(status)
            }
        }
        binding.chipGroupFilter.check(R.id.chip_all)
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            showImportDialog()
        }
    }

    private fun showImportDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("导入联系人")
            .setItems(
                arrayOf("从通讯录导入", "粘贴号码/微信号")
            ) { _, which ->
                when (which) {
                    0 -> importFromPhonebook()
                    1 -> showPasteDialog()
                }
            }
            .show()
    }

    private fun importFromPhonebook() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.importFromPhonebook()
        } else {
            requestContactsPermission.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    private fun showPasteDialog() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle("粘贴号码/微信号")
        val input = android.widget.EditText(requireContext())
        input.hint = "每行一个，格式：姓名 号码/微信号\n如：张三 13800138000\n或：李四 JackLiu88"
        input.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
        input.minLines = 6
        builder.setView(input)
        builder.setPositiveButton("导入") { _, _ ->
            val text = input.text?.toString()?.trim()
            if (text.isNullOrBlank()) {
                Toast.makeText(requireContext(), "请输入内容", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.importFromText(text)
            }
        }
        builder.setNegativeButton("取消", null)
        builder.show()
    }

    private fun observeContacts() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.contacts.collectLatest { list ->
                adapter.submitList(list)
                binding.tvEmpty.isVisible = list.isEmpty()
                binding.recyclerView.isVisible = list.isNotEmpty()
                updateBottomBar(list)
            }
        }
    }

    private fun observeImportResult() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.importResult.collectLatest { result ->
                result?.let {
                    showImportResult(it)
                    viewModel.clearImportResult()
                }
            }
        }
    }

    private fun showImportResult(result: ImportResultData) {
        val message = StringBuilder()
        message.append("新增: ${result.inserted} 条\n")
        message.append("跳过(已存在): ${result.skipped} 条\n")
        if (result.invalid > 0) {
            message.append("无效: ${result.invalid} 条")
        }
        Snackbar.make(binding.root, message.toString(), Snackbar.LENGTH_LONG).show()
    }

    private fun updateBottomBar(list: List<Contact>) {
        val pending = list.count { it.status == ContactStatus.PENDING }
        val accepted = list.count { it.status == ContactStatus.ACCEPTED }
        val rate = if (list.isNotEmpty()) "${(accepted * 100 / list.size)}%" else "0%"
        binding.tvBottomSummary.text = "共 ${list.size} 人  ·  待添加 $pending  ·  通过率 $rate"
    }

    private fun showContactDetail(contact: Contact) {
        val detailFragment = ContactDetailFragment.newInstance(contact.id)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, detailFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}