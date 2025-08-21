#!/bin/sh

# 复制项目的文件到对应docker路径，便于一键生成镜像。
usage() {
	echo "Usage: sh copy.sh"
	exit 1
}


# copy sql
echo "begin copy sql "
cp ../sql/ry_20250523.sql ./mysql/db
cp ../sql/ry_config_20250224.sql ./mysql/db

# copy html
echo "begin copy html "
cp -r ../xypai-ui/dist/** ./nginx/html/dist


# copy jar
echo "begin copy xypai-gateway "
cp ../xypai-gateway/target/xypai-gateway.jar ./ruoyi/gateway/jar

echo "begin copy xypai-auth "
cp ../xypai-auth/target/xypai-auth.jar ./ruoyi/auth/jar

echo "begin copy xypai-visual "
cp ../xypai-visual/xypai-monitor/target/xypai-visual-monitor.jar  ./ruoyi/visual/monitor/jar

echo "begin copy xypai-modules-system "
cp ../xypai-modules/xypai-system/target/xypai-modules-system.jar ./ruoyi/modules/system/jar

echo "begin copy xypai-modules-file "
cp ../xypai-modules/xypai-file/target/xypai-modules-file.jar ./ruoyi/modules/file/jar

echo "begin copy xypai-modules-job "
cp ../xypai-modules/xypai-job/target/xypai-modules-job.jar ./ruoyi/modules/job/jar

echo "begin copy xypai-modules-gen "
cp ../xypai-modules/xypai-gen/target/xypai-modules-gen.jar ./ruoyi/modules/gen/jar

