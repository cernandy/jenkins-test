pipeline {
    agent any 
    environment {
        //once you sign up for Docker hub, use that user_id here
        first_app = "cernandy/first-app"
        second_app = "cernandy/second-app"
        third_app = "cernandy/third-app"

        //- update your credentials ID after creating credentials for connecting to Docker Hub
        registryCredential = 'dockerhub_id'
        dockerImage = ''
    }
    

    parameters {
        booleanParam(name: 'FIRST_APP' , defaultValue: true, description: 'Build first app')
        booleanParam(name: 'SECOND_APP', defaultValue: true, description: 'Build second app')
        booleanParam(name: 'THIRD_APP', defaultValue: true, description: 'Build third app')
    }


    stages {
        stage('Cloning Git') {
            steps {
                checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '', url: 'https://github.com/cernandy/jenkins-test.git']]])       
            }
        }

     // Stopping Docker containers for cleaner Docker run
        stage('docker stop containers') {
            steps {
                sh 'docker ps -f name=first-app -q | xargs --no-run-if-empty docker container stop'
                sh 'docker container ls -a -fname=first-app -q | xargs -r docker container rm'

                sh 'docker ps -f name=second-app -q | xargs --no-run-if-empty docker container stop'
                sh 'docker container ls -a -fname=second-app -q | xargs -r docker container rm'

                sh 'docker ps -f name=third-app -q | xargs --no-run-if-empty docker container stop'
                sh 'docker container ls -a -fname=third-app -q | xargs -r docker container rm'
            }
        }
    
    // Building Docker images
        stage('Building App #1') {
            when { expression { params.FIRST_APP } }

            steps{
                dir('app_1/'){
                    script {
                        dockerImage = docker.build first_app
                        docker.withRegistry( '', registryCredential ) {
                            dockerImage.push()
                        }

                        dockerImage.run("-p 8096:5000 --rm --name first-app")
                    }
                }  
            }
        }


        stage('Building App #2') {
            when { expression { params.SECOND_APP } }


            steps {
                dir('app_2/'){
                    script {
                        dockerImage = docker.build second_app
                        docker.withRegistry( '', registryCredential ) {
                            dockerImage.push()
                        }
                        dockerImage.run("-p 8097:5000 --rm --name second-app")
                    }
                }  
            }
        }

        stage('Building App #3') {
            when { expression { params.THIRD_APP} }


            steps {
                dir('app_3/'){
                    script {
                        dockerImage = docker.build third_app
                        docker.withRegistry( '', registryCredential ) {
                            dockerImage.push()
                        }
                        dockerImage.run("-p 8098:5000 --rm --name third-app")
                    }
                }  
            }
        }
    }
}