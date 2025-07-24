def latestTag = ''
pipeline {
  agent none

  options {
    buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '15', daysToKeepStr: '90', numToKeepStr: '')
    disableConcurrentBuilds()
  }

  stages {
    stage('prepare') {
      agent any
      steps {
        sh 'git reset --hard HEAD && git clean -fdx'
      }
    }
    stage('build-snapshot') {
      agent {
        docker {
          image 'maven:3-eclipse-temurin-21'
          args '-v $HOME/.m2:/var/maven/.m2:z -v $HOME/.config:/var/maven/.config -v $HOME/.sonar:/var/maven/.sonar -u 1000 -ti -e _JAVA_OPTIONS=-Duser.home=/var/maven -e MAVEN_CONFIG=/var/maven/.m2'
        }
      }
      when {
        not {
          anyOf {
            branch 'master'
            branch 'release_*'
            branch 'hotfix_release_*'
            branch 'sonar_*'
            allOf {
              branch 'PR-*'
              expression { env.CHANGE_BRANCH.startsWith("release_") }
            }
          }
        }
      }
      steps {
        sh 'mvn clean verify -U -P snapshot-build'
        junit "**/target/surefire-reports/*.xml"
        step([
          $class           : 'JacocoPublisher',
          execPattern      : 'target/jacoco.exec',
          classPattern     : 'target/classes/',
          sourcePattern    : 'src/main/java',
          exclusionPattern : '**/*Test.class'
        ])
        recordIssues (
          enabledForFailure: true, aggregatingResults: false,
          tools: [checkStyle(pattern: 'target/checkstyle-result.xml', reportEncoding: 'UTF-8')]
        )
        archiveArtifacts artifacts: 'target/*.war, target/*.jar, install/db/goobi.sql', fingerprint: true
        stash includes: 'target/**', name: 'target'
      }
    }
    stage('build-release') {
      agent {
        docker {
          image 'maven:3-eclipse-temurin-21'
          args '-v $HOME/.m2:/var/maven/.m2:z -v $HOME/.config:/var/maven/.config -v $HOME/.sonar:/var/maven/.sonar -u 1000 -ti -e _JAVA_OPTIONS=-Duser.home=/var/maven -e MAVEN_CONFIG=/var/maven/.m2'
        }
      }
      when {
        anyOf {
          branch 'master'
          branch 'release_*'
          branch 'hotfix_release_*'
          allOf {
            branch 'PR-*'
            expression { env.CHANGE_BRANCH.startsWith("release_") }
          }
        }
      }
      steps {
        sh 'mvn clean verify -U -P release-build'
        step([
          $class           : 'JacocoPublisher',
          execPattern      : 'target/jacoco.exec',
          classPattern     : 'target/classes/',
          sourcePattern    : 'src/main/java',
          exclusionPattern : '**/*Test.class'
        ])
        recordIssues (
          enabledForFailure: true, aggregatingResults: false,
          tools: [checkStyle(pattern: 'target/checkstyle-result.xml', reportEncoding: 'UTF-8')]
        )
        archiveArtifacts artifacts: 'target/*.war, target/*.jar, install/db/goobi.sql', fingerprint: true
        stash includes: 'target/**', name: 'target'
      }
    }
    stage('build-sonar') {
      agent {
        docker {
          image 'maven:3-eclipse-temurin-21'
          args '-v $HOME/.m2:/var/maven/.m2:z -v $HOME/.config:/var/maven/.config -v $HOME/.sonar:/var/maven/.sonar -u 1000 -ti -e _JAVA_OPTIONS=-Duser.home=/var/maven -e MAVEN_CONFIG=/var/maven/.m2'
        }
      }
      when {
        branch 'sonar_*'
      }
      steps {
        sh 'mvn clean verify -U -P sonar-build'
        step([
          $class           : 'JacocoPublisher',
          execPattern      : 'target/jacoco.exec',
          classPattern     : 'target/classes/',
          sourcePattern    : 'src/main/java',
          exclusionPattern : '**/*Test.class'
        ])
        recordIssues (
          enabledForFailure: true, aggregatingResults: false,
          tools: [checkStyle(pattern: 'target/checkstyle-result.xml', reportEncoding: 'UTF-8')]
        )
        archiveArtifacts artifacts: 'target/*.war, target/*.jar, install/db/goobi.sql', fingerprint: true
        stash includes: 'target/**', name: 'target'
      }
    }
    stage('sonarcloud') {
      agent {
        docker {
          image 'maven:3-eclipse-temurin-21'
          args '-v $HOME/.m2:/var/maven/.m2:z -v $HOME/.config:/var/maven/.config -v $HOME/.sonar:/var/maven/.sonar -u 1000 -ti -e _JAVA_OPTIONS=-Duser.home=/var/maven -e MAVEN_CONFIG=/var/maven/.m2'
        }
      }
      when {
        anyOf {
          branch 'master'
          branch 'release_*'
          branch 'hotfix_release_*'
          branch 'sonar_*'
          allOf {
            branch 'PR-*'
            expression { env.CHANGE_BRANCH.startsWith("release_") }
          }
        }
      }
      steps {
        unstash 'target'
        withCredentials([string(credentialsId: 'jenkins-sonarcloud', variable: 'TOKEN')]) {
          sh 'mvn verify sonar:sonar -Dsonar.token=$TOKEN -U'
        }
      }
    }
    stage('deploy') {
      agent {
        docker {
          image 'maven:3-eclipse-temurin-21'
          args '-v $HOME/.m2:/var/maven/.m2:z -v $HOME/.config:/var/maven/.config -v $HOME/.sonar:/var/maven/.sonar -u 1000 -ti -e _JAVA_OPTIONS=-Duser.home=/var/maven -e MAVEN_CONFIG=/var/maven/.m2'
        }
      }
      when {
        anyOf {
          branch 'master'
          branch 'develop'
          branch 'hotfix_release_*'
        }
      }
      steps {
        unstash 'target'
        sh 'mvn deploy -U'
      }
    }
    stage('tag release') {
      agent {
        docker {
          image 'maven:3-eclipse-temurin-21'
          args '-v $HOME/.m2:/var/maven/.m2:z -v $HOME/.config:/var/maven/.config -v $HOME/.sonar:/var/maven/.sonar -u 1000 -ti -e _JAVA_OPTIONS=-Duser.home=/var/maven -e MAVEN_CONFIG=/var/maven/.m2'
        }
      }
      when { 
        anyOf {
          branch 'master'
          branch 'hotfix_release_*'
        }
      }
      steps {
        unstash 'target'
        withCredentials([gitUsernamePassword(credentialsId: '93f7e7d3-8f74-4744-a785-518fc4d55314',
                 gitToolName: 'git-tool')]) {
          sh '''#!/bin/bash -xe
              projectversion=$(mvn org.apache.maven.plugins:maven-help-plugin:3.4.0:evaluate -Dexpression=project.version -q -DforceStdout)
              if [ $? != 0 ]
              then
                  exit 1
              elif [[ "${projectversion}" =~ "SNAPSHOT" ]]
              then
                  echo "This is a SNAPSHOT version"
                  exit 1
              fi
              echo "${projectversion}"
              git tag -a "v${projectversion}" -m "releasing v${projectversion}" && git push origin v"${projectversion}"
          '''
          script {
            latestTag = sh(returnStdout: true, script:'git describe --tags --abbrev=0').trim()
          }
        }
      }
    }
    stage('trigger docker build') {
      agent any
      when { 
        anyOf {
          branch 'master'
          branch 'hotfix_release_*'
          branch 'develop'
        }
      }
      steps {
        build wait: false, job: 'goobi-workflow/goobi-docker/master', parameters: [[$class: 'StringParameterValue', name: 'UPSTREAM_BRANCH', value: String.valueOf(BRANCH_NAME)]]
      }
    }
    stage('build and publish production image to GitHub container registry') {
      agent {
        docker {
          image 'docker:24.0-cli'
          args '--user root --privileged -v /var/run/docker.sock:/var/run/docker.sock'
        }
      }
      environment {
        IMAGE_NAME = "ghcr.io/intranda/goobi-workflow"
      }
      when {
        anyOf {
          branch 'master'
          branch 'hotfix_release_*'
          branch 'develop'
          expression { return env.BRANCH_NAME =~ /_docker$/ }
        }
      }
      steps {
        unstash 'target'
        withCredentials([usernamePassword(
          credentialsId: 'jenkins-github-container-registry',
          usernameVariable: 'DOCKER_USER',
          passwordVariable: 'DOCKER_PASS'
        )]) {
          sh '''
            echo "$DOCKER_PASS" | docker login ghcr.io -u "$DOCKER_USER" --password-stdin

            # Setup QEMU and Buildx
            docker run --privileged --rm tonistiigi/binfmt --install all || true
            docker buildx create --name multiarch-builder --use || docker buildx use multiarch-builder
            docker buildx inspect --bootstrap

            TAGS=""

            if [ ! -z "$latestTag" ]; then
              TAGS="$TAGS -t $IMAGE_NAME:$latestTag"
            elif [ "$GIT_BRANCH" = "origin/develop" ] || [ "$GIT_BRANCH" = "develop" ]; then
              TAGS="$TAGS -t $IMAGE_NAME:develop"
            elif echo "$GIT_BRANCH" | grep -q "_docker$"; then
              BASE_TAG=$(echo "$GIT_BRANCH" | sed 's/_docker$//' | sed 's|/|_|g')
              TAGS="$TAGS -t $IMAGE_NAME:$BASE_TAG"
            fi

            if [ -z "$TAGS" ]; then
              echo "No tag matched. Skipping build."
              exit 0
            fi

            docker buildx build --build-arg build=false --platform linux/amd64,linux/arm64/v8,linux/ppc64le,linux/riscv64,linux/s390x $TAGS --push .
          '''
        }
      }
    }
  }
  post {
    changed {
      emailext(
        subject: '${DEFAULT_SUBJECT}',
        body: '${DEFAULT_CONTENT}',
        recipientProviders: [requestor(),culprits()],
        attachLog: true
      )
    }
  }
}
/* vim: set ts=2 sw=2 tw=120 et :*/
