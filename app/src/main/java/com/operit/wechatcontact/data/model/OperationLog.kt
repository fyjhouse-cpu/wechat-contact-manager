package com.operit.wechatcontact.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "operation_logs",
    foreignKeys = [
        ForeignKey(
            entity = Contact::class,
            parentColumns = ["id"],
            childColumns = ["contactId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("contactId"), Index("timestamp")]
)
data class OperationLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val contactId: Int,
    val fromStatus: String = "",
    val toStatus: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val remark: String = ""
)