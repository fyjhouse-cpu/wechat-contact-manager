package com.operit.wechatcontact.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contact_identifiers",
    foreignKeys = [
        ForeignKey(
            entity = Contact::class,
            parentColumns = ["id"],
            childColumns = ["contactId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["type", "value"], unique = true),
        Index("contactId")
    ]
)
data class ContactIdentifier(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val contactId: Int,
    val type: IdentifierType = IdentifierType.PHONE,
    val value: String = "",
    val isPrimary: Boolean = true
)