# 项目管理功能说明

## 功能概述

项目管理功能允许用户创建和管理项目，支持与 Git 仓库和项目管理系统（如 Jira）的集成。

## 主要特性

### 1. 项目基本信息
- 项目名称
- 项目描述
- 项目状态（活跃/归档/暂停）
- 项目所有者
- 管理员列表
- 成员列表

### 2. Git 仓库集成
支持多种 Git 平台：
- GitHub
- GitLab
- Bitbucket
- Azure DevOps

配置信息包括：
- Git 平台类型
- Base URL（如 https://github.com）
- 仓库 ID 列表（支持多个仓库）
- Access Token（加密存储，API 响应时隐藏）

### 3. 项目管理系统集成
支持多种项目管理平台：
- Jira
- Azure DevOps
- GitHub Issues
- Trello

配置信息包括：
- 系统类型
- 系统/项目 ID
- Base URL
- Access Token（加密存储，API 响应时隐藏）

## 权限控制

### OPERATOR（全局管理员）
- ✅ 创建项目
- ✅ 查看所有项目
- ✅ 修改任何项目
- ✅ 删除任何项目
- ✅ 分配项目管理员和成员

### ADMIN（项目管理员）
- ❌ 不能创建项目
- ✅ 查看被分配的项目
- ✅ 修改被分配为管理员的项目
- ❌ 不能删除项目
- ✅ 可以修改项目配置和成员

### USER（普通用户）
- ❌ 不能创建项目
- ✅ 查看被分配的项目（只读）
- ❌ 不能修改项目
- ❌ 不能删除项目

## API 接口

### 创建项目
```
POST /api/projects
Authorization: Bearer <token>
Role Required: OPERATOR

Request Body:
{
  "name": "项目名称",
  "description": "项目描述",
  "adminIds": ["user1", "user2"],
  "memberIds": ["user3", "user4"],
  "gitRepository": {
    "type": "GITHUB",
    "baseUrl": "https://github.com",
    "repositoryIds": ["owner/repo1", "owner/repo2"],
    "accessToken": "ghp_xxx"
  },
  "projectManagementSystem": {
    "type": "JIRA",
    "systemId": "PROJECT-KEY",
    "baseUrl": "https://yourcompany.atlassian.net",
    "accessToken": "xxx"
  }
}
```

### 获取项目列表
```
GET /api/projects?name=xxx&status=ACTIVE
Authorization: Bearer <token>
Role Required: OPERATOR, ADMIN, USER

Response:
{
  "success": true,
  "data": [...],
  "total": 10
}
```

### 获取单个项目
```
GET /api/projects/{id}
Authorization: Bearer <token>
Role Required: OPERATOR, ADMIN, USER
```

### 更新项目
```
PUT /api/projects/{id}
Authorization: Bearer <token>
Role Required: OPERATOR or project ADMIN

Request Body:
{
  "name": "新名称",
  "description": "新描述",
  "status": "ARCHIVED",
  ...
}
```

### 删除项目
```
DELETE /api/projects/{id}
Authorization: Bearer <token>
Role Required: OPERATOR
```

## 前端使用

### 访问项目管理页面
1. 登录系统
2. 点击左侧菜单 "项目管理"（仅 OPERATOR 和 ADMIN 可见）
3. 查看项目列表

### 创建项目
1. 点击右上角 "创建项目" 按钮（仅 OPERATOR 可见）
2. 填写项目基本信息
3. 可选：配置 Git 仓库信息
4. 可选：配置项目管理系统信息
5. 选择项目管理员和成员
6. 点击 "创建" 按钮

### 编辑项目
1. 在项目列表中找到要编辑的项目
2. 点击 "编辑" 按钮（OPERATOR 和项目 ADMIN 可见）
3. 修改项目信息
4. 点击 "保存" 按钮

### 查看项目详情
1. 在项目列表中点击 "查看" 按钮
2. 查看项目的所有配置信息（Access Token 不显示）
3. 可直接点击 "编辑" 按钮进入编辑模式（如有权限）

## 安全性

### Access Token 保护
- Access Token 存储在数据库中（建议加密）
- API 响应时不返回 Access Token
- 前端编辑时不预填充 Token（避免意外覆盖）
- 只有在明确提供新 Token 时才更新

### 权限验证
- Controller 层使用 `@PreAuthorize` 注解
- Service 层进行二次权限检查
- 支持细粒度的项目级别权限控制

## 数据模型

### Project Entity
```java
@Document(collection = "projects")
public class Project {
    private String id;
    private String name;
    private String description;
    private String ownerId;
    private String status; // ACTIVE, ARCHIVED, PAUSED
    private List<String> adminIds;
    private List<String> memberIds;
    private GitRepository gitRepository;
    private ProjectManagementSystem projectManagementSystem;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static class GitRepository {
        private String type;
        private String baseUrl;
        private List<String> repositoryIds;
        private String accessToken;
    }
    
    public static class ProjectManagementSystem {
        private String type;
        private String systemId;
        private String baseUrl;
        private String accessToken;
    }
}
```

## 扩展建议

### 1. Token 加密
建议使用 Spring Security Crypto 或类似工具加密存储 Access Token：
```java
@Autowired
private TextEncryptor textEncryptor;

// 保存时加密
project.getGitRepository().setAccessToken(
    textEncryptor.encrypt(token)
);

// 使用时解密
String decryptedToken = textEncryptor.decrypt(
    project.getGitRepository().getAccessToken()
);
```

### 2. 仓库连接测试
添加 API 端点测试 Git 和 PM 系统连接：
```java
@PostMapping("/{id}/test-git-connection")
public ResponseEntity<?> testGitConnection(@PathVariable String id) {
    // 使用 access token 测试连接
    return ResponseEntity.ok(result);
}
```

### 3. 审计日志
记录所有项目修改操作：
```java
@Aspect
public class ProjectAuditAspect {
    @After("execution(* ProjectService.updateProject(..))")
    public void logProjectUpdate(JoinPoint joinPoint) {
        // 记录修改日志
    }
}
```

### 4. Webhook 集成
支持从 Git 和 PM 系统接收 webhook 事件。

### 5. 批量导入
支持从现有 Git 组织或 PM 系统批量导入项目。
