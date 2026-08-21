#!/bin/sh
# Agent Cloud Nacos（merged 一体化）启动脚本，JVM 参数对齐官方 nacos startup.sh（JDK9+ --add-opens）
set -e
if [ -z "$JAVA_HOME" ]; then
    echo "ERROR: JAVA_HOME not set"
    exit 1
fi
BASE_DIR=$(cd "$(dirname "$0")/.." && pwd)
cd "$BASE_DIR"
exec "$JAVA_HOME/bin/java" \
    --add-opens=java.base/java.lang=ALL-UNNAMED \
    --add-opens=java.base/java.lang.reflect=ALL-UNNAMED \
    --add-opens=java.base/java.util=ALL-UNNAMED \
    -Dfile.encoding=UTF-8 \
    -jar target/agent-cloud-nacos.jar "$@"
