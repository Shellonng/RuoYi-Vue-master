# Java 编码规范（基于阿里巴巴Java开发手册）

## 一、命名规范

### 1.1 包名

- **全部小写**，使用点分隔符
- 格式：`com.assessment.模块名`

```java
✅ 正确
com.assessment.controller
com.assessment.service.impl
com.assessment.mapper

❌ 错误
com.assessment.Controller
com.assessment.Service_Impl
```

### 1.2 类名

- **大驼峰命名法**（PascalCase）
- 类名使用名词
- 接口名使用形容词或名词

```java
✅ 正确
public class GradeController { }
public class GradeServiceImpl { }
public interface IGradeService { }
public class AIGradingRequest { }

❌ 错误
public class gradeController { }
public class Grade_Service { }
public class aiGrading { }
```

**特殊后缀约定**：
- Controller：控制器类 `GradeController`
- Service：服务接口 `IGradeService`
- ServiceImpl：服务实现类 `GradeServiceImpl`
- Mapper：数据访问类 `GradeMapper`
- Entity/Model：实体类 `Grade`
- DTO：数据传输对象 `GradeCreateRequest`、`GradeResponse`
- VO：视图对象 `StudentGradeVO`
- Util：工具类 `FileUtil`
- Exception：异常类 `BusinessException`
- Config：配置类 `SwaggerConfig`

### 1.3 方法名

- **小驼峰命名法**（camelCase）
- 方法名使用动词或动词短语

```java
✅ 正确
public void createGrade() { }
public List<Grade> getStudentGrades() { }
public boolean isValid() { }
public void handleException() { }

❌ 错误
public void CreateGrade() { }
public List<Grade> student_grades() { }
public boolean Valid() { }
```

**常用动词约定**：
- `get`：获取数据
- `set`：设置数据
- `create/add`：创建/添加
- `update`：更新
- `delete/remove`：删除
- `is/has/can`：布尔判断
- `calculate`：计算
- `validate`：验证
- `convert`：转换
- `build`：构建

### 1.4 变量名

- **小驼峰命名法**
- 变量名使用名词

```java
✅ 正确
private Long studentId;
private String courseName;
private List<Grade> gradeList;
private BigDecimal averageScore;

❌ 错误
private Long StudentId;
private String course_name;
private List<Grade> list;
private BigDecimal avg;  // 过于简写
```

### 1.5 常量名

- **全部大写**，单词间用下划线分隔

```java
✅ 正确
public static final int MAX_SCORE = 100;
public static final String DEFAULT_STATUS = "draft";
public static final long CACHE_EXPIRE_TIME = 3600L;

❌ 错误
public static final int maxScore = 100;
public static final String defaultStatus = "draft";
```

## 二、注释规范

### 2.1 类注释

```java
/**
 * 成绩管理服务实现类
 *
 * 功能说明：
 * 1. 成绩的增删改查
 * 2. 成绩统计分析
 * 3. 成绩导入导出
 *
 * @author 张三
 * @since 2025-11-10
 */
@Service
public class GradeServiceImpl implements IGradeService {
    // ...
}
```

### 2.2 方法注释

```java
/**
 * 获取学生成绩列表（分页）
 *
 * @param studentId 学生ID
 * @param courseId 课程ID（可选）
 * @param type 成绩类型（可选）
 * @param page 页码，从1开始
 * @param pageSize 每页数量
 * @return 分页结果，包含成绩列表
 * @throws BusinessException 当参数无效时抛出
 */
public PageResult<GradeResponse> getStudentGrades(
        Long studentId, Long courseId, String type, Integer page, Integer pageSize) {
    // 实现逻辑
}
```

### 2.3 复杂逻辑注释

```java
// 计算知识点掌握度
// 公式：掌握度 = (正确次数 / 总次数) * 最近测试得分权重
// 权重：最近测试占60%，历史正确率占40%
BigDecimal masteryLevel = calculateMasteryLevel(correctCount, totalCount, lastScore);
```

### 2.4 TODO 注释

```java
// TODO: 后续需要优化为批量查询，避免N+1问题
for (Grade grade : grades) {
    Student student = studentMapper.selectById(grade.getStudentId());
}

// FIXME: 这里的计算逻辑有问题，需要修正
BigDecimal avg = total / count;
```

## 三、代码格式规范

### 3.1 缩进

- 使用 **4个空格** 缩进（不使用Tab）
- IDEA设置：Settings → Editor → Code Style → Java → Tabs and Indents

```java
✅ 正确
public class GradeController {
    private final IGradeService gradeService;

    public Result<Grade> getGrade(Long id) {
        if (id == null) {
            return Result.error("ID不能为空");
        }
        return Result.success(gradeService.getById(id));
    }
}
```

