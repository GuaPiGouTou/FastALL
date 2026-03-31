#!/bin/bash

# ==========================================
# FastALL 全自动部署脚本 (前后端)
# ==========================================

# 1. 前端部署 (npm run dev 后台运行)
echo ">>> [Frontend] 正在启动前端部署..."
cd FastAll/html

# 杀掉旧的 Vite 进程 (通过搜索 vite 关键字)
VITE_PID=$(ps -ef | grep vite | grep -v grep | awk '{print $2}')
if [ -n "$VITE_PID" ]; then
    echo "正在关闭旧的前端进程: $VITE_PID"
    kill -9 $VITE_PID
fi

echo "正在安装前端依赖..."
npm install
echo "正在后台启动 Vite 开发服务器..."
# 注意：使用 nohup 确保进程在 Jenkins 任务结束后依然存在
nohup npm run dev > frontend_dev.log 2>&1 &
echo "前端服务已在后台启动 (日志: FastAll/html/frontend_dev.log)"

# 2. 后端部署 (Maven 编译 + Jar 启动)
echo ">>> [Backend] 正在启动后端部署..."
cd ../ # 回到 FastAll 目录

echo "正在进行 Maven 编译打包..."
mvn clean package -DskipTests

# 这里的 Jar 包名称取决于 pom.xml
JAR_NAME="ECMO-0.0.1-SNAPSHOT.jar"
APP_PID=$(ps -ef | grep $JAR_NAME | grep -v grep | awk '{print $2}')

if [ -n "$APP_PID" ]; then
    echo "正在关闭旧的后端进程: $APP_PID"
    kill -9 $APP_PID
fi

echo "正在后台启动后端程序..."
nohup java -jar target/$JAR_NAME --spring.profiles.active=prod > backend_run.log 2>&1 &
echo "后端程序已在后台启动 (日志: FastAll/backend_run.log)"

echo ">>> [Success] 所有部署已完成！"
