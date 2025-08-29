# 🏗️ Maven构建和组织指南

## 🎯 **构建前组织优化完成**

### ✅ **已完成的POM优化**

#### 1️⃣ **版本管理优化**

```xml
<!-- 更新到最新稳定版本 -->
<properties>
    <spring-boot.version>3.2.1</spring-boot.version>
    <spring-cloud.version>2023.0.0</spring-cloud.version>
    <mysql.version>8.2.0</mysql.version>              <!-- ✅ 更新 -->
    <maven-compiler-plugin.version>3.11.0</maven-compiler-plugin.version>  <!-- ✅ 新增 -->
    <maven-surefire-plugin.version>3.1.2</maven-surefire-plugin.version>   <!-- ✅ 新增 -->
</properties>
```

#### 2️⃣ **依赖优化**

```xml
<!-- 使用新的MySQL驱动 -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>  <!-- ✅ 替换过时的mysql-connector-java -->
    <version>${mysql.version}</version>
    <scope>runtime</scope>                       <!-- ✅ 添加合适的scope -->
</dependency>
```

#### 3️⃣ **插件配置增强**

```xml
<!-- 编译器优化 -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <parameters>true</parameters>             <!-- ✅ 保留参数名 -->
        <annotationProcessorPaths>...</annotationProcessorPaths>
    </configuration>
</plugin>

<!-- 测试插件 -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>  <!-- ✅ 单元测试 -->
    <version>${maven-surefire-plugin.version}</version>
</plugin>

<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-failsafe-plugin</artifactId>  <!-- ✅ 集成测试 -->
    <version>${maven-failsafe-plugin.version}</version>
</plugin>
```

---

## 🚀 **Maven构建命令**

### 🔸 **基础构建**

```bash
# 清理和编译
mvn clean compile

# 完整构建（包含测试）
mvn clean install

# 快速构建（跳过测试）
mvn clean install -DskipTests

# 并行构建（推荐）
mvn clean install -T 1C
```

### 🔸 **高级构建选项**

```bash
# 离线构建（已有依赖时）
mvn clean install -o

# 详细日志输出
mvn clean install -X

# 静默模式
mvn clean install -q

# 指定配置文件
mvn clean install -P production

# 并行+跳过测试（最快）
mvn clean install -T 1C -DskipTests
```

### 🔸 **测试专用命令**

```bash
# 只运行单元测试
mvn test

# 只运行集成测试
mvn failsafe:integration-test

# 测试特定类
mvn test -Dtest=UserServiceTest

# 测试覆盖率
mvn test jacoco:report
```

### 🔸 **单模块构建**

```bash
# 只构建用户服务
mvn clean install -pl user-service

# 构建用户服务及其依赖
mvn clean install -pl user-service -am

# 构建多个指定模块
mvn clean install -pl user-service,gateway-service
```

---

## 📋 **构建顺序和依赖**

### 🔸 **推荐构建顺序**

```bash
1. common           # 📦 公共组件（其他模块依赖）
2. user-service     # 👤 用户服务（基础服务）
3. social-service   # 🤝 社交服务
4. wallet-service   # 💰 钱包服务  
5. feed-service     # 📱 动态服务
6. gateway-service  # 🚪 网关服务（最后启动）
```

### 🔸 **依赖关系图**

```
gateway-service
    ↓
┌─────────────────────────────────┐
│  user-service  social-service   │
│  wallet-service  feed-service   │
└─────────────────────────────────┘
    ↓
common (所有服务都依赖)
```

---

## ✅ **构建前检查清单**

### 🔍 **环境检查**

```bash
# Java版本检查
java -version
# 期望: openjdk version "21.x.x"

# Maven版本检查
mvn -version  
# 期望: Apache Maven 3.8+

# 内存检查
echo $MAVEN_OPTS
# 推荐: -Xmx2g -XX:MaxMetaspaceSize=512m
```

### 🔍 **配置检查**

```bash
# 检查POM文件语法
mvn validate

# 检查依赖冲突
mvn dependency:tree

# 检查过时依赖
mvn versions:display-dependency-updates

# 检查插件更新
mvn versions:display-plugin-updates
```

