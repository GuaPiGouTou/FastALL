pipeline {
    agent any

    tools {
        maven 'maven-3.8'   // 与 Global Tool Configuration 中配置的名称一致
        nodejs 'node-24'
    }

    environment {
        DEPLOY_BASE = '/opt/fastall'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: 'main']],
                    userRemoteConfigs: [[
                        url: 'git@github.com:GuaPiGouTou/FastALL.git',
                        credentialsId: 'sshGitHub'
                    ]]
                ])
            }
        }

        stage('Build Backend') {
            steps {
                sh '''
                    # 当前已在仓库根目录（FastALL）
                    mvn clean package -DskipTests
                    # 假设生成的 jar 名称固定为 ECMO-0.0.1-SNAPSHOT.jar
                    cp target/ECMO-0.0.1-SNAPSHOT.jar ${DEPLOY_BASE}/backend/app.jar
                '''
            }
        }

        stage('Build Frontend') {
            steps {
                sh '''
                    cd html
                    npm install
                    npm run build   # 生成生产构建，输出目录通常是 dist/
                    rm -rf ${DEPLOY_BASE}/frontend/*
                    cp -r dist/* ${DEPLOY_BASE}/frontend/
                '''
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                    # 调用重启脚本（需预先在宿主机创建 /opt/fastall/restart.sh 并配置 sudo 免密）
                    sudo ${DEPLOY_BASE}/restart.sh
                '''
            }
        }
    }

    post {
        success {
            echo '>>> 全自动流水线成功完成！前后端已部署并启动。'
        }
        failure {
            echo '>>> 流水线运行失败，请检查控制台错误日志。'
        }
    }
}