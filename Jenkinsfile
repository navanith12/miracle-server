pipeline {
    agent any
    stages {
        stage('Build'){
            steps{
                sh 'mvn clean package'
                sh 'docker images'
                sh 'docker build . -t miracleserver'
                sh 'docker images'
                sh 'docker run -d miracleserver'
                sh 'docker ps'
                sh 'EXIT'
            }
        }
    }
}

