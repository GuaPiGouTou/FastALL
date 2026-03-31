pipeline {
    agent any

    tools {
        // 名称需与 Jenkins 全局工具配置中一致
        maven 'Maven_3.8'
        nodejs 'NodeJS_16'
    }

    stages {
        stage('Checkout') {
            steps {
                // 如果仓库地址或凭据有变化，请修改此处
                checkout([$class: 'GitSCM', branches: [[name: 'main']], userRemoteConfigs: [[url: 'https://github.com/GuaPiGouTou/FastALL.git', credentialsId: 'GitHub']]])
            }
        }

        stage('Build & Deploy') {
            steps {
                echo '>>> 正在启动部署流程...'
                // 1. 给部署脚本添加可执行权限
                sh 'chmod +x deploy_all.sh'
                // 2. 执行脚本 (脚本内含后台运行逻辑，不会挂起流水线)
                sh './deploy_all.sh'
            }
        }
    }

    post {
        success {
            echo '>>> 全自动流水线成功完成！前后端已在后台启动。'
        }
        failure {
            echo '>>> 流水线运行失败，请检查控制台错误日志。'
        }
    }
}
