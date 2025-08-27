# 🚀 XyPai 用户中心微服务启动指南

## 📋 启动前准备

### 1. 确保基础服务运行

- ✅ **MySQL** 数据库服务已启动 (端口: 3306)
- ✅ **Nacos** 配置中心已启动 (端口: 8848)
- ✅ **Redis** 缓存服务已启动 (端口: 6379)

### 2. 数据库准备

确保MySQL中已存在 `ry-cloud` 数据库，并包含以下表结构：

```sql
-- 用户表
CREATE TABLE `sys_user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
  `user_name` varchar(30) NOT NULL COMMENT '用户账号',
  `nick_name` varchar(30) NOT NULL COMMENT '用户昵称',
  `email` varchar(50) DEFAULT '' COMMENT '用户邮箱',
  `phonenumber` varchar(11) DEFAULT '' COMMENT '手机号码',
  `sex` char(1) DEFAULT '0' COMMENT '用户性别（0男 1女 2未知）',
  `avatar` varchar(100) DEFAULT '' COMMENT '头像地址',
  `password` varchar(100) DEFAULT '' COMMENT '密码',
  `status` char(1) DEFAULT '0' COMMENT '帐号状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  `login_ip` varchar(128) DEFAULT '' COMMENT '最后登录IP',
  `login_date` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB COMMENT='用户信息表';

-- 部门表（可选，如果需要部门功能）
CREATE TABLE `sys_dept` (
  `dept_id` bigint NOT NULL AUTO_INCREMENT COMMENT '部门id',
  `parent_id` bigint DEFAULT '0' COMMENT '父部门id',
  `ancestors` varchar(50) DEFAULT '' COMMENT '祖级列表',
  `dept_name` varchar(30) DEFAULT '' COMMENT '部门名称',
  `order_num` int DEFAULT '0' COMMENT '显示顺序',
  `leader` varchar(20) DEFAULT NULL COMMENT '负责人',
  `phone` varchar(11) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) DEFAULT NULL COMMENT '邮箱',
  `status` char(1) DEFAULT '0' COMMENT '部门状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`dept_id`)
) ENGINE=InnoDB COMMENT='部门表';
```

### 3. Nacos配置中心配置

在Nacos配置中心添加以下配置：

#### 配置1: 应用共享配置

- **Data ID**: `application-dev.yml`
- **Group**: `DEFAULT_GROUP`
- **配置内容**: 复制 `temp-nacos-configs/xypai-user-dev-simple.yml` 的内容

#### 配置2: 用户服务专用配置（可选）

- **Data ID**: `xypai-user-dev.yml`
- **Group**: `DEFAULT_GROUP`
- **配置内容**: 可添加用户服务特定的配置

## 🚀 启动方式

### 方式1: 使用批处理文件启动

```bash
# 在项目根目录执行
bin/run-modules-user.bat
```

### 方式2: Maven命令启动

```bash
# 进入用户模块目录
cd xypai-modules/xypai-user

# 编译并启动
mvn clean compile
mvn spring-boot:run
```

### 方式3: IDE启动

直接运行 `com.xypai.user.XyPaiUserApplication.main()` 方法

## 📊 启动验证

### 1. 检查服务注册

访问 Nacos 管理界面: http://127.0.0.1:8848/nacos

- 用户名: nacos
- 密码: nacos
- 查看服务列表中是否出现 `xypai-user` 服务

### 2. 检查健康状态

访问健康检查端点: http://127.0.0.1:9203/actuator/health

期望返回:

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}
```

### 3. 检查API文档

访问Swagger文档: http://127.0.0.1:9203/swagger-ui.html

## 🔧 配置说明

### 端口配置

- **服务端口**: 9203
- **数据库端口**: 3306
- **Nacos端口**: 8848
- **Redis端口**: 6379

### 数据库连接配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/ry-cloud
    username: root
    password: root123  # 请根据实际情况修改
```

## ❗ 常见问题解决

### 1. 数据源连接失败

- 检查MySQL服务是否启动
- 验证数据库连接信息是否正确
- 确认 `ry-cloud` 数据库是否存在

### 2. Nacos连接失败

- 检查Nacos服务是否启动
- 验证Nacos地址和端口是否正确
- 确认用户名密码是否正确

### 3. 端口冲突

- 检查9203端口是否被占用
- 可在bootstrap.yml中修改端口号

### 4. 依赖冲突

如遇到依赖冲突，可尝试：

```bash
mvn clean install -U
```

## 🎯 成功启动标志

看到以下日志说明启动成功:

```
(♥◠‿◠)ﾉﾞ  用户中心启动成功   ლ(´ڡ`ლ)ﾞ
```

## 📞 技术支持

如遇到问题，请检查:

1. 日志文件: `logs/xypai-user/`
2. 配置文件: Nacos配置中心
3. 数据库连接: MySQL连接状态