---

## 🛠️ **构建优化配置**

### 🔸 **Maven配置优化**

创建 `~/.m2/settings.xml`:

```xml
<settings>
    <localRepository>~/.m2/repository</localRepository>
    
    <!-- 镜像配置（国内用户） -->
    <mirrors>
        <mirror>
            <id>aliyun</id>
            <mirrorOf>central</mirrorOf>
            <url>https://maven.aliyun.com/repository/central</url>
        </mirror>
    </mirrors>
    
    <!-- 性能配置 -->
    <profiles>
        <profile>
            <id>performance</id>
            <properties>
                <maven.compiler.source>21</maven.compiler.source>
                <maven.compiler.target>21</maven.compiler.target>
                <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
            </properties>
        </profile>
    </profiles>
    
    <activeProfiles>
        <activeProfile>performance</activeProfile>
    </activeProfiles>
</settings>
```

### 🔸 **JVM优化参数**

设置环境变量:

```bash
# Linux/Mac
export MAVEN_OPTS="-Xmx2g -XX:MaxMetaspaceSize=512m -XX:+UseG1GC"

# Windows
set MAVEN_OPTS=-Xmx2g -XX:MaxMetaspaceSize=512m -XX:+UseG1GC
```

---

## 🧪 **构建验证**

### 🔸 **完整验证流程**

```bash
# 1. 验证POM文件
mvn validate

# 2. 清理旧构建
mvn clean

# 3. 编译检查
mvn compile

# 4. 运行测试
mvn test

# 5. 打包构建
mvn package

# 6. 安装到本地仓库
mvn install

# 7. 验证可执行JAR
java -jar user-service/target/user-service-1.0.0-SNAPSHOT.jar --version
```

### 🔸 **构建成功指标**

- [ ] 所有模块编译通过
- [ ] 单元测试全部通过
- [ ] JAR包正确生成
- [ ] 依赖解析无冲突
- [ ] 无编译警告

---

## 🚨 **常见构建问题**

### ❌ **问题1: 编译失败**

```bash
错误: Java版本不兼容
解决: 确保使用Java 21
export JAVA_HOME=/path/to/java21
```

### ❌ **问题2: 依赖下载失败**

```bash
错误: Could not resolve dependencies
解决: 清理本地仓库
rm -rf ~/.m2/repository
mvn clean install
```

### ❌ **问题3: 内存不足**

```bash
错误: OutOfMemoryError
解决: 增加内存配置
export MAVEN_OPTS="-Xmx4g -XX:MaxMetaspaceSize=1g"
```

### ❌ **问题4: 测试失败**

```bash
错误: Tests run: 10, Failures: 2
解决: 检查测试环境或跳过测试
mvn clean install -DskipTests
```

---

## 🎯 **推荐构建流程**

### 🔸 **开发环境构建**

```bash
# 快速开发构建
mvn clean compile -T 1C

# 完整验证构建  
mvn clean install -T 1C
```

### 🔸 **生产环境构建**

```bash
# 生产级构建
mvn clean install -P production -T 1C

# 构建Docker镜像
mvn clean package dockerfile:build -T 1C
```

### 🔸 **CI/CD构建**

```bash
# CI流水线构建
mvn clean verify -B -T 1C \
  -Dmaven.test.failure.ignore=false \
  -Dstyle.color=never
```

---

## 🎉 **构建完成验证**

构建完成后，检查以下文件：

```
xypai-users/
├── common/target/xypai-common-1.0.0-SNAPSHOT.jar
├── user-service/target/user-service-1.0.0-SNAPSHOT.jar
├── social-service/target/social-service-1.0.0-SNAPSHOT.jar
├── wallet-service/target/wallet-service-1.0.0-SNAPSHOT.jar
├── feed-service/target/feed-service-1.0.0-SNAPSHOT.jar
└── gateway-service/target/gateway-service-1.0.0-SNAPSHOT.jar
```

**🚀 现在可以开始运行和测试微服务了！**