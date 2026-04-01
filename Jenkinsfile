pipeline {
    agent any
      tools {
            maven 'maven-3.8'
            nodejs 'node-24'
        }
    environment {
        BACKEND_DIR = '/var/jenkins_home/fastall/backend'
        FRONTEND_DIR = '/var/jenkins_home/fastall/frontend'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Backend') {
            steps {
                dir('FastAll') {
                    sh 'mvn clean package -DskipTests'
                    sh "mkdir -p ${BACKEND_DIR}"
                    sh "cp target/ECMO-0.0.1-SNAPSHOT.jar ${BACKEND_DIR}/app.jar"
                }
            }
        }

        stage('Build Frontend') {
            steps {
                dir('FastAll/html') {
                    sh 'npm install'
                    sh 'npm run build'
                    sh "rm -rf ${FRONTEND_DIR}/*"
                    sh "cp -r dist/* ${FRONTEND_DIR}/"
                }
            }
        }

        stage('Deploy') {
            steps {
                // 重启后端 systemd 服务
                sh 'ssh -o BatchMode=yes ubuntu@127.0.0.1 "sudo systemctl restart backend"'
                // 重载 Nginx
                sh 'ssh -o BatchMode=yes ubuntu@127.0.0.1 "sudo systemctl reload nginx"'

            }
        }
    }

    post {
        always {
            cleanWs()
        }
        failure {
            echo '流水线运行失败，请检查控制台错误日志。'
        }
    }
}