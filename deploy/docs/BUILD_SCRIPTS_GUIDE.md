# 打包脚本使用说明

## Windows PowerShell 脚本

项目提供了两个 PowerShell 打包脚本：

### 1. build.ps1 (英文版 - 推荐)

**特点：**
- 英文界面，避免中文编码问题
- 更好的跨平台兼容性
- 推荐在自动化环境中使用

**使用方法：**

```powershell
# 基本打包
.\build.ps1

# 指定版本号打包
.\build.ps1 -Version 1.0.0
```

### 2. build-zh.ps1 (中文版)

**特点：**
- 中文界面，更易理解
- 需要确保 PowerShell 使用 UTF-8 编码
- 如果遇到编码问题，请使用 build.ps1

**使用方法：**

```powershell
# 基本打包
.\build-zh.ps1

# 指定版本号打包
.\build-zh.ps1 -Version 1.0.0
```

**解决编码问题：**

如果 build-zh.ps1 报错，请设置 PowerShell 编码：

```powershell
# 临时设置当前会话编码
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

# 然后运行脚本
.\build-zh.ps1
```

或者直接使用英文版：

```powershell
.\build.ps1
```

## Linux/Mac Bash 脚本

### build.sh

**使用方法：**

```bash
# 添加执行权限（首次使用）
chmod +x build.sh

# 基本打包
./build.sh

# 指定版本号打包
./build.sh 1.0.0
```

## 打包输出

所有脚本成功后会显示：

1. **构建产物位置**
   - Backend JAR: `backend/target/devflow.jar`
   - Frontend files: `frontend/target/dist/`

2. **版本信息**
   - 应用版本号
   - 构建时间

3. **JAR 文件大小**

4. **下一步操作提示**

## 常见问题

### Q1: PowerShell 脚本报编码错误

**现象：**
```
The string is missing the terminator: ".
```

**解决方案：**
- 使用 `build.ps1`（英文版）
- 或设置 UTF-8 编码后使用 `build-zh.ps1`

### Q2: 权限不足无法执行

**Windows:**
```powershell
# 查看执行策略
Get-ExecutionPolicy

# 临时允许执行（当前会话）
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
```

**Linux/Mac:**
```bash
chmod +x build.sh
```

### Q3: mvn 命令未找到

**解决方案：**
- 确保已安装 Maven
- 将 Maven bin 目录添加到 PATH 环境变量

**验证：**
```bash
mvn --version
```

### Q4: 构建失败

**检查步骤：**
1. 查看错误信息
2. 确保 Java 环境正确（JDK 17+）
3. 确保 MongoDB 可访问（如果需要）
4. 检查网络连接（下载依赖）

**清理后重试：**
```powershell
mvn clean
mvn package -DskipTests
```

## 快速参考

| 操作 | Windows | Linux/Mac |
|------|---------|-----------|
| 基本打包 | `.\build.ps1` | `./build.sh` |
| 指定版本 | `.\build.ps1 -Version 1.0.0` | `./build.sh 1.0.0` |
| 设置编码 | `[Console]::OutputEncoding = [System.Text.Encoding]::UTF8` | `export LANG=en_US.UTF-8` |
| 执行权限 | 不需要 | `chmod +x build.sh` |

## 推荐做法

1. **日常开发**：使用 `build.ps1` 或 `build.sh`
2. **自动化 CI/CD**：使用 `build.ps1`（英文版，避免编码问题）
3. **手动打包**：直接运行 `mvn clean package -DskipTests`

## 相关文档

- [快速部署指南](DEPLOYMENT_QUICK_START.md)
- [版本管理指南](VERSION_MANAGEMENT_GUIDE.md)
- [实现总结](IMPLEMENTATION_SUMMARY.md)
