# DevFlow 部署目录

本目录包含 DevFlow 项目的所有部署相关脚本和文档。

## 📁 目录结构

```
deploy/
├── scripts/              # 部署和运维脚本
│   ├── build.ps1        # Windows 打包脚本（英文）
│   ├── build.sh         # Linux/Mac 打包脚本
│   ├── start-prod.ps1   # Windows 启动脚本
│   ├── start-prod.sh    # Linux/Mac 启动脚本
│   ├── stop.ps1         # Windows 停止脚本
│   ├── stop.sh          # Linux/Mac 停止脚本
│   ├── restart.ps1      # Windows 重启脚本
│   └── status.ps1       # Windows 状态检查脚本
│
└── docs/                # 部署相关文档
    ├── DEPLOYMENT_QUICK_START.md      # 快速部署指南
    ├── VERSION_MANAGEMENT_GUIDE.md    # 版本管理指南
    ├── BUILD_SCRIPTS_GUIDE.md         # 打包脚本使用指南
    ├── WINDOWS_SCRIPTS_GUIDE.md       # Windows 脚本使用指南
    └── IMPLEMENTATION_SUMMARY.md      # 实现总结文档
```

## 🚀 快速开始

### Windows 环境

```powershell
# 打包项目
cd deploy\scripts
.\build.ps1

# 启动应用
.\start-prod.ps1

# 检查状态
.\status.ps1

# 停止应用
.\stop.ps1

# 重启应用
.\restart.ps1
```

### Linux/Mac 环境

```bash
# 打包项目
cd deploy/scripts
chmod +x *.sh
./build.sh

# 启动应用
./start-prod.sh

# 停止应用
./stop.sh
```

## 📚 文档导航

### 核心文档

- **[快速部署指南](docs/DEPLOYMENT_QUICK_START.md)** - 5分钟快速部署到生产环境
  - 开发环境打包
  - 生产环境部署步骤
  - 更新流程
  - 常用运维命令
  - Docker 部署

- **[版本管理指南](docs/VERSION_MANAGEMENT_GUIDE.md)** - 完整的版本管理方案
  - 版本号来源和管理
  - Maven 配置
  - UI 版本显示
  - Git 集成
  - 版本命名规范

- **[打包脚本指南](docs/BUILD_SCRIPTS_GUIDE.md)** - 打包脚本详细使用说明
  - Windows 和 Linux/Mac 脚本使用
  - 常见问题解决
  - 编码问题处理

- **[Windows 脚本指南](docs/WINDOWS_SCRIPTS_GUIDE.md)** - Windows PowerShell 脚本完整说明
  - 所有脚本详细使用方法
  - 常见操作示例
  - 故障排查
  - 最佳实践

- **[实现总结](docs/IMPLEMENTATION_SUMMARY.md)** - 技术实现细节和检查清单

## 🛠️ 脚本说明

### 打包脚本

#### build.ps1 / build.sh
构建并打包项目，生成可部署的 JAR 文件。

**功能：**
- 清理旧文件
- 执行 Maven 打包
- 显示构建结果和版本信息
- 支持指定版本号

**使用：**
```powershell
# Windows
.\build.ps1                    # 使用当前版本
.\build.ps1 -Version 1.0.0     # 指定版本

# Linux/Mac
./build.sh                     # 使用当前版本
./build.sh 1.0.0               # 指定版本
```

### Windows 运维脚本

#### start-prod.ps1
启动 DevFlow 应用。

**功能：**
- 配置环境变量（AI、数据库、邮件）
- 检查 JAR 文件和端口
- 后台启动应用
- 日志重定向
- 健康检查
- 显示版本信息

**使用前：** 编辑脚本配置环境变量

#### stop.ps1
停止 DevFlow 应用。

**功能：**
- 读取 PID 文件
- 优雅关闭（30秒超时）
- 强制终止（如需要）
- 清理 PID 文件

#### restart.ps1
重启 DevFlow 应用。

