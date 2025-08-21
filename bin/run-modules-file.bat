@echo off
echo.
echo [信息] 使用Jar命令运行Modules-File工程。
echo.

cd %~dp0
cd ../xypai-modules/xypai-file/target

set JAVA_OPTS=-Xms512m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m

java -Dfile.encoding=utf-8 %JAVA_OPTS% -jar xypai-modules-file.jar

cd bin
pause