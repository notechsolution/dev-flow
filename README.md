# DevFlow - 需求管理与开发流程工具

## 功能特性

- 🎯 智能需求优化 - AI 辅助优化用户故事
- 🤖 多 AI 模型支持 - 支持 Qwen (通义千问) 和 Ollama 本地模型
- 📝 需求澄清 - 自动生成澄清问题
- ✅ 测试用例生成 - AI 自动生成测试用例
- 🔐 OAuth2 集成 - 支持 GitHub 等第三方登录
- 📊 项目管理 - 完整的项目和需求管理

## AI 模型支持

DevFlow 支持多种 AI 模型提供商，可通过配置轻松切换：

- **Qwen (通义千问)** - 阿里云 DashScope，云端托管 (默认)
- **Ollama** - 本地部署的开源模型，支持隐私保护

详见 [AI Provider 切换指南](AI_PROVIDER_GUIDE.md)

### 快速切换 AI 模型

```bash
# 使用 Ollama (本地模型)
java -jar devflow.jar --spring.profiles.active=ollama

# 使用 Qwen (云端模型 - 默认)
java -jar devflow.jar --spring.profiles.active=office
```

## 打包与部署

### Package process:
1. in parent pom, execute mvn clean package
2. java -jar target/xxx.jar

