package com.operit.wechatcontact.data.importer

import android.content.ContentUris
import android.content.Context
import android.provider.ContactsContract
import com.operit.wechatcontact.data.dao.ContactDao
import com.operit.wechatcontact.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactImporter(
    private val context: Context,
    private val dao: ContactDao
) {

    suspend fun importFromPhonebook(): ImportResultData = withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID
            ),
            null, null, null
        )

        val processed = mutableSetOf<Long>()
        var inserted = 0
        var skipped = 0
        val invalidItems = mutableListOf<String>()

        cursor?.use { it ->
            val nameIdx = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIdx = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val contactIdIdx = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)

            while (it.moveToNext()) {
                val contactId = it.getLong(contactIdIdx)
                if (contactId in processed) continue
                processed.add(contactId)

                val name = it.getString(nameIdx) ?: ""
                val rawNumber = it.getString(numberIdx) ?: ""
                val normalized = IdentifierUtil.normalizePhone(rawNumber)

                if (IdentifierUtil.isValidPhone(normalized)) {
                    val existing = dao.findIdentifier(IdentifierType.PHONE, normalized)
                    if (existing != null) {
                        skipped++
                    } else {
                        val now = System.currentTimeMillis()
                        val contact = Contact(
                            name = name,
                            source = "通讯录导入",
                            status = ContactStatus.PENDING,
                            createdAt = now,
                            updatedAt = now
                        )
                        val id = dao.insertContact(contact)
                        if (id != -1L) {
                            dao.insertIdentifier(ContactIdentifier(
                                contactId = id.toInt(),
                                type = IdentifierType.PHONE,
                                value = normalized
                            ))
                            inserted++
                        }
                    }
                } else if (rawNumber.isNotBlank()) {
                    invalidItems.add(rawNumber)
                }
            }
        }

        ImportResultData(inserted = inserted, skipped = skipped, invalid = invalidItems.size, invalidItems = invalidItems)
    }

    suspend fun importFromText(text: String): ImportResultData = withContext(Dispatchers.IO) {
        val lines = text.split(Regex("[\\r\\n]+")).map { it.trim() }.filter { it.isNotBlank() }
        var inserted = 0
        var skipped = 0
        val invalidItems = mutableListOf<String>()

        for (line in lines) {
            val result = parseSingleIdentifier(line)
            when (result) {
                is ParseResult.Success -> {
                    val existing = dao.findIdentifier(result.type, result.value)
                    if (existing != null) {
                        skipped++
                    } else {
                        val now = System.currentTimeMillis()
                        val contact = Contact(
                            name = result.name,
                            source = "手动粘贴",
                            status = ContactStatus.PENDING,
                            createdAt = now,
                            updatedAt = now
                        )
                        val id = dao.insertContact(contact)
                        if (id != -1L) {
                            dao.insertIdentifier(ContactIdentifier(
                                contactId = id.toInt(),
                                type = result.type,
                                value = result.value
                            ))
                            inserted++
                        }
                    }
                }
                is ParseResult.Invalid -> {
                    invalidItems.add(result.raw)
                }
            }
        }

        ImportResultData(inserted = inserted, skipped = skipped, invalid = invalidItems.size, invalidItems = invalidItems)
    }

    private sealed class ParseResult {
        data class Success(val type: IdentifierType, val value: String, val name: String) : ParseResult()
        data class Invalid(val raw: String) : ParseResult()
    }

    private fun parseSingleIdentifier(line: String): ParseResult {
        val trimmed = line.trim()
        val parts = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }

        var name = ""
        var rawValue = ""

        if (parts.size >= 2) {
            name = parts[0]
            rawValue = parts.subList(1, parts.size).joinToString("")
        } else {
            rawValue = parts[0]
        }

        val normalized = IdentifierUtil.normalizePhone(rawValue)
        if (IdentifierUtil.isValidPhone(normalized)) {
            return ParseResult.Success(IdentifierType.PHONE, normalized, name)
        }
        if (IdentifierUtil.isValidWechatId(rawValue)) {
            return ParseResult.Success(IdentifierType.WECHAT_ID, rawValue, name)
        }
        return ParseResult.Invalid(trimmed)
    }
}