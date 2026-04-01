pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                // 请确保 'githubssh' 是你在 Jenkins 中创建的凭据 ID
                checkout([$class: 'GitSCM', branches: [[name: 'main']], userRemoteConfigs: [[url: 'git@github.com:GuaPiGouTou/FastALL.git', credentialsId: 'sshGitHub']]])
            }
        }

        stage('Deploy All') {
            steps {
                echo '>>> 正在启动部署流程...'
                // 直接通过 Shell 执行脚本，不再依赖 Jenkins 插件工具
                sh 'chmod +x deploy_all.sh'
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
