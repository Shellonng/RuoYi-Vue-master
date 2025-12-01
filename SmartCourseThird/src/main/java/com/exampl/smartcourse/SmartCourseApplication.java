package com.exampl.smartcourse;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 智慧课程平台 - 评估专家系统启动类
 *
 * @author 开发团队
 * @since 2025-11-17
 */
@SpringBootApplication
@MapperScan({
    "com.exampl.smartcourse.mapper",
    "com.exampl.smartcourse.analysis.mapper",
    "com.exampl.smartcourse.mastery.mapper"
})
public class SmartCourseApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartCourseApplication.class, args);
		System.out.println("========================================");
		System.out.println("智慧课程平台 - 评估专家系统启动成功！");
		System.out.println("Swagger API文档: http://localhost:8083/swagger-ui.html");
		System.out.println("========================================");
	}
	

}