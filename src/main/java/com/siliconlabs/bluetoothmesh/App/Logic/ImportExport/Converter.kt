package com.siliconlabs.bluetoothmesh.App.Logic.ImportExport

import android.util.Log
import com.siliconlab.bluetoothmesh.adk.data_model.model.Address
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object Converter {
    internal fun hexToBytes(hexString: String?): ByteArray? {
        if (hexString == null) return null
        if (hexString.length % 2 != 0) {
            Log.e("Converter", "hexToBytes: invalid hexString")
            return null
        }
        return ByteArray(hexString.length / 2) {
            hexString.substring(it * 2, it * 2 + 2).toInt(16).toByte()
        }
    }
    internal fun bytesToHex(bytes: ByteArray?): String? {
        if (bytes == null) return null
        val sb = StringBuilder()
        bytes.forEach { sb.append(String.format("%02x", it)) }
        return sb.toString()
    }
    internal fun hexToInt(hexString: String?): Int? {
        return hexString?.let { Integer.parseInt(it, 16) }
    }
    internal fun intToHex(value: Int?, width: Int = 1): String? {
        return value?.let { String.format("%1$0${width}X", it) }
    }
    internal fun isVirtualAddress(hexString: String): Boolean {
        return hexString.length == 32
    }

    internal fun stringToUuid(uuid: String): UUID {
        return UUID.fromString(uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-" +
                uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-" + uuid.substring(20,
            32))
    }
    internal fun timestampToLong(timestamp: String?): Long? {
        try {
            return timestamp?.let { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(it).time }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return null
    }
    fun longToTimestamp(timestamp: Long?): String? {
        return timestamp?.let { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date(it)) }
    }
    fun addressToHex(address: Address): String? {
        return intToHex(address.value, 4) ?: bytesToHex(address.virtualLabel)
    }
    fun uuidToString(uuid: UUID): String {
        return uuid.toString().replace("-", "")
    }
}