### 3.2 每行长度

- 单行代码不超过 **120个字符**
- 超长代码需要换行

```java
✅ 正确
public Result<PageResult<GradeResponse>> getStudentGrades(
        Long studentId,
        Long courseId,
        String type,
        Integer page,
        Integer pageSize) {
    // ...
}

❌ 错误
public Result<PageResult<GradeResponse>> getStudentGrades(Long studentId, Long courseId, String type, Integer page, Integer pageSize) {
    // 一行太长
}
```

### 3.3 空行规范

```java
public class GradeServiceImpl {

    private final GradeMapper gradeMapper;
    private final StudentMapper studentMapper;
    // 字段之间不需要空行

    // 字段与方法之间空一行
    @Override
    public Grade getById(Long id) {
        // 方法内部逻辑分组时，可以用空行分隔
        // 第一部分：参数校验
        if (id == null) {
            throw new BusinessException("ID不能为空");
        }

        // 第二部分：查询数据
        Grade grade = gradeMapper.selectById(id);
        if (grade == null) {
            throw new ResourceNotFoundException("成绩不存在");
        }

        // 第三部分：返回结果
        return grade;
    }
    // 方法之间空一行
    @Override
    public void deleteById(Long id) {
        // ...
    }
}
```

### 3.4 大括号规范

- 左大括号不换行，右大括号换行
- if/else/for/while 必须使用大括号

```java
✅ 正确
if (score > 90) {
    level = "优秀";
} else if (score > 80) {
    level = "良好";
} else {
    level = "一般";
}

❌ 错误
if (score > 90)
{
    level = "优秀";
}

if (score > 90) level = "优秀";  // 必须加大括号
```

## 四、代码编写规范

### 4.1 魔法值

- **禁止**在代码中直接使用魔法数字和魔法字符串
- 使用常量或枚举

```java
❌ 错误
if (grade.getStatus().equals("published")) {
    // ...
}
if (score >= 60) {
    // ...
}

✅ 正确
// 方式1：使用常量
public class GradeConstants {
    public static final String STATUS_PUBLISHED = "published";
    public static final String STATUS_DRAFT = "draft";
    public static final int PASS_SCORE = 60;
}

if (GradeConstants.STATUS_PUBLISHED.equals(grade.getStatus())) {
    // ...
}

// 方式2：使用枚举（推荐）
public enum GradeStatus {
    DRAFT("draft", "草稿"),
    PUBLISHED("published", "已发布");

    private String code;
    private String desc;

    // getter/setter/constructor
}
```

### 4.2 空指针检查

```java
❌ 错误
String name = student.getName();
int length = name.length();  // 可能NPE

✅ 正确
// 方式1：手动检查
if (student != null && student.getName() != null) {
    int length = student.getName().length();
}

// 方式2：Optional
Optional.ofNullable(student)
    .map(Student::getName)
    .map(String::length)
    .orElse(0);

// 方式3：工具类
String name = ObjectUtil.defaultIfNull(student.getName(), "");
```

### 4.3 集合判空

```java
❌ 错误
if (gradeList != null && gradeList.size() > 0) {
    // ...
}

✅ 正确
// 方式1：使用工具类
if (CollectionUtil.isNotEmpty(gradeList)) {
    // ...
}

// 方式2：Apache Commons
if (CollectionUtils.isNotEmpty(gradeList)) {
    // ...
}
```

### 4.4 字符串比较

```java
❌ 错误
if (status == "published") {  // 使用 ==
    // ...
}
if (status.equals("published")) {  // 可能NPE
    // ...
}

✅ 正确
if ("published".equals(status)) {  // 常量在前
    // ...
}
if (Objects.equals(status, "published")) {  // 使用Objects工具类
    // ...
}
```

### 4.5 异常处理

```java
❌ 错误
try {
    // 业务逻辑
} catch (Exception e) {
    e.printStackTrace();  // 禁止使用
}

✅ 正确
try {
    // 业务逻辑
} catch (Exception e) {
    log.error("处理失败: {}", e.getMessage(), e);
    throw new BusinessException("处理失败");
}
```

### 4.6 日志规范

```java
// 使用Slf4j的占位符
✅ 正确
log.info("学生{}的成绩为{}", studentId, score);
log.error("处理失败: {}", e.getMessage(), e);

❌ 错误
log.info("学生" + studentId + "的成绩为" + score);  // 字符串拼接
System.out.println("日志信息");  // 禁止使用System.out
```

### 4.7 Lambda 表达式

```java
✅ 推荐使用Lambda和Stream
List<Long> studentIds = grades.stream()
    .map(Grade::getStudentId)
    .distinct()
    .collect(Collectors.toList());

// 复杂逻辑建议换行
List<GradeResponse> responses = grades.stream()
    .filter(grade -> grade.getScore() >= 60)
    .sorted(Comparator.comparing(Grade::getScore).reversed())
    .map(this::convertToResponse)
    .collect(Collectors.toList());
```

