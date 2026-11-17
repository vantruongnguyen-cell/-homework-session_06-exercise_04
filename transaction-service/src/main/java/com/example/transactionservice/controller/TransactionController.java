package com.example.transactionservice.controller;

import com.example.transactionservice.dto.TransferRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);
    private final RestTemplate restTemplate;

    @Value("${account.service.url}")
    private String accountServiceUrl;

    public TransactionController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody TransferRequest request) {
        String url = accountServiceUrl + "/api/accounts/" + request.getFromAccountId();
        log.info("Đang gọi nội bộ sang account-service để kiểm tra tài khoản nguồn: {}", url);

        try {
            // Gọi HTTP GET nội bộ sang account-service bằng DNS container
            ResponseEntity response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Xác thực tài khoản nguồn thành công: {}", response.getBody());
                return ResponseEntity.ok(Map.of(
                        "status", "SUCCESS",
                        "message", "Chuyển tiền thành công từ tài khoản ID " + request.getFromAccountId(),
                        "amount", request.getAmount(),
                        "sourceAccount", response.getBody()
                ));
            }
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Tài khoản nguồn ID {} không tồn tại!", request.getFromAccountId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", "FAILED", "message", "Tài khoản nguồn không tồn tại"));
        } catch (Exception e) {
            log.error("Lỗi khi gọi sang account-service: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "ERROR", "message", "Lỗi kết nối nội bộ tới account-service"));
        }

        return ResponseEntity.badRequest().build();
    }
}