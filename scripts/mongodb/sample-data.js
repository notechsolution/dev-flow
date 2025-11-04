// ============================================
// DevFlow 示例数据脚本
// ============================================
// 此脚本用于插入测试数据（可选）
// 执行方式: mongosh < scripts/mongodb/sample-data.js

print('====================================');
print('Inserting Sample Data for DevFlow');
print('====================================');

// 连接到 devflow 数据库
db = db.getSiblingDB('devflow');

// 清空现有数据（谨慎使用！）
// db.users.deleteMany({});
// db.projects.deleteMany({});
// db.user_stories.deleteMany({});

print('Creating sample admin user...');

// 示例管理员用户
// 注意：密码需要在应用层使用 BCrypt 加密
// 这里仅作为参考，实际应用应通过应用接口创建用户
var adminUser = {
    _id: ObjectId(),
    username: 'admin',
    email: 'admin@devflow.com',
    // password: 'admin123' (需要 BCrypt 加密)
    role: 'ADMIN',
    enabled: true,
    createdAt: new Date(),
    updatedAt: new Date()
};

print('⚠ Note: Admin user should be created through application interface');
print('  Username: admin');
print('  Password: admin123 (default, please change after first login)');

// 示例项目
print('Creating sample projects...');

var project1 = {
    _id: ObjectId(),
    name: 'DevFlow 项目管理系统',
    description: '一个基于 AI 的需求管理和开发流程工具',
    owner: 'admin',
    createdAt: new Date(),
    updatedAt: new Date(),
    status: 'ACTIVE'
};

var project2 = {
    _id: ObjectId(),
    name: '电商平台',
    description: '在线购物电商平台项目',
    owner: 'admin',
    createdAt: new Date(),
    updatedAt: new Date(),
    status: 'ACTIVE'
};

try {
    db.projects.insertMany([project1, project2]);
    print('✓ Sample projects inserted');
} catch (e) {
    print('⚠ Failed to insert projects: ' + e);
}

// 示例用户故事
print('Creating sample user stories...');

var stories = [
    {
        _id: ObjectId(),
        projectId: project1._id.toString(),
        title: '用户登录功能',
        description: '作为一个用户，我希望能够使用用户名和密码登录系统，以便访问我的项目和数据。',
        acceptanceCriteria: [
            '用户可以输入用户名和密码',
            '系统验证用户凭证',
            '登录成功后跳转到主页',
            '登录失败显示错误提示'
        ],
        priority: 'HIGH',
        status: 'TODO',
        estimatedHours: 8,
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        _id: ObjectId(),
        projectId: project1._id.toString(),
        title: 'AI 需求优化功能',
        description: '作为产品经理，我希望 AI 能够帮我优化需求描述，以便生成更清晰、更专业的用户故事。',
        acceptanceCriteria: [
            '用户输入原始需求',
            'AI 分析并优化需求',
            '生成结构化的用户故事',
            '包含验收标准和测试用例'
        ],
        priority: 'HIGH',
        status: 'IN_PROGRESS',
        estimatedHours: 16,
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        _id: ObjectId(),
        projectId: project2._id.toString(),
        title: '商品搜索功能',
        description: '作为买家，我希望能够搜索商品，以便快速找到我想购买的产品。',
        acceptanceCriteria: [
            '用户可以输入搜索关键词',
            '系统显示相关商品列表',
            '支持按价格、销量等排序',
            '支持筛选分类、品牌等'
        ],
        priority: 'MEDIUM',
        status: 'TODO',
        estimatedHours: 12,
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        _id: ObjectId(),
        projectId: project2._id.toString(),
        title: '购物车功能',
        description: '作为买家，我希望能够将商品加入购物车，以便一次性购买多个商品。',
        acceptanceCriteria: [
            '用户可以将商品添加到购物车',
            '可以查看购物车商品列表',
            '可以修改商品数量',
            '可以删除购物车中的商品',
            '显示商品总价'
        ],
        priority: 'HIGH',
        status: 'TODO',
        estimatedHours: 10,
        createdAt: new Date(),
        updatedAt: new Date()
    }
];

try {
    db.user_stories.insertMany(stories);
    print('✓ Sample user stories inserted (' + stories.length + ' stories)');
} catch (e) {
    print('⚠ Failed to insert user stories: ' + e);
}

print('====================================');
print('Sample data insertion completed!');
print('====================================');
print('Projects: 2');
print('User Stories: ' + stories.length);
print('====================================');
print('');
print('Next Steps:');
print('1. Create admin user through application:');
print('   POST /api/auth/register');
print('   { "username": "admin", "email": "admin@devflow.com", "password": "admin123" }');
print('');
print('2. Access application:');
print('   http://localhost:8099');
print('');
print('3. Login with admin account');
print('====================================');
