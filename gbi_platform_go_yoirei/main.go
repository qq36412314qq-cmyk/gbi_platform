package main

import (
	"bytes"
	"context"
	"embed"
	"encoding/json"
	"fmt"
	"io"
	"log"
	"net"
	"net/http"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"sync"
	"time"

	"github.com/getlantern/systray"
)

//go:embed resources/app.ico
var iconFS embed.FS

//go:embed resources/settings.html
var settingsHTML string

const defaultPort = "8765"
const configFileName = "yoirei_config.json"
const lockFileName = "yoirei.lock"

type HardWareInfo struct {
	MotherboardSN string `json:"motherboardSn"`
	CPUID         string `json:"cpuId"`
	DiskSN        string `json:"diskSn,omitempty"`
	Timestamp     int64  `json:"timestamp"`
	Nonce         string `json:"nonce"`
}

type DeviceAuthState struct {
	ServerUrl    string `json:"serverUrl"`
	MotherboardSn string `json:"motherboardSn"`
	CpuId        string `json:"cpuId"`
	DiskSn       string `json:"diskSn"`
	DeviceId     int64  `json:"deviceId"`
	Enabled      bool   `json:"enabled"`
}

type AppConfig struct {
	ServerUrl string `json:"serverUrl"`
}

var (
	appDir       string
	logFile      *os.File
	appConfig    AppConfig
	authState    DeviceAuthState
	authStateMu  sync.Mutex
	mu           sync.Mutex
	lockFile     *os.File
)

func initAppDir() {
	exe, err := os.Executable()
	if err != nil {
		appDir = "."
		return
	}
	appDir = filepath.Dir(exe)
}

func initLog() {
	logPath := filepath.Join(appDir, "yoirei.log")
	var err error
	logFile, err = os.OpenFile(logPath, os.O_CREATE|os.O_WRONLY|os.O_APPEND, 0644)
	if err != nil {
		fmt.Fprintf(os.Stderr, "[错误] 无法打开日志文件: %v\n", err)
		return
	}
	log.SetOutput(logFile)
	log.SetFlags(log.Ldate | log.Ltime | log.Lmicroseconds)
	log.Printf("[初始化] 程序目录: %s", appDir)
	logFile.Sync()
}

func closeLog() {
	if logFile != nil {
		logFile.Close()
	}
}

func tryAcquireLock() bool {
    lockPath := filepath.Join(appDir, lockFileName)
    // 尝试读取已有lock文件
    data, err := os.ReadFile(lockPath)
    if err == nil {
        var oldPid int
        fmt.Sscanf(string(data), "%d", &oldPid)
        // 判断旧PID进程是否还在运行
        if isProcessRunning(oldPid) {
            fmt.Fprintf(os.Stderr, "[锁定] 进程 %d 正在运行\n", oldPid)
            return false
        }
        log.Printf("[锁定] 检测到残留lock文件，进程%d已退出，清理锁", oldPid)
        os.Remove(lockPath)
    }

    f, err := os.OpenFile(lockPath, os.O_CREATE|os.O_WRONLY, 0644)
    if err != nil {
        fmt.Fprintf(os.Stderr, "[锁定] 无法创建锁定文件: %v\n", err)
        return false
    }
    pid := os.Getpid()
    _, err = f.WriteString(fmt.Sprintf("%d\n", pid))
    if err != nil {
        f.Close()
        return false
    }
    f.Sync()
    lockFile = f
    log.Printf("[锁定] 进程ID %d 已获取锁", pid)
    return true
}

// 新增辅助函数：判断Windows进程是否存活
func isProcessRunning(pid int) bool {
    if pid <= 0 {
        return false
    }
    cmd := exec.Command("tasklist", "/FI", fmt.Sprintf("PID eq %d", pid))
    out, err := cmd.Output()
    if err != nil {
        return false
    }
    return strings.Contains(string(out), fmt.Sprintf("%d", pid))
}


func releaseLock() {
	if lockFile != nil {
		lockFile.Close()
		lockFile = nil
	}
	lockPath := filepath.Join(appDir, lockFileName)
	os.Remove(lockPath)
}

func loadConfig() {
	configPath := filepath.Join(appDir, configFileName)
	data, err := os.ReadFile(configPath)
	if err != nil {
		log.Printf("[配置] 使用默认配置")
		appConfig = AppConfig{ServerUrl: "http://127.0.0.1:8080"}
		return
	}
	if err := json.Unmarshal(data, &appConfig); err != nil {
		log.Printf("[配置] 解析失败，使用默认: %v", err)
		appConfig = AppConfig{ServerUrl: "http://127.0.0.1:8080"}
		return
	}
	log.Printf("[配置] 已加载: %s", appConfig.ServerUrl)
}

