package com.example.shoesstoreapp.service
import android.content.Context
import android.content.Intent
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.HashMap
class VNPayHelper {
    companion object {
        fun createPaymentUrl(
            context: Context,
            amount: Double,
            orderInfo: String
        ): String {
            val vnpParams = HashMap<String, String>()

            val calendar = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"))
            val formatter = SimpleDateFormat("yyyyMMddHHmmss")
            val createDate = formatter.format(calendar.time)

            vnpParams["vnp_Version"] = "2.1.0"
            vnpParams["vnp_Command"] = "pay"
            vnpParams["vnp_TmnCode"] = VNPayConfig.VNPAY_TMN_CODE
            vnpParams["vnp_Amount"] = (amount * 100).toInt().toString() // Convert to VND cents
            vnpParams["vnp_CreateDate"] = createDate
            vnpParams["vnp_CurrCode"] = "VND"
            vnpParams["vnp_IpAddr"] = "127.0.0.1"
            vnpParams["vnp_Locale"] = "vn"
            vnpParams["vnp_OrderInfo"] = orderInfo
            vnpParams["vnp_ReturnUrl"] = VNPayConfig.RETURN_URL
            vnpParams["vnp_TxnRef"] = System.currentTimeMillis().toString()

            val queryUrl = buildQueryString(vnpParams)
            val signData = queryUrl + VNPayConfig.VNPAY_HASH_SECRET
            val signature = hmacSHA512(signData)

            return VNPayConfig.VNPAY_URL + "?" + queryUrl + "&vnp_SecureHash=$signature"
        }

        private fun buildQueryString(params: HashMap<String, String>): String {
            val sortedParams = params.toSortedMap()
            return sortedParams.map { "${it.key}=${Uri.encode(it.value)}" }.joinToString("&")
        }

        private fun hmacSHA512(data: String): String {
            val key = VNPayConfig.VNPAY_HASH_SECRET
            val sha512HMAC = Mac.getInstance("HmacSHA512")
            val secretKey = SecretKeySpec(key.toByteArray(), "HmacSHA512")
            sha512HMAC.init(secretKey)
            val hash = sha512HMAC.doFinal(data.toByteArray())
            return bytesToHex(hash)
        }

        private fun bytesToHex(bytes: ByteArray): String {
            val hexChars = "0123456789ABCDEF"
            val result = StringBuilder(bytes.size * 2)
            bytes.forEach {
                val i = it.toInt()
                result.append(hexChars[i shr 4 and 0x0f])
                result.append(hexChars[i and 0x0f])
            }
            return result.toString()
        }

        fun handlePaymentResult(uri: Uri): PaymentResult {
            val responseCode = uri.getQueryParameter("vnp_ResponseCode")
            val transactionId = uri.getQueryParameter("vnp_TransactionNo") ?: ""

            return when (responseCode) {
                VNPayConfig.STATUS_SUCCESS -> PaymentResult.Success(transactionId)
                VNPayConfig.STATUS_FAILED -> PaymentResult.Failed("Payment failed")
                else -> PaymentResult.Failed("Unknown error")
            }
        }
    }
}

sealed class PaymentResult {
    data class Success(val transactionId: String) : PaymentResult()
    data class Failed(val message: String) : PaymentResult()
}