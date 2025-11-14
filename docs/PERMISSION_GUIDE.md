# 用户权限管理系统

## 角色定义

系统包含三种用户角色，每种角色具有不同的权限级别：

### 1. OPERATOR（操作员）
- **权限范围**：全局
- **能力**：
  - ✅ 管理所有项目和 User Story
  - ✅ 创建、编辑、删除所有用户
  - ✅ 访问和修改任何项目的数据
  - ✅ 查看所有系统数据

### 2. ADMIN（管理员）
- **权限范围**：指定项目
- **能力**：
  - ✅ 管理分配给自己的项目
  - ✅ 创建和管理项目内的用户（不能删除用户）
  - ✅ 管理项目内的 User Story
  - ✅ 查看项目相关数据
  - ❌ 无法访问未分配的项目
  - ❌ 无法删除用户

### 3. USER（普通用户）
- **权限范围**：指定项目（只读/创建）
- **能力**：
  - ✅ 访问分配给自己的项目
  - ✅ 创建 User Story
  - ✅ 查看项目内的 User Story
  - ❌ 无法管理用户
  - ❌ 无法删除或修改其他人的 User Story
  - ❌ 无法访问未分配的项目

## API 权限矩阵

| API 端点 | OPERATOR | ADMIN | USER |
|---------|----------|-------|------|
| POST /api/users | ✅ | ✅ | ❌ |
| GET /api/users | ✅ | ✅ | ❌ |
| PUT /api/users/{id} | ✅ | ✅ | ❌ |
| DELETE /api/users/{id} | ✅ | ❌ | ❌ |
| POST /api/user-stories | ✅ | ✅ | ✅ |
| GET /api/user-stories | ✅ | ✅ | ✅ |
| PUT /api/user-stories/{id} | ✅ | ✅ | ❌ |
| DELETE /api/user-stories/{id} | ✅ | ✅ | ❌ |

## 前端页面访问控制

### 侧边栏菜单权限
- **Dashboard**: 所有角色可见
- **User Story Creation**: 所有角色可见
- **用户管理**: 仅 OPERATOR 和 ADMIN 可见
- **Project**: 仅 OPERATOR 和 ADMIN 可见
- **Metering**: 仅 OPERATOR 和 ADMIN 可见

### 页面级权限
- `/dashboard`: 所有角色
- `/requirement/UserStoryCreation`: 所有角色
- `/requirement/UserStoryDetail/:id`: 所有角色（但只能访问自己项目的）
- `/users`: 仅 OPERATOR 和 ADMIN

## 实现细节

### 后端权限控制

#### 1. Spring Security 注解
```java
@PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
@PreAuthorize("hasRole('OPERATOR')")
```

#### 2. Service 层权限检查
```java
// UserManagementService 提供权限检查方法
boolean hasProjectAccess(String userId, String projectId);
boolean isProjectAdmin(String userId, String projectId);
boolean isOperator(String userId);
```

#### 3. 项目访问控制
- OPERATOR: 可访问所有项目
- ADMIN: 只能访问 projectIds 中的项目
- USER: 只能访问 projectIds 中的项目

### 前端权限控制

#### 1. 路由守卫
```typescript
// 在路由配置中添加 meta 信息
meta: { 
    requiresAuth: true,
    roles: ['OPERATOR', 'ADMIN']
}
```

#### 2. 组件级控制
```vue
<!-- 根据角色显示/隐藏元素 -->
<el-button v-if="store.role === 'OPERATOR'">删除</el-button>
<el-menu-item v-if="store.role === 'ADMIN' || store.role === 'OPERATOR'">
```

#### 3. API 调用
- 所有 API 调用自动携带用户认证信息
- 后端根据用户角色过滤返回数据

## 数据库设计

### User 实体
```java
{
    "id": "string",
    "username": "string",
    "email": "string",
    "password": "string (hashed)",
    "role": "OPERATOR|ADMIN|USER",
    "projectIds": ["string"], // 用户可访问的项目ID列表
    "createdBy": "string",
    "createdAt": "datetime",
    "updatedBy": "string",
    "updatedAt": "datetime"
}
```

### Project 实体
```java
{
    "id": "string",
    "name": "string",
    "description": "string",
    "ownerId": "string",
    "memberIds": ["string"], // 项目成员ID列表
    "adminIds": ["string"],  // 项目管理员ID列表
    "status": "ACTIVE|ARCHIVED|DELETED"
}
```

## 使用示例

### 创建管理员用户
```bash
POST /api/users
{
    "username": "admin1",
    "email": "admin1@example.com",
    "password": "password123",
    "role": "ADMIN",
    "projectIds": ["project-alpha", "project-beta"]
}
```

### 创建普通用户
```bash
POST /api/users
{
    "username": "user1",
    "email": "user1@example.com",
    "password": "password123",
    "role": "USER",
    "projectIds": ["project-alpha"]
}
```

### 权限检查示例
```java
// 检查用户是否有项目访问权限
boolean hasAccess = userManagementService.hasProjectAccess(userId, projectId);

// 检查用户是否是项目管理员
boolean isAdmin = userManagementService.isProjectAdmin(userId, projectId);
```

## 安全建议

1. **密码安全**: 使用 BCrypt 加密存储密码
2. **JWT Token**: 使用 JWT 进行用户认证
3. **HTTPS**: 生产环境必须使用 HTTPS
4. **输入验证**: 所有输入必须经过验证
5. **日志记录**: 记录所有权限相关操作
6. **定期审计**: 定期检查用户权限配置

## 常见问题

### Q: 如何给用户添加新项目权限？
A: 通过 PUT /api/users/{id} 接口，更新用户的 projectIds 数组。

### Q: OPERATOR 和 ADMIN 的区别？
A: OPERATOR 可以管理所有项目和用户，ADMIN 只能管理分配给自己的项目。

### Q: 如何限制用户只能看到自己创建的 User Story？
A: 在 UserStoryService 中添加过滤逻辑，根据用户角色和 createdBy 字段过滤数据。

### Q: 如何实现项目级的权限控制？
A: 通过 User 实体的 projectIds 字段和 Project 实体的 memberIds、adminIds 字段配合实现。
