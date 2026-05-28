package com.operit.wechatcontact.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.operit.wechatcontact.data.dao.StatsRow
import com.operit.wechatcontact.data.importer.ContactImporter
import com.operit.wechatcontact.data.model.*
import com.operit.wechatcontact.data.repository.ContactRepository
import com.operit.wechatcontact.util.IdentifierUtil
import com.operit.wechatcontact.util.WechatUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactViewModel @Inject constructor(
    private val repository: ContactRepository,
    private val importer: ContactImporter,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _filterStatus = MutableStateFlow<String?>(null)
    private val _searchKeyword = MutableStateFlow<String?>(null)

    val contacts: StateFlow<List<Contact>> = combine(
        _filterStatus, _searchKeyword
    ) { status, keyword -> Pair(status, keyword) }
        .flatMapLatest { (status, keyword) ->
            repository.getContacts(status, keyword)
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val stats: StateFlow<StatsRow> = flow {
        emit(repository.getStats())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, StatsRow())

    private val _importResult = MutableStateFlow<ImportResultData?>(null)
    val importResult: StateFlow<ImportResultData?> = _importResult

    private val _currentContact = MutableStateFlow<Contact?>(null)
    val currentContact: StateFlow<Contact?> = _currentContact

    fun setFilterStatus(status: String?) {
        _filterStatus.value = status
    }

    fun setSearchKeyword(keyword: String?) {
        _searchKeyword.value = if (keyword.isNullOrBlank()) null else keyword
    }

    fun importFromPhonebook() {
        viewModelScope.launch {
            _importResult.value = importer.importFromPhonebook()
        }
    }

    fun importFromText(text: String) {
        viewModelScope.launch {
            _importResult.value = importer.importFromText(text)
        }
    }

    fun clearImportResult() {
        _importResult.value = null
    }

    fun setCurrentContact(contact: Contact?) {
        _currentContact.value = contact
    }

    fun launchWechat(contactId: Int) {
        viewModelScope.launch {
            val withId = repository.getContactWithIdentifiers(contactId)
            val primaryId = withId?.identifiers?.firstOrNull { it.isPrimary }
            if (primaryId != null) {
                _currentContact.value = withId.contact
                WechatUtil.launchWechat(context, primaryId.value)
            }
        }
    }

    fun updateStatus(contactId: Int, newStatus: ContactStatus) {
        viewModelScope.launch {
            val contact = repository.getContactById(contactId) ?: return@launch
            val fromStatus = contact.status.name
            val updated = contact.copy(
                status = newStatus,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateContact(updated)
            repository.insertLog(OperationLog(
                contactId = contactId,
                fromStatus = fromStatus,
                toStatus = newStatus.name
            ))
        }
    }

    fun deleteContact(contact: Contact) {
        viewModelScope.launch {
            repository.deleteContact(contact)
        }
    }

    fun getContactDetail(contactId: Int): Flow<ContactWithIdentifiers?> {
        return flow {
            emit(repository.getContactWithIdentifiers(contactId))
        }
    }

    fun getOperationLogs(contactId: Int): Flow<List<OperationLog>> {
        return flow {
            emit(repository.getLogsByContactId(contactId))
        }
    }
}