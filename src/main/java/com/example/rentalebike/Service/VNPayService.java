package com.example.rentalebike.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Calendar;

@Service
public class VNPayService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(VNPayService.class);

    @Value("${vnpay.tmn.code}")
    private String tmnCode;

    @Value("${vnpay.hash.secret}")
    private String hashSecret;

    @Value("${vnpay.pay.url}")
    private String payUrl;

    @Value("${vnpay.return.url}")
    private String returnUrl;

    /**
     * Tạo URL thanh toán VNPay - đơn giản theo chuẩn VNPay
     */
    public String createPaymentUrl(String txnRef, Double amount, String orderInfo, String ipAddress) {
        try {
            logger.info("Creating VNPay payment URL - TxnRef: {}, Amount: {}, IP: {}", txnRef, amount, ipAddress);
            
            Map<String, String> vnpParams = new HashMap<>();
            vnpParams.put("vnp_Version", "2.1.0");
            vnpParams.put("vnp_Command", "pay");
            vnpParams.put("vnp_TmnCode", tmnCode);
            vnpParams.put("vnp_Amount", String.valueOf((long) (amount * 100))); // VND cents
            vnpParams.put("vnp_CurrCode", "VND");
            vnpParams.put("vnp_TxnRef", txnRef);
            vnpParams.put("vnp_OrderInfo", orderInfo);
            vnpParams.put("vnp_OrderType", "other");
            vnpParams.put("vnp_Locale", "vn");
            vnpParams.put("vnp_ReturnUrl", returnUrl);
            vnpParams.put("vnp_IpAddr", ipAddress);
            vnpParams.put("vnp_CreateDate", getCurrentDateString());
            vnpParams.put("vnp_ExpireDate", getExpireDateString()); // Thêm thời gian hết hạn

            // Sort và build query string
            String queryString = buildQueryString(vnpParams);
            String secureHash = hmacSHA512(hashSecret, queryString);
            
            String paymentUrl = payUrl + "?" + queryString + "&vnp_SecureHash=" + secureHash;
            logger.info("VNPay payment URL created successfully: {}", paymentUrl);
            
            return paymentUrl;

        } catch (Exception e) {
            throw new RuntimeException("Tạo URL thanh toán VNPay thất bại: " + e.getMessage());
        }
    }

    /**
     * Xác thực callback từ VNPay - đơn giản theo chuẩn VNPay
     */
    public boolean validateCallback(Map<String, String> params) {
        try {
            String vnpSecureHash = params.get("vnp_SecureHash");
            params.remove("vnp_SecureHashType");
            params.remove("vnp_SecureHash");

            String queryString = buildQueryString(params);
            String signValue = hmacSHA512(hashSecret, queryString);
            
            return signValue.equals(vnpSecureHash);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Kiểm tra giao dịch thành công hay không
     */
    public boolean isPaymentSuccess(Map<String, String> params) {
        return "00".equals(params.get("vnp_ResponseCode"));
    }

    /**
     * Lấy thông tin giao dịch từ callback
     */
    public Map<String, String> extractTransactionInfo(Map<String, String> params) {
        Map<String, String> info = new HashMap<>();
        info.put("txnRef", params.get("vnp_TxnRef"));
        info.put("responseCode", params.get("vnp_ResponseCode"));
        info.put("transactionNo", params.get("vnp_TransactionNo"));
        info.put("bankCode", params.get("vnp_BankCode"));
        info.put("cardType", params.get("vnp_CardType"));
        info.put("amount", String.valueOf(Double.parseDouble(params.get("vnp_Amount")) / 100));
        return info;
    }

    // Helper methods
    private String buildQueryString(Map<String, String> params) throws Exception {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        
        StringBuilder query = new StringBuilder();
        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            String fieldValue = params.get(fieldName);
            
            if (fieldValue != null && !fieldValue.isEmpty()) {
                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                if (i < fieldNames.size() - 1) {
                    query.append('&');
                }
            }
        }
        return query.toString();
    }

    private String getCurrentDateString() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh")); // Sử dụng timezone Việt Nam
        return formatter.format(new Date());
    }

    private String getExpireDateString() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        
        // Thời gian hết hạn: 15 phút từ bây giờ
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        calendar.add(Calendar.MINUTE, 15);
        
        return formatter.format(calendar.getTime());
    }

    private String hmacSHA512(String key, String data) throws Exception {
        Mac hmac512 = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA512");
        hmac512.init(secretKey);
        byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
        
        StringBuilder sb = new StringBuilder();
        for (byte b : result) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }
}
