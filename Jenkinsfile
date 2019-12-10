peline {
    agent any
    stages {
        stage('Build'){
            steps{
                sh 'mvn clean package'
                sh "docker build . -t miracleServer:${env.BUILD_ID}"
            }
        }
}
}

