package com.operit.wechatcontact.data.dao

import androidx.room.*
import com.operit.wechatcontact.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Transaction
    @Query("""
        SELECT * FROM contacts
        WHERE (:status IS NULL OR status = :status)
          AND (:keyword IS NULL OR name LIKE '%' || :keyword || '%'
               OR id IN (SELECT contactId FROM contact_identifiers WHERE value LIKE '%' || :keyword || '%'))
        ORDER BY
            CASE status
                WHEN 'PENDING' THEN 0
                WHEN 'APPLIED' THEN 1
                WHEN 'ACCEPTED' THEN 2
                WHEN 'REJECTED' THEN 3
                WHEN 'FAILED' THEN 4
            END,
            updatedAt DESC
    """)
    fun getContacts(status: String?, keyword: String?): Flow<List<Contact>>

    @Query("SELECT * FROM contacts ORDER BY updatedAt DESC")
    fun getAllContacts(): Flow<List<Contact>>

    @Query("SELECT * FROM contacts WHERE id = :id")
    suspend fun getContactById(id: Int): Contact?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertContact(contact: Contact): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIdentifier(identifier: ContactIdentifier): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIdentifiers(identifiers: List<ContactIdentifier>): List<Long>

    @Update
    suspend fun updateContact(contact: Contact)

    @Delete
    suspend fun deleteContact(contact: Contact)

    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun deleteContactById(id: Int)

    @Query("SELECT * FROM contact_identifiers WHERE contactId = :contactId ORDER BY isPrimary DESC")
    suspend fun getIdentifiersByContactId(contactId: Int): List<ContactIdentifier>

    @Query("SELECT * FROM contact_identifiers WHERE type = :type AND value = :value LIMIT 1")
    suspend fun findIdentifier(type: IdentifierType, value: String): ContactIdentifier?

    @Insert
    suspend fun insertLog(log: OperationLog)

    @Query("SELECT * FROM operation_logs WHERE contactId = :contactId ORDER BY timestamp DESC")
    suspend fun getLogsByContactId(contactId: Int): List<OperationLog>

    @Query("""
        SELECT
            (SELECT COUNT(*) FROM contacts) as total,
            (SELECT COUNT(*) FROM contacts WHERE status = 'PENDING') as pending,
            (SELECT COUNT(*) FROM contacts WHERE status = 'APPLIED') as applied,
            (SELECT COUNT(*) FROM contacts WHERE status = 'ACCEPTED') as accepted,
            (SELECT COUNT(*) FROM contacts WHERE status = 'REJECTED') as rejected,
            (SELECT COUNT(*) FROM contacts WHERE status = 'FAILED') as failed
        FROM contacts
        LIMIT 1
    """)
    suspend fun getStats(): StatsRow

    @Transaction
    suspend fun batchInsertContactWithIdentifiers(
        contact: Contact,
        identifiers: List<ContactIdentifier>
    ): Long {
        val contactId = insertContact(contact)
        if (contactId == -1L) return -1L
        insertAllIdentifiers(identifiers)
        return contactId
    }
}

data class StatsRow(
    val total: Int? = 0,
    val pending: Int? = 0,
    val applied: Int? = 0,
    val accepted: Int? = 0,
    val rejected: Int? = 0,
    val failed: Int? = 0
)