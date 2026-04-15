# 第一阶段：Maven 编译
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# 先复制 pom.xml 下载依赖（利用 Docker 缓存层）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码并编译打包
COPY src ./src
RUN mvn clean package -DskipTests -B

# 第二阶段：运行环境
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 从 builder 阶段复制生成的 jar 包
COPY --from=builder /app/target/*.jar app.jar

# 暴露 8080 端口
EXPOSE 8080

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