## 五、数据库操作规范

### 5.1 使用MyBatis Plus

```java
✅ 正确：使用QueryWrapper
LambdaQueryWrapper<Grade> queryWrapper = new LambdaQueryWrapper<>();
queryWrapper.eq(Grade::getStudentId, studentId)
    .eq(courseId != null, Grade::getCourseId, courseId)
    .orderByDesc(Grade::getCreatedAt);
List<Grade> grades = gradeMapper.selectList(queryWrapper);

❌ 错误：字符串拼接SQL（有SQL注入风险）
String sql = "SELECT * FROM grade WHERE student_id = " + studentId;
```

### 5.2 事务管理

```java
✅ 正确：使用@Transactional
@Override
@Transactional(rollbackFor = Exception.class)
public void createGrade(GradeCreateRequest request) {
    // 插入成绩
    Grade grade = new Grade();
    // ... 设置属性
    gradeMapper.insert(grade);

    // 更新知识点掌握度
    updateKnowledgeMastery(grade);

    // 如果发生异常，会自动回滚
}
```

### 5.3 批量操作

```java
❌ 错误：循环单条插入
for (Grade grade : grades) {
    gradeMapper.insert(grade);  // N次数据库操作
}

✅ 正确：批量插入
gradeService.saveBatch(grades);  // MyBatis Plus提供
```

## 六、性能优化建议

### 6.1 避免N+1查询

```java
❌ 错误
List<Grade> grades = gradeMapper.selectList(queryWrapper);
for (Grade grade : grades) {
    Student student = studentMapper.selectById(grade.getStudentId());  // N+1问题
    grade.setStudentName(student.getName());
}

✅ 正确
List<Grade> grades = gradeMapper.selectList(queryWrapper);
Set<Long> studentIds = grades.stream()
    .map(Grade::getStudentId)
    .collect(Collectors.toSet());

// 批量查询
List<Student> students = studentMapper.selectBatchIds(studentIds);
Map<Long, Student> studentMap = students.stream()
    .collect(Collectors.toMap(Student::getId, s -> s));

// 填充数据
grades.forEach(grade -> {
    Student student = studentMap.get(grade.getStudentId());
    if (student != null) {
        grade.setStudentName(student.getName());
    }
});
```

### 6.2 使用缓存

```java
// 使用Spring Cache
@Cacheable(value = "grades", key = "#id")
public Grade getById(Long id) {
    return gradeMapper.selectById(id);
}

@CacheEvict(value = "grades", key = "#id")
public void deleteById(Long id) {
    gradeMapper.deleteById(id);
}
```

## 七、IDEA 代码模板

### 7.1 设置代码模板

Settings → Editor → File and Code Templates

**类模板**：
```java
#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME};#end

/**
 * ${NAME}
 *
 * @author ${USER}
 * @since ${DATE}
 */
public class ${NAME} {
}
```

### 7.2 Live Templates（代码片段）

Settings → Editor → Live Templates

**创建常用片段**：
- `psvm`：public static void main
- `sout`：System.out.println
- `fori`：for循环
- `ifn`：if null判断

**自定义片段示例**：

触发词：`log`
```java
private static final Logger log = LoggerFactory.getLogger($CLASS_NAME$.class);
```

触发词：`trans`
```java
@Transactional(rollbackFor = Exception.class)
```

## 八、代码检查工具

### 8.1 使用Checkstyle

在 `pom.xml` 中添加：
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-checkstyle-plugin</artifactId>
    <version>3.3.0</version>
    <configuration>
        <configLocation>checkstyle.xml</configLocation>
    </configuration>
</plugin>
```

### 8.2 使用SonarLint（IDEA插件）

- 实时检测代码质量问题
- 提供修复建议
- 支持自定义规则

## 九、代码质量检查清单

提交代码前，确保：

- [ ] 所有public方法都有JavaDoc注释
- [ ] 没有使用魔法值
- [ ] 没有System.out.println
- [ ] 异常处理完善
- [ ] 日志记录合理
- [ ] 没有TODO/FIXME（或已记录在任务列表）
- [ ] 代码格式化（Ctrl+Alt+L）
- [ ] 没有未使用的import
- [ ] 变量命名符合规范
- [ ] 单元测试通过

## 十、推荐阅读

- [阿里巴巴Java开发手册](https://github.com/alibaba/p3c)
- [Effective Java（第3版）](https://book.douban.com/subject/30412517/)
- [Clean Code（代码整洁之道）](https://book.douban.com/subject/4199741/)
