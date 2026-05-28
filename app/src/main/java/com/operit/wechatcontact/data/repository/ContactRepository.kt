package com.operit.wechatcontact.data.repository

import com.operit.wechatcontact.data.dao.ContactDao
import com.operit.wechatcontact.data.model.*
import kotlinx.coroutines.flow.Flow

class ContactRepository(private val dao: ContactDao) {

    fun getContacts(status: String?, keyword: String?): Flow<List<Contact>> =
        dao.getContacts(status, keyword)

    fun getAllContacts(): Flow<List<Contact>> = dao.getAllContacts()

    suspend fun getContactById(id: Int): Contact? = dao.getContactById(id)

    suspend fun getContactWithIdentifiers(id: Int): ContactWithIdentifiers? {
        val contact = dao.getContactById(id) ?: return null
        val identifiers = dao.getIdentifiersByContactId(id)
        return ContactWithIdentifiers(contact, identifiers)
    }

    suspend fun insertContact(contact: Contact): Long = dao.insertContact(contact)

    suspend fun updateContact(contact: Contact) = dao.updateContact(contact)

    suspend fun deleteContact(contact: Contact) = dao.deleteContact(contact)

    suspend fun getIdentifiersByContactId(contactId: Int): List<ContactIdentifier> =
        dao.getIdentifiersByContactId(contactId)

    suspend fun findIdentifier(type: IdentifierType, value: String): ContactIdentifier? =
        dao.findIdentifier(type, value)

    suspend fun insertIdentifier(identifier: ContactIdentifier): Long =
        dao.insertIdentifier(identifier)

    suspend fun insertLog(log: OperationLog) = dao.insertLog(log)

    suspend fun getLogsByContactId(contactId: Int): List<OperationLog> =
        dao.getLogsByContactId(contactId)

    suspend fun getStats(): StatsRow = dao.getStats()

    suspend fun batchInsertContactWithIdentifiers(
        contact: Contact,
        identifiers: List<ContactIdentifier>
    ): Long = dao.batchInsertContactWithIdentifiers(contact, identifiers)
}