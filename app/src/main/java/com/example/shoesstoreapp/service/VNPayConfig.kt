package com.example.shoesstoreapp.service

object VNPayConfig {
    const val VNPAY_TMN_CODE ="ZEMKFMD7" // Terminal ID from VNPay
    const val VNPAY_HASH_SECRET = "8BJ16WHHG9BMIKWB0C8URU7IBCBRRZ9Z"  // Hash secret from VNPay
    const val VNPAY_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html" // Sandbox URL
    const val RETURN_URL = "shoesstoreapp://payment_result" // Your app's scheme

    // Payment Status
    const val STATUS_SUCCESS = "00"
    const val STATUS_FAILED = "01"
    const val STATUS_PENDING = "02"
}