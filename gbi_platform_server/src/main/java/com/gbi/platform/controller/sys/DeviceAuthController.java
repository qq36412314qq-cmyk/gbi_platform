package com.gbi.platform.controller.sys;

import com.gbi.platform.common.result.Result;
import com.gbi.platform.dto.ClientDeviceAuthDTO;
import com.gbi.platform.dto.DeviceHardwareInfoDTO;
import com.gbi.platform.entity.SysClientDeviceAuth;
import com.gbi.platform.service.ConfigService;
import com.gbi.platform.service.SysClientDeviceAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 客户端设备校验接口（仅回环访问）
 *
 * @author gbi
 */
@Tag(name = "设备校验")
@RestController
@RequestMapping("/sys/device")
@RequiredArgsConstructor
public class DeviceAuthController {

    private final ConfigService configService;
    private final SysClientDeviceAuthService deviceAuthService;

    @Operation(summary = "获取硬件信息（代理Go YOIREI Device Verification）")
    @GetMapping("/hardware/info")
    public Result<?> getHardwareInfo() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Object> resp = restTemplate.getForEntity("http://127.0.0.1:8765/api/hardware/info", Object.class);
            return Result.success(resp.getBody());
        } catch (Exception e) {
            return Result.error("设备校验YOIREI Device Verification未启动，请联系管理员");
        }
    }

    @Operation(summary = "获取硬件信息并签名")
    @PostMapping("/hardware/sign")
    public Result<Map<String, String>> signHardware(@RequestBody DeviceHardwareInfoDTO hardwareInfo) {
        String privateKeyPem = configService.getValueByKey("device.auth.rsa_private_key");
        if (privateKeyPem == null || privateKeyPem.isBlank()) {
            return Result.error("RSA私钥未配置");
        }

        // 签名数据：主板SN + CPU编号 + 时间戳 + nonce
        String signData = hardwareInfo.getMotherboardSn() + "|"
                + hardwareInfo.getCpuId() + "|"
                + hardwareInfo.getTimestamp() + "|"
                + hardwareInfo.getNonce();

        // 使用 HMAC-SHA256 签名（简化版，生产环境建议用RSA）
        String signature;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(privateKeyPem.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] rawHash = mac.doFinal(signData.getBytes(StandardCharsets.UTF_8));
            signature = Base64.getEncoder().encodeToString(rawHash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            return Result.error("签名失败：" + e.getMessage());
        }

        Map<String, String> result = new HashMap<>();
        result.put("signature", signature);
        return Result.success(result);
    }

    @Operation(summary = "客户端注册/更新设备授权记录")
    @PostMapping("/auth/register")
    public Result<Map<String, Object>> registerDevice(@RequestBody DeviceHardwareInfoDTO hardwareInfo) {
        String motherboardSn = hardwareInfo.getMotherboardSn();
        String cpuId = hardwareInfo.getCpuId();
        if (motherboardSn == null || motherboardSn.isBlank() || cpuId == null || cpuId.isBlank()) {
            return Result.error("主板SN和CPU编号不能为空");
        }

        Long deviceId = deviceAuthService.upsertByHardware(motherboardSn, cpuId, hardwareInfo.getDiskSn());
        Map<String, Object> result = new HashMap<>();
        result.put("deviceId", deviceId);
        result.put("action", deviceId != null ? "registered" : "not_found");
        return Result.success(result);
    }
}
