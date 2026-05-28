package com.operit.wechatcontact

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.operit.wechatcontact.data.model.ContactStatus
import com.operit.wechatcontact.databinding.ActivityMainBinding
import com.operit.wechatcontact.ui.fragment.ContactListFragment
import com.operit.wechatcontact.ui.fragment.StatsFragment
import com.operit.wechatcontact.ui.fragment.SettingsFragment
import com.operit.wechatcontact.ui.viewmodel.ContactViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: ContactViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNav()
        showFragment(ContactListFragment())

        observeCurrentContact()
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_list -> {
                    showFragment(ContactListFragment())
                    true
                }
                R.id.nav_stats -> {
                    showFragment(StatsFragment())
                    true
                }
                R.id.nav_settings -> {
                    showFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun showFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun observeCurrentContact() {
        lifecycleScope.launch {
            viewModel.currentContact.collectLatest { contact ->
                if (contact != null) {
                    showQuickMarkBanner(contact)
                }
            }
        }
    }

    private fun showQuickMarkBanner(contact: com.operit.wechatcontact.data.model.Contact) {
        binding.bannerQuickMark.isVisible = true
        binding.bannerQuickMark.tvBannerName.text = contact.name.ifBlank { "联系人" }

        binding.bannerQuickMark.btnMarkApplied.setOnClickListener {
            viewModel.updateStatus(contact.id, ContactStatus.APPLIED)
            binding.bannerQuickMark.isVisible = false
        }
        binding.bannerQuickMark.btnMarkAccepted.setOnClickListener {
            viewModel.updateStatus(contact.id, ContactStatus.ACCEPTED)
            binding.bannerQuickMark.isVisible = false
        }
        binding.bannerQuickMark.btnMarkRejected.setOnClickListener {
            viewModel.updateStatus(contact.id, ContactStatus.REJECTED)
            binding.bannerQuickMark.isVisible = false
        }
        binding.bannerQuickMark.btnCloseBanner.setOnClickListener {
            binding.bannerQuickMark.isVisible = false
        }
    }
}