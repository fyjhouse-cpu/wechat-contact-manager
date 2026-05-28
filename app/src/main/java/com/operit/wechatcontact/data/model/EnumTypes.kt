package com.operit.wechatcontact.data.model

enum class IdentifierType {
    PHONE,
    WECHAT_ID
}

enum class ContactStatus(val label: String, val colorHex: Int) {
    PENDING("待添加", 0xFF6B7280),
    APPLIED("已申请", 0xFFF59E0B),
    ACCEPTED("已通过", 0xFF10B981),
    REJECTED("被拒绝", 0xFFEF4444),
    FAILED("失败", 0xFFF97316);

    companion object {
        fun fromString(value: String): ContactStatus {
            return entries.find { it.name == value } ?: PENDING
        }
    }
}

data class ContactWithIdentifiers(
    val contact: Contact,
    val identifiers: List<ContactIdentifier>
)

data class ImportResultData(
    val inserted: Int = 0,
    val skipped: Int = 0,
    val invalid: Int = 0,
    val invalidItems: List<String> = emptyList()
)