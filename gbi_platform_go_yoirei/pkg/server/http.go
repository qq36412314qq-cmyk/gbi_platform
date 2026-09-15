package server

import (
	"encoding/json"
	"log"
	"net/http"
	"time"

	"gbi_platform_go_yoirei/pkg/hardware"
)

const defaultPort = "8765"

// StartServer 启动HTTP服务（仅监听127.0.0.1）
func StartServer(port string) {
	if port == "" {
		port = defaultPort
	}

	http.HandleFunc("/api/hardware/info", handleHardwareInfo)
	http.HandleFunc("/api/health", handleHealth)

	addr := "127.0.0.1:" + port
	log.Printf("Agent服务已启动，监听地址: %s", addr)
	log.Printf("硬件信息采集接口: http://127.0.0.1:%s/api/hardware/info", port)

	if err := http.ListenAndServe(addr, nil); err != nil {
		log.Fatalf("服务启动失败: %v", err)
	}
}

// handleHardwareInfo 处理硬件信息查询请求
func handleHardwareInfo(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGET {
		w.WriteHeader(http.StatusMethodNotAllowed)
		return
	}

	info, err := hardware.CollectHardwareInfo()
	if err != nil {
		w.Header().Set("Content-Type", "application/json; charset=utf-8")
		w.WriteHeader(http.StatusInternalServerError)
		json.NewEncoder(w).Encode(map[string]string{
			"error": err.Error(),
		})
		return
	}

	w.Header().Set("Content-Type", "application/json; charset=utf-8")
	json.NewEncoder(w).Encode(map[string]interface{}{
		"motherboardSn": info.MotherboardSN,
		"cpuId":         info.CPUID,
		"diskSn":        info.DiskSN,
		"timestamp":     time.Now().Unix(),
		"nonce":         generateNonce(),
	})
}

// handleHealth 健康检查接口
func handleHealth(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json; charset=utf-8")
	json.NewEncoder(w).Encode(map[string]string{
		"status": "ok",
	})
}

// generateNonce 生成随机nonce
func generateNonce() string {
	return time.Now().Format("20060102150405") + "x"
}
