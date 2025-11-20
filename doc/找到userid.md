📝 使用说明
后续业务开发只需调用一行代码：
// 获取当前用户的业务ID（user.id）
Long userId = BusinessUserUtils.getCurrentBusinessUserId();
🔧 其他可用方法
// 获取完整用户信息
User user = BusinessUserUtils.getCurrentBusinessUser();

// 获取用户角色
String role = BusinessUserUtils.getCurrentUserRole();

// 判断角色
boolean isStudent = BusinessUserUtils.isStudent();
boolean isTeacher = BusinessUserUtils.isTeacher();
boolean isAdmin = BusinessUserUtils.isAdmin();