def latestTag = ''
pipeline {
  agent none

  options {
    buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '15', daysToKeepStr: '90', numToKeepStr: '')
    disableConcurrentBuilds()
  }

  environment {
    GHCR_IMAGE_BASE = 'ghcr.io/intranda/goobi-workflow'
    DOCKERHUB_IMAGE_BASE = 'intranda/goobi-workflow'
    NEXUS_IMAGE_BASE = 'nexus.intranda.com:4443/goobi-workflow'
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
            env.latestTag = sh(returnStdout: true, script:'git describe --tags --abbrev=0').trim()
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
    stage('build and publish image to Docker registries') {
      agent any
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
        withCredentials([
          usernamePassword(
            credentialsId: 'jenkins-github-container-registry',
            usernameVariable: 'GHCR_USER',
            passwordVariable: 'GHCR_PASS'
          ),
          usernamePassword(
            credentialsId: '0b13af35-a2fb-41f7-8ec7-01eaddcbe99d',
            usernameVariable: 'DOCKERHUB_USER',
            passwordVariable: 'DOCKERHUB_PASS'
          ),
          usernamePassword(
            credentialsId: 'jenkins-docker',
            usernameVariable: 'NEXUS_USER',
            passwordVariable: 'NEXUS_PASS'
          )
        ]) {
          sh '''
            # Login to registries
            echo "$GHCR_PASS" | docker login ghcr.io -u "$GHCR_USER" --password-stdin
            echo "$DOCKERHUB_PASS" | docker login docker.io -u "$DOCKERHUB_USER" --password-stdin
            echo "$NEXUS_PASS" | docker login nexus.intranda.com:4443 -u "$NEXUS_USER" --password-stdin
            
            # Setup QEMU and Buildx
            docker buildx create --name multiarch-builder --use || docker buildx use multiarch-builder
            docker buildx inspect --bootstrap

            # Tag logic
            TAGS=""
            echo "latestTag: $latestTag"
            if [ ! -z "$latestTag" ]; then
              TAGS="$TAGS -t $GHCR_IMAGE_BASE:$latestTag -t $DOCKERHUB_IMAGE_BASE:$latestTag -t $NEXUS_IMAGE_BASE:$latestTag"
            fi
            if [ "$GIT_BRANCH" = "origin/master" ] || [ "$GIT_BRANCH" = "master" ]; then
              TAGS="$TAGS -t $GHCR_IMAGE_BASE:latest -t $DOCKERHUB_IMAGE_BASE:latest -t $NEXUS_IMAGE_BASE:latest"
            elif [ "$GIT_BRANCH" = "origin/develop" ] || [ "$GIT_BRANCH" = "develop" ]; then
              TAGS="$TAGS -t $GHCR_IMAGE_BASE:develop -t $DOCKERHUB_IMAGE_BASE:develop -t $NEXUS_IMAGE_BASE:develop"
            elif echo "$GIT_BRANCH" | grep -q "_docker$"; then
              TAG_SUFFIX=$(echo "$GIT_BRANCH" | sed 's/_docker$//' | sed 's|/|_|g')
              TAGS="$TAGS -t $GHCR_IMAGE_BASE:$TAG_SUFFIX -t $DOCKERHUB_IMAGE_BASE:$TAG_SUFFIX -t $NEXUS_IMAGE_BASE:$TAG_SUFFIX"
            fi

            if [ -z "$TAGS" ]; then
              echo "No matching tag, skipping build."
              exit 0
            fi

            # Build and push to all registries
            docker buildx build --build-arg build=false \
              --platform linux/amd64,linux/arm64/v8,linux/ppc64le,linux/riscv64,linux/s390x \
              $TAGS \
              --push .
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
