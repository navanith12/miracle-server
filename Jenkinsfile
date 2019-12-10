pipeline {
    agent any
    stages {
        stage('Build'){
            steps{
                sh 'mvn clean package'
                sh 'docker build -t miracleServer .'
                sh 'docker run -d miracleServer'
            }
        }
    }
}

