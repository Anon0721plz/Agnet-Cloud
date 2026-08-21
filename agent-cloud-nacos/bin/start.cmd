@echo off
rem Agent Cloud Nacos（merged 一体化）启动脚本，JVM 参数对齐官方 nacos startup.cmd（JDK9+ --add-opens）
setlocal
if "%JAVA_HOME%" == "" (
    echo ERROR: JAVA_HOME not set
    exit /b 1
)
set "BASE_DIR=%~dp0.."
cd /d "%BASE_DIR%"
"%JAVA_HOME%\bin\java" ^
    --add-opens=java.base/java.lang=ALL-UNNAMED ^
    --add-opens=java.base/java.lang.reflect=ALL-UNNAMED ^
    --add-opens=java.base/java.util=ALL-UNNAMED ^
    -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 ^
    -jar target\agent-cloud-nacos.jar %*