func saveConfig(cfg AppConfig) error {
	data, err := json.MarshalIndent(cfg, "", "  ")
	if err != nil {
		return err
	}
	configPath := filepath.Join(appDir, configFileName)
	return os.WriteFile(configPath, data, 0644)
}

func loadAuthState() {
	statePath := filepath.Join(appDir, "yoirei_state.json")
	data, err := os.ReadFile(statePath)
	if err != nil {
		authState = DeviceAuthState{Enabled: true}
		return
	}
	if err := json.Unmarshal(data, &authState); err != nil {
		authState = DeviceAuthState{Enabled: true}
		return
	}
	log.Printf("[授权状态] 已加载 deviceId=%d enabled=%v", authState.DeviceId, authState.Enabled)
}

func saveAuthState() error {
	data, err := json.MarshalIndent(authState, "", "  ")
	if err != nil {
		return err
	}
	statePath := filepath.Join(appDir, "yoirei_state.json")
	return os.WriteFile(statePath, data, 0644)
}

// reportAuth 向后端注册/更新设备授权记录，返回true表示成功
func reportAuth() bool {
	mu.Lock()
	serverUrl := appConfig.ServerUrl
	mu.Unlock()

	if serverUrl == "" {
		log.Printf("[授权上报] 后端地址未配置，跳过上报")
		return false
	}

	info := collectHardwareInfo()
	body, _ := json.Marshal(map[string]string{
		"motherboardSn": info.MotherboardSN,
		"cpuId":         info.CPUID,
		"diskSn":        info.DiskSN,
	})

	reqUrl := serverUrl + "/sys/device/auth/register"
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	req, err := http.NewRequestWithContext(ctx, "POST", reqUrl, bytes.NewReader(body))
	if err != nil {
		log.Printf("[授权上报] 创建请求失败: %v", err)
		return false
	}
	req.Header.Set("Content-Type", "application/json; charset=utf-8")

	client := &http.Client{}
	resp, err := client.Do(req)
	if err != nil {
		log.Printf("[授权上报] 请求后端失败: %v", err)
		return false
	}
	defer resp.Body.Close()

	respBody, _ := io.ReadAll(resp.Body)
	log.Printf("[授权上报] 后端响应 status=%d body=%s", resp.StatusCode, string(respBody))

	var result map[string]interface{}
	if err := json.Unmarshal(respBody, &result); err != nil {
		log.Printf("[授权上报] 解析响应失败: %v", err)
		return false
	}

	if resp.StatusCode >= 200 && resp.StatusCode < 300 {
		var respData struct {
			Code    int    `json:"code"`
			Msg     string `json:"msg"`
			Data    struct {
				Action  string `json:"action"`
				DeviceId int64 `json:"deviceId"`
			} `json:"data"`
		}
		if err := json.Unmarshal(respBody, &respData); err != nil {
			log.Printf("[授权上报] 解析响应失败: %v", err)
			return false
		}
		if respData.Data.DeviceId > 0 {
			authStateMu.Lock()
			authState.MotherboardSn = info.MotherboardSN
			authState.CpuId = info.CPUID
			authState.DiskSn = info.DiskSN
			authState.DeviceId = respData.Data.DeviceId
			saveAuthState()
			authStateMu.Unlock()
			log.Printf("[授权上报] 成功 deviceId=%d action=%s", respData.Data.DeviceId, respData.Data.Action)
			return true
		}
		log.Printf("[授权上报] 响应无deviceId code=%d msg=%s", respData.Code, respData.Msg)
		return false
	}
	log.Printf("[授权上报] 后端返回异常 status=%d body=%s", resp.StatusCode, string(respBody))
	return false
}

// autoReportLoop 启动后延迟1秒首次上报，之后每24小时周期上报
func autoReportLoop() {
	time.Sleep(1 * time.Second)
	reportAuth()
	ticker := time.NewTicker(24 * time.Hour)
	defer ticker.Stop()
	for range ticker.C {
		reportAuth()
	}
}

func openLogFile() {
	if logFile != nil {
		logFile.Sync()
	}
	path := filepath.Join(appDir, "yoirei.log")
	exec.Command("cmd", "/c", "start", "", path).Start()
}

