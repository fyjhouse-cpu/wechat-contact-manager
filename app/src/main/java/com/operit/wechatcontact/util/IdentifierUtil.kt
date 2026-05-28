package com.operit.wechatcontact.util

import com.operit.wechatcontact.data.model.IdentifierType

object IdentifierUtil {

    fun normalizePhone(raw: String): String {
        val digits = raw.replace(Regex("[^0-9]"), "")
        return when {
            digits.startsWith("86") && digits.length == 13 -> digits.substring(2)
            digits.startsWith("0") && digits.length == 12 -> digits.substring(1)
            digits.length == 11 -> digits
            else -> digits
        }
    }

    fun isValidPhone(phone: String): Boolean {
        return Regex("^1[3-9]\\d{9}$").matches(phone)
    }

    fun isValidWechatId(id: String): Boolean {
        if (id.length < 6 || id.length > 20) return false
        return Regex("^[a-zA-Z][a-zA-Z0-9_\\.\\-]*$").matches(id)
    }

    fun detectType(value: String): IdentifierType {
        val normalized = normalizePhone(value)
        return if (isValidPhone(normalized)) {
            IdentifierType.PHONE
        } else if (isValidWechatId(value)) {
            IdentifierType.WECHAT_ID
        } else {
            IdentifierType.PHONE
        }
    }

    fun maskPhone(phone: String): String {
        return if (phone.length == 11) {
            "${phone.substring(0, 3)}****${phone.substring(7)}"
        } else phone
    }

    fun maskWechatId(id: String): String {
        return if (id.length > 6) {
            "${id.substring(0, 3)}****${id.substring(id.length - 3)}"
        } else id
    }

    fun formatTimestamp(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.CHINA)
        return sdf.format(java.util.Date(timestamp))
    }

    fun formatRelativeTime(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        val minutes = diff / 60000
        val hours = diff / 3600000
        val days = diff / 86400000
        return when {
            minutes < 1 -> "刚刚"
            minutes < 60 -> "${minutes}分钟前"
            hours < 24 -> "${hours}小时前"
            days < 30 -> "${days}天前"
            else -> formatTimestamp(timestamp)
        }
    }
}