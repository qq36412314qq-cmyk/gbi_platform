package hardware

import (
	"encoding/json"
	"fmt"
	"math/rand"
	"os/exec"
	"strings"
	"time"
)

// HardwareInfo 硬件信息结构
type HardwareInfo struct {
	MotherboardSN string `json:"motherboardSn"`
	CPUID         string `json:"cpuId"`
	DiskSN        string `json:"diskSn"`
	Timestamp     int64  `json:"timestamp"`
	Nonce         string `json:"nonce"`
}

// CollectHardwareInfo 采集硬件信息（PowerShell WMI）
func CollectHardwareInfo() (*HardwareInfo, error) {
	info := &HardwareInfo{}

	// 主板SN
	snOut, err := exec.Command("powershell", "-Command",
		`(Get-WmiObject -Class Win32_BaseBoard).SerialNumber`).Output()
	if err == nil && len(snOut) > 0 {
		info.MotherboardSN = strings.TrimSpace(string(snOut))
	}

	// CPU ID
	cpuOut, err := exec.Command("powershell", "-Command",
		`(Get-WmiObject -Class Win32_Processor).ProcessorId`).Output()
	if err == nil && len(cpuOut) > 0 {
		info.CPUID = strings.TrimSpace(string(cpuOut))
	}

	// 硬盘序列号
	diskOut, err := exec.Command("powershell", "-Command",
		`(Get-WmiObject -Class Win32_DiskDrive).SerialNumber`).Output()
	if err == nil && len(diskOut) > 0 {
		info.DiskSN = strings.TrimSpace(string(diskOut))
	}

	if info.MotherboardSN == "" && info.CPUID == "" {
		return nil, fmt.Errorf("无法采集硬件信息")
	}

	return info, nil
}

// GetHardwareInfoJSON 返回JSON格式硬件信息
func GetHardwareInfoJSON() (string, error) {
	info, err := CollectHardwareInfo()
	if err != nil {
		return "", err
	}
	info.Timestamp = time.Now().Unix()
	info.Nonce = generateNonce()
	data, err := json.Marshal(info)
	if err != nil {
		return "", err
	}
	return string(data), nil
}

// generateNonce 生成16位随机nonce
func generateNonce() string {
	b := make([]byte, 8)
	for i := range b {
		b[i] = byte(0x30 + rand.Intn(10))
	}
	return string(b)
}