func createShortcut() {
    exePath, _ := os.Executable()
    desktopPath := os.Getenv("USERPROFILE") + "\\Desktop"
    // 固定快捷方式名称 Yoirei.lnk
    shortcutPath := filepath.Join(desktopPath, "Yoirei.lnk")

    exeDir := filepath.Dir(exePath)
    icoPath := filepath.Join(exeDir, "icon64.ico")
    // 容错：找不到icon64.ico就使用exe自身图标
    iconTarget := exePath
    if _, err := os.Stat(icoPath); err == nil {
        iconTarget = icoPath
        log.Printf("[快捷方式] 使用外部图标: %s", icoPath)
    }else{
        log.Printf("[快捷方式] 未找到icon64.ico，回退使用exe内置图标")
    }

    if _, err := os.Stat(shortcutPath); err == nil {
        log.Printf("[快捷方式] 已存在: %s", shortcutPath)
        return
    }

    // 注意：powershell内部字符串，把"替换成\"来转义，避免路径空格bug
    psCmd := fmt.Sprintf(`
$WshShell = New-Object -ComObject WScript.Shell
$Shortcut = $WshShell.CreateShortcut("%s")
$Shortcut.TargetPath = "%s"
$Shortcut.WorkingDirectory = "%s"
$Shortcut.Description = "GBI Yoirei 设备校验代理"
$Shortcut.IconLocation = "%s,0"
$Shortcut.Save()
`, shortcutPath, exePath, appDir, iconTarget)

    cmd := exec.Command("powershell", "-Command", psCmd)
    out, err := cmd.CombinedOutput()
    if err != nil {
        log.Printf("[快捷方式] 创建失败: %v 输出:%s", err, string(out))
    } else {
        log.Printf("[快捷方式] 已创建桌面快捷方式: %s", shortcutPath)
    }
}

func extractIcon() []byte {
    // embed.FS根目录是项目根，路径是 resources/app.ico
    data, err := iconFS.ReadFile("resources/app.ico")
    if err != nil {
        log.Printf("[托盘] 读取嵌入图标失败: %v", err)
        return nil
    }
    log.Printf("[托盘] 图标已加载: app.ico (%d 字节)", len(data))
    return data
}

func collectHardwareInfo() *HardWareInfo {
    info := &HardWareInfo{
        Timestamp: time.Now().Unix(),
        Nonce:     generateNonce(),
    }
    info.MotherboardSN = execPS(`(Get-CimInstance -ClassName Win32_BaseBoard).SerialNumber`)
    info.CPUID = execPS(`(Get-CimInstance -ClassName Win32_Processor).ProcessorId`)
    info.DiskSN = execPS(`(Get-CimInstance -ClassName Win32_DiskDrive | Select-Object -First 1).SerialNumber`)
    return info
}

func execPS(cmd string) string {
    ctx, cancel := context.WithTimeout(context.Background(), 3*time.Second)
    defer cancel()
    out, err := exec.CommandContext(ctx, "powershell", "-Command", cmd).Output()
    if err != nil {
        log.Printf("[powershell] 执行失败: %v", err)
        return ""
    }
    return strings.TrimRight(string(out), "\r\n \t")
}


func generateNonce() string {
	return time.Now().Format("20060102150405") + "x"
}

func isPortAvailable(port string) bool {
	conn, err := net.Listen("tcp", "127.0.0.1:"+port)
	if err != nil {
		return false
	}
	conn.Close()
	return true
}

func recoverMiddleware(next http.HandlerFunc) http.HandlerFunc {
    return func(w http.ResponseWriter, r *http.Request) {
        defer func() {
            if err := recover(); err != nil {
                log.Printf("[HTTP Panic] %v", err)
                http.Error(w, "server internal error", http.StatusInternalServerError)
            }
        }()
        next(w, r)
    }
}

// corsMiddleware 添加CORS头，允许任何来源访问
func corsMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		// 允许任何来源
		w.Header().Set("Access-Control-Allow-Origin", "*")
		w.Header().Set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
		w.Header().Set("Access-Control-Allow-Headers", "Origin, Content-Type, Authorization, X-Requested-With")
		
		// 处理预检请求
		if r.Method == http.MethodOptions {
			w.WriteHeader(http.StatusOK)
			return
		}
		
		next.ServeHTTP(w, r)
	})
}

