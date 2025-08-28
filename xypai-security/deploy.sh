#!/bin/bash

# 🛡️ XY相遇派安全认证模块部署脚本
# XV04:02 一键部署脚本

set -e

echo "🛡️ XY相遇派安全认证模块部署开始..."
echo "================================"

# 函数：打印步骤
print_step() {
    echo ""
    echo "📋 步骤: $1"
    echo "--------------------------------"
}

# 函数：检查命令是否存在
check_command() {
    if ! command -v $1 &> /dev/null; then
        echo "❌ 错误: $1 命令未找到，请先安装 $1"
        exit 1
    fi
}

# 检查必要的命令
print_step "检查环境依赖"
check_command "docker"
check_command "docker-compose"
check_command "mvn"

echo "✅ 环境检查通过"

# 构建项目
print_step "构建Maven项目"
echo "🔨 正在编译安全认证模块..."
mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo "✅ Maven构建成功"
else
    echo "❌ Maven构建失败"
    exit 1
fi

# 停止现有容器
print_step "停止现有容器"
echo "🛑 停止并移除现有容器..."
docker-compose down

# 构建和启动服务
print_step "构建和启动Docker服务"
echo "🐳 构建Docker镜像并启动服务..."
docker-compose up --build -d

# 检查服务状态
print_step "检查服务状态"
echo "⏳ 等待服务启动..."
sleep 30

# 健康检查
print_step "服务健康检查"
echo "🏥 检查认证中心健康状态..."
if curl -f http://localhost:9401/auth/health &> /dev/null; then
    echo "✅ 认证中心(security-oauth)启动成功 - http://localhost:9401"
else
    echo "⚠️  认证中心健康检查失败，请检查日志"
fi

echo "🏥 检查Web管理端健康状态..."
if curl -f http://localhost:9402/admin/health &> /dev/null; then
    echo "✅ Web管理端(security-web)启动成功 - http://localhost:9402"
else
    echo "⚠️  Web管理端健康检查失败，请检查日志"
fi

# 显示服务信息
print_step "部署完成"
echo "🎉 XY相遇派安全认证模块部署成功！"
echo ""
echo "📋 服务信息:"
echo "  🛡️  认证中心:    http://localhost:9401"
echo "  🌐 Web管理端:   http://localhost:9402"
echo "  🗄️  MySQL:      localhost:3306"
echo "  🗄️  Redis:       localhost:6379"
echo "  🌐 Nacos:       http://localhost:8848"
echo ""
echo "📖 快速测试:"
echo "  curl http://localhost:9401/auth/health"
echo "  curl http://localhost:9402/admin/config"
echo ""
echo "📝 查看日志:"
echo "  docker-compose logs -f security-oauth"
echo "  docker-compose logs -f security-web"
echo ""
echo "🛑 停止服务:"
echo "  docker-compose down"
echo ""
echo "================================"
echo "🎯 部署完成！"
