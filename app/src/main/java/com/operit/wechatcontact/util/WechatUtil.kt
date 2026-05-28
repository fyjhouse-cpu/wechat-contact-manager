package com.operit.wechatcontact.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat

object WechatUtil {

    private const val WECHAT_PACKAGE = "com.tencent.mm"

    fun isWechatInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(WECHAT_PACKAGE, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun launchWechat(context: Context, identifier: String): Boolean {
        if (!isWechatInstalled(context)) {
            Toast.makeText(context, "请先安装微信", Toast.LENGTH_LONG).show()
            return false
        }
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("wechat_id", identifier))
        Toast.makeText(context, "已复制，正在打开微信...", Toast.LENGTH_SHORT).show()
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(WECHAT_PACKAGE)
            intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Toast.makeText(context, "打开微信失败", Toast.LENGTH_SHORT).show()
            false
        }
    }
}