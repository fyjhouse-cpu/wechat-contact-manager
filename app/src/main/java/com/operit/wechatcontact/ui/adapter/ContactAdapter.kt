package com.operit.wechatcontact.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.operit.wechatcontact.R
import com.operit.wechatcontact.data.model.Contact
import com.operit.wechatcontact.data.model.ContactStatus
import com.operit.wechatcontact.databinding.ItemContactBinding
import com.operit.wechatcontact.util.IdentifierUtil

class ContactAdapter(
    private val onItemClick: (Contact) -> Unit,
    private val onAddClick: (Contact) -> Unit,
    private val onStatusChange: (Int, ContactStatus) -> Unit
) : ListAdapter<Contact, ContactAdapter.ViewHolder>(ContactDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemContactBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemContactBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: Contact) {
            binding.tvName.text = if (contact.name.isNotBlank()) contact.name else "未命名"
            binding.tvSource.text = contact.source.ifBlank { "手动添加" }
            binding.tvTime.text = IdentifierUtil.formatRelativeTime(contact.updatedAt)
            binding.tvStatus.text = contact.status.label

            val statusColor = ContextCompat.getColor(
                binding.root.context,
                getStatusColorRes(contact.status)
            )
            binding.ivStatusDot.setBackgroundColor(statusColor)
            binding.tvStatus.setTextColor(statusColor)

            if (contact.status != ContactStatus.PENDING) {
                binding.root.alpha = 0.65f
            } else {
                binding.root.alpha = 1.0f
            }

            binding.root.setOnClickListener { onItemClick(contact) }
            binding.btnAction.setOnClickListener {
                when (contact.status) {
                    ContactStatus.PENDING -> onAddClick(contact)
                    ContactStatus.APPLIED -> onStatusChange(contact.id, ContactStatus.ACCEPTED)
                    ContactStatus.ACCEPTED -> onAddClick(contact)
                    ContactStatus.REJECTED -> onStatusChange(contact.id, ContactStatus.PENDING)
                    ContactStatus.FAILED -> onStatusChange(contact.id, ContactStatus.PENDING)
                }
            }
            binding.btnAction.text = getActionText(contact.status)
        }

        private fun getStatusColorRes(status: ContactStatus): Int {
            return when (status) {
                ContactStatus.PENDING -> R.color.status_pending
                ContactStatus.APPLIED -> R.color.status_applied
                ContactStatus.ACCEPTED -> R.color.status_accepted
                ContactStatus.REJECTED -> R.color.status_rejected
                ContactStatus.FAILED -> R.color.status_failed
            }
        }

        private fun getActionText(status: ContactStatus): String {
            return when (status) {
                ContactStatus.PENDING -> "去添加"
                ContactStatus.APPLIED -> "标记通过"
                ContactStatus.ACCEPTED -> "查看"
                ContactStatus.REJECTED -> "重试"
                ContactStatus.FAILED -> "重试"
            }
        }
    }

    class ContactDiffCallback : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem == newItem
        }
    }
}