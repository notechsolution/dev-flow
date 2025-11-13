// ============================================
// DevFlow MongoDB 初始化脚本
// ============================================
// 此脚本在 Docker 容器首次启动时自动执行
// 用于创建应用数据库、用户和必要的集合

print('====================================');
print('Initializing DevFlow Database');
print('====================================');

// 切换到 devflow 数据库
db = db.getSiblingDB('devflow');

// 创建应用用户
try {
    db.createUser({
        user: 'devflow',
        pwd: 'devflow123',  // 生产环境请使用环境变量
        roles: [
            {
                role: 'readWrite',
                db: 'devflow'
            },
            {
                role: 'dbAdmin',
                db: 'devflow'
            }
        ]
    });
    print('✓ User "devflow" created successfully');
} catch (e) {
    print('⚠ User creation failed (may already exist): ' + e);
}

// 创建集合
print('Creating collections...');

db.createCollection('users');
print('✓ Collection "users" created');

db.createCollection('projects');
print('✓ Collection "projects" created');

db.createCollection('user_stories');
print('✓ Collection "user_stories" created');

db.createCollection('images');
print('✓ Collection "images" created');

db.createCollection('prompt_templates');
print('✓ Collection "prompt_templates" created');

// 创建索引以提高性能
print('Creating indexes...');

// users 集合索引
db.users.createIndex({ 'email': 1 }, { unique: true, name: 'idx_users_email' });
print('✓ Index created: users.email (unique)');

db.users.createIndex({ 'username': 1 }, { unique: true, name: 'idx_users_username' });
print('✓ Index created: users.username (unique)');

db.users.createIndex({ 'createdAt': -1 }, { name: 'idx_users_created_at' });
print('✓ Index created: users.createdAt');

// projects 集合索引
db.projects.createIndex({ 'owner': 1 }, { name: 'idx_projects_owner' });
print('✓ Index created: projects.owner');

db.projects.createIndex({ 'createdAt': -1 }, { name: 'idx_projects_created_at' });
print('✓ Index created: projects.createdAt');

db.projects.createIndex({ 'name': 1, 'owner': 1 }, { name: 'idx_projects_name_owner' });
print('✓ Index created: projects.name + owner');

// user_stories 集合索引
db.user_stories.createIndex({ 'projectId': 1 }, { name: 'idx_user_stories_project_id' });
print('✓ Index created: user_stories.projectId');

db.user_stories.createIndex({ 'createdAt': -1 }, { name: 'idx_user_stories_created_at' });
print('✓ Index created: user_stories.createdAt');

db.user_stories.createIndex({ 'status': 1 }, { name: 'idx_user_stories_status' });
print('✓ Index created: user_stories.status');

db.user_stories.createIndex({ 'projectId': 1, 'status': 1 }, { name: 'idx_user_stories_project_status' });
print('✓ Index created: user_stories.projectId + status');

// images 集合索引
db.images.createIndex({ 'uploadedBy': 1 }, { name: 'idx_images_uploaded_by' });
print('✓ Index created: images.uploadedBy');

db.images.createIndex({ 'uploadedAt': -1 }, { name: 'idx_images_uploaded_at' });
print('✓ Index created: images.uploadedAt');

// prompt_templates 集合索引
db.prompt_templates.createIndex({ 'type': 1, 'level': 1 }, { name: 'idx_prompt_templates_type_level' });
print('✓ Index created: prompt_templates.type + level');

db.prompt_templates.createIndex({ 'type': 1, 'level': 1, 'projectId': 1 }, { name: 'idx_prompt_templates_type_level_project' });
print('✓ Index created: prompt_templates.type + level + projectId');

db.prompt_templates.createIndex({ 'type': 1, 'level': 1, 'userId': 1 }, { name: 'idx_prompt_templates_type_level_user' });
print('✓ Index created: prompt_templates.type + level + userId');

print('====================================');
print('Database initialization completed!');
print('====================================');
print('Database: devflow');
print('User: devflow');
print('Collections created: users, projects, user_stories, images, prompt_templates');
print('Indexes created: 16 total');
print('====================================');
