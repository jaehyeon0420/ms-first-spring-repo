pipeline {
    agent any
      
    environment {
        APP_NAME = "msai-firstpjt-spring-app"
        ACR_NAME = "backprojectAcr"
        ACR_LOGIN_SERVER = "backprojectAcr.azurecr.io"
    }

    stages {
        stage('Cleanup Workspace') {
            steps {
                cleanWs()
            }
        }

        stage('Checkout Code') {
            steps {
                git branch: 'master', url: 'https://github.com/jaehyeon0420/ms-first-spring-repo.git'
            }
        }

        stage('Build') {
            steps {
                // Windows라면 bat, Linux라면 sh 사용
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    IMAGE_TAG = "${env.ACR_LOGIN_SERVER}/${env.APP_NAME}:${env.BUILD_NUMBER}"
                    sh "docker build -t ${IMAGE_TAG} ."
                }
            }
        }

        stage('Login to Azure & Push to ACR') {
            steps {
                withCredentials([
                    string(credentialsId: 'AZURE_APP_ID', variable: 'AZURE_APP_ID'),
                    string(credentialsId: 'AZURE_PASSWORD', variable: 'AZURE_PASSWORD'),
                    string(credentialsId: 'AZURE_TENANT_ID', variable: 'AZURE_TENANT_ID')
                ]) {
                    sh """
                    echo Azure login...
                    az login --service-principal ^
                      -u %AZURE_APP_ID% ^
                      -p %AZURE_PASSWORD% ^
                      --tenant %AZURE_TENANT_ID%

                    echo Logging into ACR...
                    az acr login --name ${env.ACR_NAME}

                    echo Pushing Docker image to ACR...
                    docker push ${env.ACR_LOGIN_SERVER}/${env.APP_NAME}:${env.BUILD_NUMBER}
                    """
                }
            }
        }
    }
}