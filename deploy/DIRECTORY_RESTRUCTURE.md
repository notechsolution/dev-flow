# 部署目录重组说明

## 📁 目录结构变化

为了更好地组织项目文件，我们将所有部署相关的脚本和文档统一移动到了 `deploy/` 目录下。

### 新的目录结构

```
dev-flow/
├── deploy/                          # 部署相关文件（新）
│   ├── README.md                    # 部署目录说明
│   ├── scripts/                     # 部署脚本
│   │   ├── build.ps1               # Windows 打包脚本
│   │   ├── build.sh                # Linux/Mac 打包脚本
│   │   ├── start-prod.ps1          # Windows 启动脚本
│   │   ├── start-prod.sh           # Linux/Mac 启动脚本
│   │   ├── stop.ps1                # Windows 停止脚本
│   │   ├── stop.sh                 # Linux/Mac 停止脚本
│   │   ├── restart.ps1             # Windows 重启脚本
│   │   └── status.ps1              # Windows 状态检查脚本
│   │
│   └── docs/                        # 部署文档
│       ├── DEPLOYMENT_QUICK_START.md       # 快速部署指南
│       ├── VERSION_MANAGEMENT_GUIDE.md     # 版本管理指南
│       ├── BUILD_SCRIPTS_GUIDE.md          # 打包脚本指南
│       ├── WINDOWS_SCRIPTS_GUIDE.md        # Windows 脚本指南
│       └── IMPLEMENTATION_SUMMARY.md       # 实现总结
│
├── backend/                         # 后端代码
├── frontend/                        # 前端代码
├── scripts/                         # 其他脚本（如 MongoDB 初始化脚本）
├── logs/                           # 应用日志目录
├── README.md                       # 项目主文档（已更新路径）
├── DEPLOYMENT_GUIDE.md            # 完整部署指南
├── TROUBLESHOOTING.md             # 故障排查
├── USER_MANUAL.md                 # 用户手册
└── ...其他文件
```

## 📋 文件移动清单

### 脚本文件（移动到 `deploy/scripts/`）

| 原路径 | 新路径 |
|--------|--------|
| `build.ps1` | `deploy/scripts/build.ps1` |
| `build.sh` | `deploy/scripts/build.sh` |
| `start-prod.ps1` | `deploy/scripts/start-prod.ps1` |
| `start-prod.sh` | `deploy/scripts/start-prod.sh` |
| `stop.ps1` | `deploy/scripts/stop.ps1` |
| `stop.sh` | `deploy/scripts/stop.sh` |
| `restart.ps1` | `deploy/scripts/restart.ps1` |
| `status.ps1` | `deploy/scripts/status.ps1` |

### 文档文件（移动到 `deploy/docs/`）

| 原路径 | 新路径 |
|--------|--------|
| `DEPLOYMENT_QUICK_START.md` | `deploy/docs/DEPLOYMENT_QUICK_START.md` |
| `VERSION_MANAGEMENT_GUIDE.md` | `deploy/docs/VERSION_MANAGEMENT_GUIDE.md` |
| `BUILD_SCRIPTS_GUIDE.md` | `deploy/docs/BUILD_SCRIPTS_GUIDE.md` |
| `WINDOWS_SCRIPTS_GUIDE.md` | `deploy/docs/WINDOWS_SCRIPTS_GUIDE.md` |
| `IMPLEMENTATION_SUMMARY.md` | `deploy/docs/IMPLEMENTATION_SUMMARY.md` |

## 🔄 使用方式变化

### 之前的使用方式

```powershell
# 打包
.\build.ps1

# 启动
.\start-prod.ps1

# 停止
.\stop.ps1
```

### 现在的使用方式

```powershell
# 进入部署脚本目录
cd deploy\scripts

# 打包
.\build.ps1

# 启动
.\start-prod.ps1

# 停止
.\stop.ps1
```

**或者使用相对路径：**

```powershell
# 从项目根目录
deploy\scripts\build.ps1
deploy\scripts\start-prod.ps1
deploy\scripts\stop.ps1
```

## 📝 文档路径更新

主 `README.md` 已更新，所有文档链接指向新位置：

- 快速部署指南：`deploy/docs/DEPLOYMENT_QUICK_START.md`
- 版本管理指南：`deploy/docs/VERSION_MANAGEMENT_GUIDE.md`
- Windows 脚本指南：`deploy/docs/WINDOWS_SCRIPTS_GUIDE.md`
- 打包脚本指南：`deploy/docs/BUILD_SCRIPTS_GUIDE.md`
- 实现总结：`deploy/docs/IMPLEMENTATION_SUMMARY.md`

## ✅ 优势

### 1. 更清晰的组织结构
- 部署相关文件集中管理
- 便于查找和维护
- 避免根目录文件过多

### 2. 更好的可维护性
- 脚本和文档放在一起
- 便于整体迁移和备份
- 方便版本控制

### 3. 更规范的项目结构
- 符合常见项目规范
- 便于新成员理解项目结构
- 易于扩展和添加新脚本

## 🚀 快速访问

### 查看部署文档
```bash
cd deploy
cat README.md
```

### 进入脚本目录
```bash
cd deploy/scripts
ls -la          # Linux/Mac
dir             # Windows
```

### 进入文档目录
```bash
cd deploy/docs
ls -la          # Linux/Mac
dir             # Windows
```

## 💡 提示

1. **收藏常用路径**
   - 将 `deploy/scripts/` 添加到快速访问
   - 创建桌面快捷方式到常用脚本

2. **使用别名**（可选）
   ```powershell
   # PowerShell 配置文件中添加
   New-Alias -Name dfbuild -Value "D:\githome\github\dev-flow\deploy\scripts\build.ps1"
   New-Alias -Name dfstart -Value "D:\githome\github\dev-flow\deploy\scripts\start-prod.ps1"
   New-Alias -Name dfstop -Value "D:\githome\github\dev-flow\deploy\scripts\stop.ps1"
   ```

3. **环境变量**（可选）
   ```powershell
   # 添加到 PATH
   $env:PATH += ";D:\githome\github\dev-flow\deploy\scripts"
   ```

## 📞 获取帮助

如有任何问题或建议，请查看：
- [部署目录 README](deploy/README.md)
- [项目主 README](README.md)
- [快速部署指南](deploy/docs/DEPLOYMENT_QUICK_START.md)
