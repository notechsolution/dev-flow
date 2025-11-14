# 项目列表加载功能更新

## 修改说明

已将以下三个页面的项目列表从硬编码改为从后台 API 加载，并且会根据用户角色自动过滤项目列表。

## 修改的文件

### 1. Dashboard.vue (User Story 列表页)
**位置**: `frontend/src/views/Dashboard.vue`

**修改内容**:
- ✅ 添加 `loadProjects()` 方法，从 `/api/projects` 加载项目列表
- ✅ 添加 `loadUsers()` 方法，从 `/api/users` 加载用户列表
- ✅ 在 `onMounted()` 中调用这两个方法
- ✅ 移除了硬编码的 mock 数据

**权限说明**:
- OPERATOR: 可以看到所有项目
- ADMIN: 只能看到自己管理的项目
- USER: 只能看到自己参与的项目

### 2. UserStoryCreation.vue (User Story 创建页)
**位置**: `frontend/src/views/requirement/UserStoryCreation.vue`

**修改内容**:
- ✅ 添加 `loadProjects()` 方法，从 `/api/projects` 加载项目列表
- ✅ 添加 `loadUsers()` 方法，从 `/api/users` 加载用户列表
- ✅ 在 `onMounted()` 中调用这两个方法
- ✅ 移除了硬编码的 mock 数据
- ✅ 添加错误提示

**权限说明**:
- 项目下拉框只显示当前用户有权访问的项目
- Owner 下拉框显示所有用户（用于分配责任人）

### 3. UserManagement.vue (用户管理页)
**位置**: `frontend/src/views/user/UserManagement.vue`

**修改内容**:
- ✅ 添加 `loadProjects()` 方法，从 `/api/projects` 加载项目列表
- ✅ 在 `onMounted()` 中调用该方法
- ✅ 移除了硬编码的 mock 数据

**权限说明**:
- 项目筛选下拉框只显示当前用户有权访问的项目
- 用户创建/编辑时的项目分配下拉框也只显示可访问的项目

## 后端权限控制

后端的 `ProjectController.java` 已经实现了基于角色的权限过滤：

```java
@GetMapping
@PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN', 'USER')")
public ResponseEntity<Map<String, Object>> getAllProjects(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String status) {
    String currentUserId = getCurrentUserId();
    List<ProjectResponse> projects = projectService.getAllProjects(currentUserId, name, status);
    // ...
}
```

`ProjectServiceImpl.java` 中的权限逻辑：

```java
@Override
public List<ProjectResponse> getAllProjects(String currentUserId, String nameFilter, String statusFilter) {
    User currentUser = userRepository.findById(currentUserId).orElseThrow();
    
    List<Project> projects;
    
    // OPERATOR sees all projects
    if ("OPERATOR".equals(currentUser.getRole())) {
        projects = projectRepository.findAll();
    } else {
        // ADMIN and USER see only projects they have access to
        List<Project> ownedProjects = projectRepository.findByOwnerId(currentUserId);
        List<Project> adminProjects = projectRepository.findByAdminId(currentUserId);
        List<Project> memberProjects = projectRepository.findByMemberId(currentUserId);
        
        // Combine and deduplicate
        projects = combineAndDeduplicate(ownedProjects, adminProjects, memberProjects);
    }
    
    // Apply filters...
    return projects.stream().map(this::toProjectResponse).collect(Collectors.toList());
}
```

## API 端点

### GET /api/projects
**权限**: OPERATOR, ADMIN, USER

**返回格式**:
```json
{
  "success": true,
  "data": [
    {
      "id": "project-id-1",
      "name": "Project Alpha",
      "description": "Project description",
      "status": "ACTIVE",
      "ownerId": "user-id",
      "adminIds": ["user1", "user2"],
      "memberIds": ["user3", "user4"],
      "createdAt": "2025-10-30T10:00:00",
      "updatedAt": "2025-10-30T10:00:00"
    }
  ],
  "total": 1
}
```

### GET /api/users
**权限**: OPERATOR, ADMIN

**返回格式**:
```json
{
  "success": true,
  "data": [
    {
      "id": "user-id-1",
      "username": "john.doe",
      "email": "john@example.com",
      "role": "USER",
      "projectIds": ["project-1", "project-2"],
      "createdAt": "2025-10-30T10:00:00"
    }
  ],
  "total": 1
}
```

## 数据流程

```
Frontend Component (onMounted)
    ↓
loadProjects() / loadUsers()
    ↓
aiApi.getProjects() / aiApi.getUsers()
    ↓
HTTP GET /api/projects | /api/users
    ↓
ProjectController.getAllProjects() | UserManagementController.getUsers()
    ↓
ProjectServiceImpl.getAllProjects() | UserManagementServiceImpl.getUsers()
    ↓
权限过滤 (根据当前用户角色)
    ↓
返回过滤后的项目/用户列表
    ↓
Frontend 更新下拉框选项
```

## 测试建议

### 1. OPERATOR 用户测试
```
登录 OPERATOR 账号
→ 访问 Dashboard: 应该看到所有项目
→ 访问 User Story 创建: 项目下拉框显示所有项目
→ 访问用户管理: 项目筛选显示所有项目
```

### 2. ADMIN 用户测试
```
登录 ADMIN 账号
→ 访问 Dashboard: 只看到自己管理的项目
→ 访问 User Story 创建: 项目下拉框只显示可访问项目
→ 访问用户管理: 项目筛选只显示可访问项目
```

### 3. USER 用户测试
```
登录 USER 账号
→ 访问 Dashboard: 只看到自己参与的项目
→ 访问 User Story 创建: 项目下拉框只显示可访问项目
```

## 注意事项

1. **性能优化**: 如果项目或用户列表很大，建议添加分页或搜索功能
2. **缓存**: 可以考虑在前端缓存项目列表，避免每次切换页面都重新加载
3. **错误处理**: 已添加基本的错误处理和 console.error 日志
4. **Loading 状态**: 项目列表加载时，下拉框可能为空，用户看到空列表是正常的
5. **权限检查**: 后端已经做了权限检查，前端不需要额外过滤

## 后续改进建议

1. **添加 Loading 提示**: 在加载项目列表时显示加载动画
2. **缓存优化**: 使用 Vuex/Pinia 缓存项目列表，避免重复请求
3. **实时更新**: 使用 WebSocket 或轮询实时更新项目列表
4. **搜索功能**: 在项目下拉框添加搜索过滤功能
5. **分组显示**: 按项目状态或所有权分组显示项目