**功能：**
- 调用 stop.ps1 停止应用
- 等待 3 秒
- 调用 start-prod.ps1 启动应用

#### status.ps1
检查应用状态。

**功能：**
- 进程状态检查
- 运行时长和资源使用
- 端口监听状态
- 健康检查 API
- 版本信息 API
- 综合状态报告

### Linux/Mac 运维脚本

#### start-prod.sh
启动 DevFlow 应用（Linux/Mac 版本）。

**功能：**
- 配置环境变量
- 检查依赖
- 后台启动
- 日志管理
- 健康检查

**使用前：** 
1. 添加执行权限：`chmod +x start-prod.sh`
2. 编辑脚本配置环境变量

#### stop.sh
停止 DevFlow 应用（Linux/Mac 版本）。

**功能：**
- 优雅关闭
- 强制终止（如需要）
- PID 文件管理

## 📖 使用场景

### 场景 1: 开发环境测试

```powershell
# 1. 打包项目
cd deploy\scripts
.\build.ps1

# 2. 启动应用
.\start-prod.ps1

# 3. 查看日志
Get-Content ..\..\logs\devflow.log -Wait -Tail 50

# 4. 测试完成后停止
.\stop.ps1
```

### 场景 2: 生产环境部署

```powershell
# 1. 在开发机器上打包
.\build.ps1

# 2. 上传到服务器
# 将 backend/target/devflow.jar 和 deploy/scripts/* 上传到服务器

# 3. 在服务器上配置并启动
cd /app/deploy/scripts
# 编辑 start-prod.ps1 或 start-prod.sh
.\start-prod.ps1  # 或 ./start-prod.sh

# 4. 验证部署
.\status.ps1  # 或使用 curl 测试 API
```

### 场景 3: 版本更新

```powershell
# 1. 停止当前版本
.\stop.ps1

# 2. 备份旧版本
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
Copy-Item backend\target\devflow.jar "devflow.jar.backup.$timestamp"

# 3. 部署新版本
# 将新的 JAR 文件复制到服务器

# 4. 启动新版本
.\start-prod.ps1

# 5. 验证版本
(Invoke-RestMethod -Uri http://localhost:8099/api/version).version
```

### 场景 4: 故障排查

```powershell
# 1. 检查状态
.\status.ps1

# 2. 查看错误日志
Get-Content ..\..\logs\devflow.log | Select-String "ERROR"

# 3. 尝试重启
.\restart.ps1

# 4. 如果问题持续，查看详细日志
Get-Content ..\..\logs\devflow.log -Tail 200
```

## ⚠️ 重要提示

### 首次使用

1. **编辑启动脚本配置**
   - Windows: 编辑 `scripts/start-prod.ps1`
   - Linux/Mac: 编辑 `scripts/start-prod.sh`
   - 设置正确的环境变量（API Key、数据库连接、邮件配置等）

2. **添加执行权限**（Linux/Mac）
   ```bash
   chmod +x scripts/*.sh
   ```

3. **检查依赖**
   - Java 17 或更高版本
   - Maven 3.6 或更高版本（仅打包时需要）
   - MongoDB 连接可用

### 安全建议

1. **不要在脚本中硬编码敏感信息**
   - 使用环境变量
   - 或从安全的配置文件加载

2. **限制脚本文件权限**（Linux/Mac）
   ```bash
   chmod 700 scripts/*.sh
   ```

3. **定期备份**
   - 备份 JAR 文件
   - 备份日志文件
   - 备份配置文件

## 🔗 相关链接

- [项目主页](../)
- [完整文档](../README.md)
- [用户手册](../USER_MANUAL.md)
- [故障排查](../TROUBLESHOOTING.md)

## 📞 获取帮助

如有问题，请查看：
1. [快速部署指南](docs/DEPLOYMENT_QUICK_START.md)
2. [故障排查文档](../TROUBLESHOOTING.md)
3. 项目日志文件：`../../logs/devflow.log`
