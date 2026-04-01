pipeline {
    agent any

    tools {
        maven 'maven-3.8'
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
                    cd FastAll
                    mvn clean package -DskipTests
                    cp target/ECMO-0.0.1-SNAPSHOT.jar ${DEPLOY_BASE}/backend/app.jar
                '''
            }
        }

        stage('Build Frontend') {
            steps {
                sh '''
                    cd FastAll/html
                    npm install
                    npm run build
                    rm -rf ${DEPLOY_BASE}/frontend/*
                    cp -r dist/* ${DEPLOY_BASE}/frontend/
                '''
            }
        }

        stage('Deploy') {
            steps {
                sh '''
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
