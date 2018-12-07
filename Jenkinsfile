pipeline {

  agent {
    docker {
      image 'maven:3-jdk-8'
      args '-v $HOME/.m2:/root/.m2'
    }
  }


  triggers {
    pollSCM 'H/15 * * * *'
  }
  stages {
    stage('build') {
      steps {
              sh 'mvn -f Goobi/pom.xml install'
            }
    }
  }
  post {
    success {
      archiveArtifacts artifacts: 'Goobi/module-war/target/*.war', fingerprint: true
    }
  }
}
/* vim: set ts=2 sw=2 tw=120 et :*/
