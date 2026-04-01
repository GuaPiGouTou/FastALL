pipeline {
    agent any

    tools {
        maven 'maven-3.8'
        nodejs 'node-24'
    }

    environment {
        BASE_DIR="/var/jenkins_home/fastall"
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
                    mkdir -p /var/jenkins_home/fastall/backend
                    cp target/ECMO-0.0.1-SNAPSHOT.jar /var/jenkins_home/fastall/backend/app.jar
                '''
            }
        }

        stage('Build Frontend') {
            steps {
                sh '''
                      cd FastAll/html
                      npm install
                      npm run build
                      mkdir -p ${DEPLOY_BASE}/frontend
                      rm -rf ${DEPLOY_BASE}/frontend/*
                      cp -r dist/* ${DEPLOY_BASE}/frontend/
                '''
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                    echo "构建产物已复制到 /var/jenkins_home/fastall"
                    cd /var/jenkins_home/fastall
                    # 确保脚本可执行
                    chmod +x start_all.sh stop_all.sh restart.sh
                    # 执行重启脚本（如需 sudo，请提前配置免密）
                    ./restart.sh
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