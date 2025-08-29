#!/bin/bash

# ========================================
# 🏗️ XyPai-Users Maven构建脚本 (Linux/Mac)
# ========================================

set -e  # 遇到错误立即退出

echo ""
echo "🚀 开始构建 XyPai-Users 微服务集群..."
echo ""

# 设置Maven优化参数
export MAVEN_OPTS="-Xmx2g -XX:MaxMetaspaceSize=512m -XX:+UseG1GC"

# 检查Java版本
echo "🔍 检查Java版本..."
if ! java -version 2>&1 | grep -q "21"; then
    echo "⚠️  建议使用Java 21版本"
fi

# 检查Maven版本
echo "🔍 检查Maven版本..."
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven未安装！"
    exit 1
fi

echo ""
echo "📋 选择构建模式:"
echo "1. 快速构建 (跳过测试)"
echo "2. 完整构建 (包含测试)"  
echo "3. 清理构建"
echo "4. 单模块构建"
echo "5. 验证构建"
echo ""
read -p "请选择 [1-5]: " choice

case $choice in
    1)
        echo ""
        echo "⚡ 执行快速构建..."
        mvn clean install -DskipTests -T 1C --batch-mode
        ;;
    2)
        echo ""
        echo "🧪 执行完整构建..."
        mvn clean install -T 1C --batch-mode
        ;;
    3)
        echo ""
        echo "🧹 执行清理构建..."
        mvn clean
        echo "清理完成！"
        exit 0
        ;;
    4)
        echo ""
        echo "📦 可选模块:"
        echo "1. common"
        echo "2. user-service"
        echo "3. social-service"
        echo "4. wallet-service"
        echo "5. feed-service"
        echo "6. gateway-service"
        echo ""
        read -p "请选择模块 [1-6]: " module_choice
        
        case $module_choice in
            1) MODULE="common" ;;
            2) MODULE="user-service" ;;
            3) MODULE="social-service" ;;
            4) MODULE="wallet-service" ;;
            5) MODULE="feed-service" ;;
            6) MODULE="gateway-service" ;;
            *) echo "❌ 无效选择！"; exit 1 ;;
        esac
        
        echo ""
        echo "🔧 构建模块: $MODULE"
        mvn clean install -pl $MODULE -am -T 1C --batch-mode
        ;;
    5)
        echo ""
        echo "🔍 执行验证构建..."
        echo "1. 验证POM文件..."
        mvn validate
        echo "2. 检查依赖冲突..."
        mvn dependency:tree | grep -i conflict || echo "无依赖冲突"
        echo "3. 编译检查..."
        mvn clean compile -T 1C
        echo "验证完成！"
        exit 0
        ;;
    *)
        echo "❌ 无效选择！"
        exit 1
        ;;
esac

# 检查构建结果
if [ $? -eq 0 ]; then
    echo ""
    echo "✅ 构建成功！"
    echo ""
    echo "📁 构建产物位置:"
    echo "   common/target/xypai-common-1.0.0-SNAPSHOT.jar"
    echo "   user-service/target/user-service-1.0.0-SNAPSHOT.jar"
    echo "   social-service/target/social-service-1.0.0-SNAPSHOT.jar"
    echo "   wallet-service/target/wallet-service-1.0.0-SNAPSHOT.jar"
    echo "   feed-service/target/feed-service-1.0.0-SNAPSHOT.jar"
    echo "   gateway-service/target/gateway-service-1.0.0-SNAPSHOT.jar"
    echo ""
    echo "🚀 可以开始运行服务了！"
    echo ""
    echo "💡 快速启动命令:"
    echo "   java -jar user-service/target/user-service-1.0.0-SNAPSHOT.jar"
    echo "   java -jar gateway-service/target/gateway-service-1.0.0-SNAPSHOT.jar"
else
    echo ""
    echo "❌ 构建失败！请检查错误信息。"
    echo ""
    echo "💡 常见解决方案:"
    echo "   1. 检查Java版本: java -version"
    echo "   2. 检查网络连接"
    echo "   3. 清理Maven本地仓库: rm -rf ~/.m2/repository"
    echo "   4. 增加内存: export MAVEN_OPTS='-Xmx4g'"
    echo ""
    exit 1
fi