func startHTTPServer(port string) {
	mux := http.NewServeMux()
	
	mux.HandleFunc("/api/hardware/info", recoverMiddleware(func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodGet {
			http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
			return
		}
		info := collectHardwareInfo()
		w.Header().Set("Content-Type", "application/json; charset=utf-8")
		json.NewEncoder(w).Encode(info)
	}))
	
	mux.HandleFunc("/api/config/get", func(w http.ResponseWriter, r *http.Request) {
		mu.Lock()
		cfg := appConfig
		mu.Unlock()
		w.Header().Set("Content-Type", "application/json; charset=utf-8")
		json.NewEncoder(w).Encode(cfg)
	})
	
	mux.HandleFunc("/api/config/save", func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPost {
			http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
			return
		}
		var cfg AppConfig
		if err := json.NewDecoder(r.Body).Decode(&cfg); err != nil {
			http.Error(w, "Invalid request body", http.StatusBadRequest)
			return
		}
		mu.Lock()
		appConfig = cfg
		mu.Unlock()
		if err := saveConfig(cfg); err != nil {
			http.Error(w, "Save failed", http.StatusInternalServerError)
			return
		}
		w.Header().Set("Content-Type", "application/json; charset=utf-8")
		json.NewEncoder(w).Encode(map[string]string{"status": "ok"})
	})
	
	// 查询当前授权状态
	mux.HandleFunc("/api/auth/status", func(w http.ResponseWriter, r *http.Request) {
		if r.Method == http.MethodPost {
			// 更新开关状态
			var req struct { Enabled bool `json:"enabled"` }
			if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
				http.Error(w, "Invalid request", http.StatusBadRequest)
				return
			}
			authStateMu.Lock()
			authState.Enabled = req.Enabled
			saveAuthState()
			authStateMu.Unlock()
			w.Header().Set("Content-Type", "application/json; charset=utf-8")
			json.NewEncoder(w).Encode(map[string]string{"status": "ok"})
			return
		}
		authStateMu.Lock()
		st := authState
		authStateMu.Unlock()
		w.Header().Set("Content-Type", "application/json; charset=utf-8")
		json.NewEncoder(w).Encode(st)
	})
	
	// 手动触发上报
	mux.HandleFunc("/api/auth/report", func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPost {
			http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
			return
		}
		if reportAuth() {
			w.Header().Set("Content-Type", "application/json; charset=utf-8")
			json.NewEncoder(w).Encode(map[string]string{"status": "ok"})
		} else {
			http.Error(w, "report failed", http.StatusInternalServerError)
		}
	})
	
	mux.HandleFunc("/settings", func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "text/html; charset=utf-8")
		w.Write([]byte(settingsHTML))
	})
	
	mux.HandleFunc("/api/health", func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json; charset=utf-8")
		json.NewEncoder(w).Encode(map[string]string{"status": "ok"})
	})
	mux.HandleFunc("/health", func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json; charset=utf-8")
		json.NewEncoder(w).Encode(map[string]string{"status": "ok"})
	})

	addr := "127.0.0.1:" + port
	log.Printf("[HTTP] 服务已启动: http://%s", addr)
	
	// 关键修改：使用CORS中间件
	handler := corsMiddleware(mux)
	
	if err := http.ListenAndServe(addr, handler); err != nil {
		log.Printf("[HTTP] 服务启动失败: %v", err)
	}
}


func main() {
	initAppDir()
	initLog()
	defer closeLog()
	defer releaseLock()

	if !tryAcquireLock() {
		log.Printf("[锁定] 检测到其他实例正在运行，退出当前进程")
		fmt.Println("检测到另一个 Yoirei 实例正在运行，请勿重复启动！")
		time.Sleep(2 * time.Second)
		os.Exit(0)
	}

	loadConfig()
	loadAuthState()
	createShortcut()

	port := defaultPort
	if !isPortAvailable(port) {
		log.Printf("[端口] %s 已被占用，尝试获取其他端口", port)
		for i := 1; i <= 10; i++ {
			testPort := fmt.Sprintf("%d", 8765+i)
			if isPortAvailable(testPort) {
				port = testPort
				log.Printf("[端口] 使用备用端口: %s", port)
				break
			}
		}
	}

	log.Printf("GBI Yoirei 设备校验程序 启动中 (端口: %s)", port)

	iconData := extractIcon()

	go startHTTPServer(port)

	// 启动后异步上报硬件信息（含定时周期上报）
	go autoReportLoop()

	systray.Run(func() {
		if iconData != nil && len(iconData) > 0 {
			systray.SetIcon(iconData)
			log.Printf("[托盘] 图标已设置 (大小: %d 字节)", len(iconData))
		} else {
			log.Printf("[托盘] 警告: 图标数据为空")
		}
		systray.SetTitle("GBI Yoirei")
		systray.SetTooltip("GBI设备校验代理")

		mSettings := systray.AddMenuItem("设置...", "打开设置界面")
		go func() {
			<-mSettings.ClickedCh
			exec.Command("cmd", "/c", "start", "", "http://127.0.0.1:"+port+"/settings").Start()
		}()

		mLog := systray.AddMenuItem("查看日志", "打开日志文件")
		go func() {
			<-mLog.ClickedCh
			openLogFile()
		}()

		mExit := systray.AddMenuItem("退出", "退出程序")
		go func() {
			<-mExit.ClickedCh
			log.Println("[托盘] 用户点击退出")
			systray.Quit()
		}()
	}, func() {
		log.Println("[托盘] 托盘已初始化")
	})
}
