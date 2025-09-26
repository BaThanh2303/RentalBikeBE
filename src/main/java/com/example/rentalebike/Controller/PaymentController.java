package com.example.rentalebike.Controller;

import com.example.rentalebike.Models.Payment;
import com.example.rentalebike.Models.Rental;
import com.example.rentalebike.Models.Vehicle;
import com.example.rentalebike.Service.PaymentService;
import com.example.rentalebike.Service.RentalService;
import com.example.rentalebike.Service.VNPayService;
import com.example.rentalebike.Service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RentalService rentalService;

    @Autowired
    private VehicleService vehicleService;

    /**
     * Tạo thanh toán VNPay - đơn giản
     */
    @PostMapping("/vnpay/create")
    public ResponseEntity<?> createVNPayPayment(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            Long rentalId = Long.valueOf(request.get("rentalId").toString());
            Double amount = Double.valueOf(request.get("amount").toString());
            String orderInfo = request.getOrDefault("orderInfo", "Thanh toan thue xe E-Bike").toString();

            // Kiểm tra rental
            Rental rental = rentalService.findById(rentalId);
            if (rental == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Rental not found"));
            }

            // Kiểm tra payment đã tồn tại chưa
            if (paymentService.findByRentalId(rentalId).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Payment already exists"));
            }

            // Tạo payment record
            String txnRef = "RENTAL_" + rentalId + "_" + System.currentTimeMillis();
            Payment payment = Payment.builder()
                .rental(rental)
                .user(rental.getUser())
                .amount(amount)
                .currency("VND")
                .paymentMethod("vnpay")
                .vnpayTxnRef(txnRef)
                .status(Payment.Status.PENDING)
                .callbackProcessed(false)
                .build();

            paymentService.save(payment);

            // Tạo VNPay URL
            String ipAddress = getClientIpAddress(httpRequest);
            String paymentUrl = vnPayService.createPaymentUrl(txnRef, amount, orderInfo, ipAddress);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "paymentUrl", paymentUrl,
                "txnRef", txnRef,
                "message", "Payment created successfully"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Xử lý callback từ VNPay - đơn giản
     */
    @PostMapping("/vnpay/callback")
    public ResponseEntity<?> handleVNPayCallback(@RequestParam Map<String, String> params) {
        try {
            // Xác thực callback
            if (!vnPayService.validateCallback(params)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid signature"));
            }

            // Lấy thông tin giao dịch
            Map<String, String> transactionInfo = vnPayService.extractTransactionInfo(params);
            String txnRef = transactionInfo.get("txnRef");
            boolean isSuccess = vnPayService.isPaymentSuccess(params);

            // Tìm payment
            Payment payment = paymentService.findByVnpayTxnRef(txnRef)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

            // Cập nhật payment
            payment.setVnpayResponseCode(transactionInfo.get("responseCode"));
            payment.setVnpayTransactionNo(transactionInfo.get("transactionNo"));
            payment.setVnpayBankCode(transactionInfo.get("bankCode"));
            payment.setVnpayCardType(transactionInfo.get("cardType"));
            payment.setCallbackProcessed(true);

            if (isSuccess) {
                payment.setStatus(Payment.Status.COMPLETED);
                // Cập nhật rental status
                Rental rental = payment.getRental();
                rental.setStatus(Rental.Status.PAID);
                
                // Update vehicle status to RENTED to prevent others from renting
                Vehicle vehicle = rental.getVehicle();
                if (vehicle != null) {
                    vehicle.setStatus(Vehicle.Status.RENTED);
                    vehicleService.save(vehicle);
                    logger.info("Vehicle status updated to RENTED via callback - Vehicle ID: {}", vehicle.getVehicleId());
                }
                
                rentalService.save(rental);
            } else {
                payment.setStatus(Payment.Status.FAILED);
            }

            paymentService.save(payment);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Payment processed successfully",
                "status", payment.getStatus().toString()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * VNPay Return Handler - Xử lý return từ VNPay với redirect logic
     */
    @GetMapping("/vnpay/return")
    public ResponseEntity<?> handleVNPayReturn(HttpServletRequest request) {
        try {
            // 1. Extract VNPay parameters
            String vnpResponseCode = request.getParameter("vnp_ResponseCode");
            String vnpTxnRef = request.getParameter("vnp_TxnRef");
            String vnpAmount = request.getParameter("vnp_Amount");
            String vnpBankCode = request.getParameter("vnp_BankCode");
            String vnpTransactionNo = request.getParameter("vnp_TransactionNo");
            String vnpCardType = request.getParameter("vnp_CardType");

            logger.info("VNPay Return - TxnRef: {}, ResponseCode: {}, Amount: {}", 
                vnpTxnRef, vnpResponseCode, vnpAmount);

            // 2. Find payment and validate
            Payment payment = paymentService.findByVnpayTxnRef(vnpTxnRef)
                .orElseThrow(() -> new RuntimeException("Payment not found for txnRef: " + vnpTxnRef));

            Rental rental = payment.getRental();
            Long rentalId = rental.getRentalId();

            // 3. Update payment with VNPay details
            payment.setVnpayResponseCode(vnpResponseCode);
            payment.setVnpayTransactionNo(vnpTransactionNo);
            payment.setVnpayBankCode(vnpBankCode);
            payment.setVnpayCardType(vnpCardType);
            payment.setCallbackProcessed(true);

            // 4. Handle success/failure based on response code
            if ("00".equals(vnpResponseCode)) {
                // Success: Update payment + rental + vehicle status
                payment.setStatus(Payment.Status.COMPLETED);
                rental.setStatus(Rental.Status.PAID);
                
                // Update vehicle status to RENTED to prevent others from renting
                Vehicle vehicle = rental.getVehicle();
                if (vehicle != null) {
                    vehicle.setStatus(Vehicle.Status.RENTED);
                    vehicleService.save(vehicle);
                    logger.info("Vehicle status updated to RENTED - Vehicle ID: {}, License Plate: {}", 
                        vehicle.getVehicleId(), vehicle.getLicensePlate());
                }
                
                // Save updates
                paymentService.save(payment);
                rentalService.save(rental);
                
                logger.info("Payment completed successfully - Rental ID: {}, Payment ID: {}, Vehicle ID: {}", 
                    rentalId, payment.getPaymentId(), vehicle != null ? vehicle.getVehicleId() : "N/A");

                // Redirect to success page
                String successUrl = String.format(
                    "http://localhost:5173/payment/success?rentalId=%d&paymentId=%d&vnp_ResponseCode=%s&vnp_TxnRef=%s&amount=%s",
                    rentalId, payment.getPaymentId(), vnpResponseCode, vnpTxnRef, vnpAmount
                );
                
                return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", successUrl)
                    .build();
                    
            } else {
                // Failed: Update payment status only
                payment.setStatus(Payment.Status.FAILED);
                paymentService.save(payment);
                
                logger.warn("Payment failed - Rental ID: {}, ResponseCode: {}", rentalId, vnpResponseCode);

                // Redirect to error page
                String errorUrl = String.format(
                    "http://localhost:5173/payment/error?rentalId=%d&paymentId=%d&vnp_ResponseCode=%s&vnp_TxnRef=%s&errorMessage=%s",
                    rentalId, payment.getPaymentId(), vnpResponseCode, vnpTxnRef, 
                    getErrorMessage(vnpResponseCode)
                );
                
                return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", errorUrl)
                    .build();
            }

        } catch (Exception e) {
            logger.error("Error handling VNPay return: ", e);
            
            // Redirect to error page with error message
            String errorUrl = String.format(
                "http://localhost:5173/payment/error?errorMessage=%s",
                URLEncoder.encode("Lỗi xử lý thanh toán: " + e.getMessage(), StandardCharsets.UTF_8)
            );
            
            return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", errorUrl)
                .build();
        }
    }

    /**
     * Test VNPay configuration
     */
    @GetMapping("/vnpay/test")
    public ResponseEntity<?> testVNPayConfig() {
        try {
            String testTxnRef = "TEST_" + System.currentTimeMillis();
            String testUrl = vnPayService.createPaymentUrl(testTxnRef, 1000.0, "Test payment", "127.0.0.1");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "VNPay configuration is working",
                "testUrl", testUrl,
                "txnRef", testTxnRef
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Test vehicle status update flow
     */
    @GetMapping("/vnpay/test-vehicle-status")
    public ResponseEntity<?> testVehicleStatusUpdate() {
        try {
            // Mock test data
            String testTxnRef = "TEST_VEHICLE_" + System.currentTimeMillis();
            String vnpResponseCode = "00"; // Success
            String vnpAmount = "50000"; // 500 VND in cents
            String vnpBankCode = "VNPAYQR";
            String vnpTransactionNo = "TEST_TXN_" + System.currentTimeMillis();
            String vnpCardType = "ATM";
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Vehicle status update test endpoint",
                "testData", Map.of(
                    "vnp_ResponseCode", vnpResponseCode,
                    "vnp_TxnRef", testTxnRef,
                    "vnp_Amount", vnpAmount,
                    "vnp_BankCode", vnpBankCode,
                    "vnp_TransactionNo", vnpTransactionNo,
                    "vnp_CardType", vnpCardType
                ),
                "expectedFlow", Map.of(
                    "step1", "Payment: PENDING → COMPLETED",
                    "step2", "Rental: PENDING → PAID", 
                    "step3", "Vehicle: AVAILABLE → RENTED",
                    "step4", "Redirect to success page"
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Test VNPay return flow với mock data
     */
    @GetMapping("/vnpay/test-return")
    public ResponseEntity<?> testVNPayReturn() {
        try {
            // Mock VNPay return parameters
            String testTxnRef = "TEST_" + System.currentTimeMillis();
            String vnpResponseCode = "00"; // Success
            String vnpAmount = "100000"; // 1000 VND in cents
            String vnpBankCode = "VNPAYQR";
            String vnpTransactionNo = "TEST_TXN_" + System.currentTimeMillis();
            String vnpCardType = "ATM";
            
            // Create test URL with mock parameters
            String testUrl = String.format(
                "/api/payments/vnpay/return?vnp_ResponseCode=%s&vnp_TxnRef=%s&vnp_Amount=%s&vnp_BankCode=%s&vnp_TransactionNo=%s&vnp_CardType=%s",
                vnpResponseCode, testTxnRef, vnpAmount, vnpBankCode, vnpTransactionNo, vnpCardType
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Test VNPay return URL created",
                "testUrl", testUrl,
                "parameters", Map.of(
                    "vnp_ResponseCode", vnpResponseCode,
                    "vnp_TxnRef", testTxnRef,
                    "vnp_Amount", vnpAmount,
                    "vnp_BankCode", vnpBankCode,
                    "vnp_TransactionNo", vnpTransactionNo,
                    "vnp_CardType", vnpCardType
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Lấy thông tin payment
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<?> getPayment(@PathVariable Long paymentId) {
        try {
            Payment payment = paymentService.getPaymentById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

            return ResponseEntity.ok(Map.of(
                "success", true,
                "payment", Map.of(
                    "paymentId", payment.getPaymentId(),
                    "rentalId", payment.getRental().getRentalId(),
                    "amount", payment.getAmount(),
                    "status", payment.getStatus(),
                    "vnpayTxnRef", payment.getVnpayTxnRef(),
                    "createdAt", payment.getCreatedAt()
                )
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // Helper method
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            return xForwardedForHeader.split(",")[0];
        }
    }

    /**
     * Get error message for VNPay response codes
     */
    private String getErrorMessage(String responseCode) {
        switch (responseCode) {
            case "00":
                return "Giao dịch thành công";
            case "07":
                return "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường)";
            case "09":
                return "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking";
            case "10":
                return "Xác thực thông tin thẻ/tài khoản không đúng quá 3 lần";
            case "11":
                return "Đã hết hạn chờ thanh toán. Xin vui lòng thực hiện lại giao dịch";
            case "12":
                return "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng bị khóa";
            case "24":
                return "Giao dịch không thành công do: Khách hàng hủy giao dịch";
            case "51":
                return "Giao dịch không thành công do: Tài khoản của quý khách không đủ số dư để thực hiện giao dịch";
            case "65":
                return "Giao dịch không thành công do: Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày";
            case "75":
                return "Ngân hàng thanh toán đang bảo trì";
            case "79":
                return "Nhập sai mật khẩu thanh toán quá số lần quy định";
            case "99":
                return "Các lỗi khác (lỗi còn lại, không có trong danh sách mã lỗi đã liệt kê)";
            default:
                return "Giao dịch không thành công. Mã lỗi: " + responseCode;
        }
    }
}
