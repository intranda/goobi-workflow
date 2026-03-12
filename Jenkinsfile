def mavenDockerImage = 'maven:3-eclipse-temurin-21'
def mavenDockerArgs = '-v $HOME/.m2:/var/maven/.m2:z -v $HOME/.config:/var/maven/.config -v $HOME/.sonar:/var/maven/.sonar -u 1000 -e _JAVA_OPTIONS=-Duser.home=/var/maven -e MAVEN_CONFIG=/var/maven/.m2'

pipeline {
  agent none

  options {
    buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '15', daysToKeepStr: '90', numToKeepStr: '')
    disableConcurrentBuilds()
    timeout(time: 30, unit: 'MINUTES')
  }

  environment {
    GHCR_IMAGE_BASE = 'ghcr.io/intranda/goobi-workflow'
    DOCKERHUB_IMAGE_BASE = 'intranda/goobi-workflow'
    NEXUS_IMAGE_BASE = 'nexus.intranda.com:4443/goobi-workflow'
  }

  stages {

    // ─────────────────────────────────────────────────────────────────────────
    // 1. SETUP
    // ─────────────────────────────────────────────────────────────────────────
    stage('setup') {
      agent any
      steps {
        sh 'git reset --hard HEAD && git clean -fdx'
        script {
          if (env.TAG_NAME) {
            env.BUILD_VERSION = env.TAG_NAME.replaceAll('^v', '')
          } else if (env.BRANCH_NAME == 'master') {
            env.BUILD_VERSION = 'latest-SNAPSHOT'
          } else {
            env.BUILD_VERSION = 'dev-SNAPSHOT'
          }
        }
      }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. BUILD  (sequential: core must be installed before plugins can build)
    // ─────────────────────────────────────────────────────────────────────────
    stage('build') {
      agent {
        docker {
          image mavenDockerImage
          args mavenDockerArgs
        }
      }
      stages {
        stage('workflow-base') {
          steps {
            sh 'mvn clean install -f config/workflow-base/pom.xml -Drevision=$BUILD_VERSION --no-transfer-progress'
          }
        }
        stage('core') {
          steps {
            // install so the JAR lands in .m2 for plugins
            sh 'mvn clean install -U -Drevision=$BUILD_VERSION -DskipTests -Dcheckstyle.skip=true -Djacoco.skip=true --no-transfer-progress'
            archiveArtifacts artifacts: 'target/*.war, target/*.jar, install/db/goobi.sql', fingerprint: true, onlyIfSuccessful: true
          }
        }
        stage('plugins') {
          steps {
            withCredentials([gitUsernamePassword(credentialsId: '93f7e7d3-8f74-4744-a785-518fc4d55314', gitToolName: 'git-tool')]) {
              sh 'git submodule update --init -- plugins/'
            }
            script {
              if (env.TAG_NAME || env.BRANCH_NAME == 'master') {
                // Update workflow-base parent version from dev-SNAPSHOT to the actual build version
                sh "sed -i '/<parent>/,/<\\/parent>/s|<version>dev-SNAPSHOT</version>|<version>'\$BUILD_VERSION'</version>|' plugins/goobi-plugin-*/pom.xml"
              }
            }
            // Plugin poms use ${revision} placeholder for their own version, default is dev-SNAPSHOT.
            sh 'mvn -f plugins/pom.xml clean install -U -T 1C -DskipTests -Dcheckstyle.skip=true -Djacoco.skip=true -Drevision=$BUILD_VERSION --no-transfer-progress'
            // Collect default plugin JARs into a staging dir for the Docker image (release only)
            script {
              if (env.TAG_NAME || env.BRANCH_NAME == 'master' || env.BRANCH_NAME == 'develop' || env.BRANCH_NAME?.endsWith('_docker')) {
                sh '''#!/bin/bash -xe
                    mkdir -p target/default-plugins/plugins/{opac,GUI,step,dashboard,statistics} \
                             target/default-plugins/lib \
                             target/default-plugins/config

                    for plugin in pica marc; do
                      find "plugins/goobi-plugin-opac-${plugin}" -name "plugin-opac-${plugin}-base.jar" \
                        -not -path "*sources*" | head -1 \
                        | xargs -I{} cp {} "target/default-plugins/plugins/opac/plugin-opac-${plugin}-base.jar" || true
                    done

                    for plugin in file-upload imageqa; do
                      find "plugins/goobi-plugin-step-${plugin}" -name "plugin-step-${plugin}-gui.jar" \
                        -not -path "*sources*" | head -1 \
                        | xargs -I{} cp {} "target/default-plugins/plugins/GUI/plugin-step-${plugin}-gui.jar" || true
                      find "plugins/goobi-plugin-step-${plugin}" -name "plugin-step-${plugin}-base.jar" \
                        -not -path "*sources*" | head -1 \
                        | xargs -I{} cp {} "target/default-plugins/plugins/step/plugin-step-${plugin}-base.jar" || true
                    done

                    find plugins/goobi-plugin-step-file-upload -name "plugin_intranda_step_fileUpload.xml" \
                      | head -1 | xargs -I{} cp {} target/default-plugins/config/plugin_intranda_step_fileUpload.xml || true
                    find plugins/goobi-plugin-step-imageqa -name "plugin_intranda_step_imageQA.xml" \
                      | head -1 | xargs -I{} cp {} target/default-plugins/config/plugin_intranda_step_imageQA.xml || true

                    for artifact in gui base api lib; do
                      find plugins/goobi-plugin-dashboard-extended -name "plugin-dashboard-extended-${artifact}.jar" \
                        -not -path "*sources*" | head -1 \
                        | xargs -I{} cp {} "target/default-plugins/plugins/$([ "$artifact" = "gui" ] && echo GUI || [ "$artifact" = "base" ] && echo dashboard || echo lib)/plugin-dashboard-extended-${artifact}.jar" || true
                    done
                    find plugins/goobi-plugin-dashboard-extended -name "plugin_intranda_dashboard_extended.xml" \
                      | head -1 | xargs -I{} cp {} target/default-plugins/config/plugin_intranda_dashboard_extended.xml || true

                    find plugins/goobi-plugin-rest-intranda -name "plugin-rest-intranda-api.jar" \
                      -not -path "*sources*" | head -1 \
                      | xargs -I{} cp {} target/default-plugins/lib/plugin-rest-intranda-api.jar || true

                    for file in plugin-statistics-intranda-gui.jar plugin-statistics-intranda-base.jar \
                                statistics_template.pdf statistics_template.xlsx; do
                      find plugins/goobi-plugin-statistics-intranda -name "${file}" \
                        -not -path "*sources*" | head -1 \
                        | xargs -I{} cp {} "target/default-plugins/plugins/statistics/${file}" || true
                    done
                '''
                stash includes: 'target/default-plugins/**', name: 'default-plugins'
              }
            }
            archiveArtifacts artifacts: 'plugins/**/target/*.jar, plugins/**/install/**', fingerprint: true, onlyIfSuccessful: true, allowEmptyArchive: true
            // Stash built artifacts and compiled classes for use in test and deploy stages
            // Excludes target/node/ (Node.js runtime) and target/workflow-core/ (exploded WAR)
            stash name: 'build-output', includes: [
              'target/classes/**',
              'target/*.war',
              'target/*.jar',
              'target/.flattened-pom.xml',
              'plugins/**/target/classes/**',
              'plugins/**/target/*.jar',
              'plugins/**/.flattened-pom.xml'
            ].join(',')
            sh 'cp -r /var/maven/.m2/repository/io/goobi/workflow m2-goobi'
            stash name: 'm2-goobi', includes: 'm2-goobi/**'
          }
        }
      }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. TEST + LINT  (parallel: core branch and plugins branch)
    //    Failures are hard on master/v*, unstable on all other branches
    // ─────────────────────────────────────────────────────────────────────────
    stage('test + lint') {
      parallel {

        stage('core') {
          agent {
            docker {
              image mavenDockerImage
              args mavenDockerArgs
            }
          }
          stages {
            stage('test') {
              steps {
                unstash 'm2-goobi'
                sh 'mkdir -p /var/maven/.m2/repository/io/goobi/workflow && cp -r m2-goobi/. /var/maven/.m2/repository/io/goobi/workflow/'
                unstash 'build-output'
                script {
                  def strict = env.TAG_NAME != null || env.BRANCH_NAME == 'master'
                  if (strict) {
                    sh "mvn test -Drevision=\$BUILD_VERSION -Dmaven.main.skip=true -P '!local-development' --no-transfer-progress"
                  } else {
                    catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
                      sh "mvn test -Drevision=\$BUILD_VERSION -Dmaven.main.skip=true -P '!local-development' --no-transfer-progress"
                    }
                  }
                }
                junit '**/target/surefire-reports/*.xml'
                step([
                  $class           : 'JacocoPublisher',
                  execPattern      : 'target/jacoco.exec',
                  classPattern     : 'target/classes/',
                  sourcePattern    : 'src/main/java',
                  exclusionPattern : '**/*Test.class'
                ])
              }
            }
            stage('checkstyle') {
              steps {
                sh 'mvn checkstyle:checkstyle -Drevision=$BUILD_VERSION -Dcheckstyle.skip=false --no-transfer-progress'
                script {
                  def strict = env.TAG_NAME != null || env.BRANCH_NAME == 'master'
                  recordIssues(
                    enabledForFailure: true,
                    aggregatingResults: false,
                    tools: [checkStyle(pattern: 'target/checkstyle-result.xml', reportEncoding: 'UTF-8')],
                    qualityGates: [[threshold: 1, type: 'TOTAL', unstable: !strict]]
                  )
                }
              }
            }
          }
        }

        stage('plugins') {
          agent {
            docker {
              image mavenDockerImage
              args mavenDockerArgs
            }
          }
          steps {
            unstash 'm2-goobi'
            sh 'mkdir -p /var/maven/.m2/repository/io/goobi/workflow && cp -r m2-goobi/. /var/maven/.m2/repository/io/goobi/workflow/'
            withCredentials([gitUsernamePassword(credentialsId: '93f7e7d3-8f74-4744-a785-518fc4d55314', gitToolName: 'git-tool')]) {
              sh 'git submodule update --init -- plugins/'
            }
            unstash 'build-output'
            script {
              if (env.TAG_NAME || env.BRANCH_NAME == 'master') {
                sh "sed -i '/<parent>/,/<\\/parent>/s|<version>dev-SNAPSHOT</version>|<version>'\$BUILD_VERSION'</version>|' plugins/goobi-plugin-*/pom.xml"
              }
            }
            script {
              def strict = env.TAG_NAME != null || env.BRANCH_NAME == 'master'
              def cmd = "mvn -f plugins/pom.xml test -T 1C -Dmaven.main.skip=true -Drevision=\$BUILD_VERSION -P '!local-development' --no-transfer-progress"
              if (strict) {
                sh cmd
              } else {
                catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
                  sh cmd
                }
              }
            }
            junit 'plugins/**/target/surefire-reports/*.xml'
          }
        }

      }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. SONAR  (runs after tests so jacoco.exec is available)
    // ─────────────────────────────────────────────────────────────────────────
    stage('sonar') {
      agent {
        docker {
          image mavenDockerImage
          args mavenDockerArgs
        }
      }
      when {
        beforeAgent true
        anyOf {
          branch 'master'
          branch 'v*'
          branch 'sonar_*'
        }
      }
      steps {
        unstash 'm2-goobi'
        sh 'mkdir -p /var/maven/.m2/repository/io/goobi/workflow && cp -r m2-goobi/. /var/maven/.m2/repository/io/goobi/workflow/'
        unstash 'build-output'
        withCredentials([string(credentialsId: 'jenkins-sonarcloud', variable: 'TOKEN')]) {
          sh 'mvn sonar:sonar -Drevision=$BUILD_VERSION -Dsonar.token=$TOKEN -U --no-transfer-progress'
        }
      }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. DEPLOY  (core + plugins to Nexus)
    //    core:    master, develop, v*
    //    plugins: master, v*
    // ─────────────────────────────────────────────────────────────────────────
    stage('deploy') {
      agent {
        docker {
          image mavenDockerImage
          args mavenDockerArgs
        }
      }
      when {
        beforeAgent true
        anyOf {
          branch 'master'
          branch 'develop'
          branch 'v*'
        }
      }
      steps {
        unstash 'm2-goobi'
        sh 'mkdir -p /var/maven/.m2/repository/io/goobi/workflow && cp -r m2-goobi/. /var/maven/.m2/repository/io/goobi/workflow/'
        unstash 'build-output'
        sh 'mvn deploy -Dmaven.main.skip=true -Dmaven.test.skip=true -Drevision=$BUILD_VERSION -U --no-transfer-progress'
        script {
          if (env.TAG_NAME || env.BRANCH_NAME == 'master') {
            withCredentials([gitUsernamePassword(credentialsId: '93f7e7d3-8f74-4744-a785-518fc4d55314', gitToolName: 'git-tool')]) {
              sh 'git submodule update --init -- plugins/'
            }
            sh "sed -i '/<parent>/,/<\\/parent>/s|<version>dev-SNAPSHOT</version>|<version>'\$BUILD_VERSION'</version>|' plugins/goobi-plugin-*/pom.xml"
            sh '''#!/bin/bash -xe
                for plugin_dir in plugins/goobi-plugin-*/; do
                  [ -f "${plugin_dir}module-lib/pom.xml" ] || continue
                  mvn -f "${plugin_dir}pom.xml" -N deploy \
                    -Dmaven.main.skip=true -Dmaven.test.skip=true -Drevision=$BUILD_VERSION -U --no-transfer-progress
                  mvn -f "${plugin_dir}module-lib/pom.xml" deploy \
                    -Dmaven.main.skip=true -Dmaven.test.skip=true -Drevision=$BUILD_VERSION -U --no-transfer-progress
                done
            '''
          }
        }
      }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. DOCKER  (build multiarch image and push to all registries)
    // ─────────────────────────────────────────────────────────────────────────
    stage('docker') {
      agent any
      when {
        beforeAgent true
        anyOf {
          branch 'master'
          branch 'develop'
          branch 'v*'
          expression { return env.BRANCH_NAME =~ /_docker$/ }
        }
      }
      steps {
        unstash 'build-output'
        unstash 'default-plugins'
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
            echo "$GHCR_PASS"     | docker login ghcr.io              -u "$GHCR_USER"     --password-stdin
            echo "$DOCKERHUB_PASS"| docker login docker.io            -u "$DOCKERHUB_USER" --password-stdin
            echo "$NEXUS_PASS"    | docker login nexus.intranda.com:4443 -u "$NEXUS_USER" --password-stdin

            docker buildx create --name multiarch-builder --use || docker buildx use multiarch-builder
            docker buildx inspect --bootstrap

            TAGS=""
            if [ -n "$TAG_NAME" ]; then
              TAGS="-t $GHCR_IMAGE_BASE:$TAG_NAME -t $DOCKERHUB_IMAGE_BASE:$TAG_NAME -t $NEXUS_IMAGE_BASE:$TAG_NAME"
            elif [ "$GIT_BRANCH" = "origin/master" ] || [ "$GIT_BRANCH" = "master" ]; then
              TAGS="-t $GHCR_IMAGE_BASE:latest -t $DOCKERHUB_IMAGE_BASE:latest -t $NEXUS_IMAGE_BASE:latest"
            elif [ "$GIT_BRANCH" = "origin/develop" ] || [ "$GIT_BRANCH" = "develop" ]; then
              TAGS="-t $GHCR_IMAGE_BASE:dev -t $DOCKERHUB_IMAGE_BASE:dev -t $NEXUS_IMAGE_BASE:dev"
            elif echo "$GIT_BRANCH" | grep -q "_docker$"; then
              TAG_SUFFIX=$(echo "$GIT_BRANCH" | sed 's/_docker$//' | sed 's|/|_|g')
              TAGS="-t $GHCR_IMAGE_BASE:$TAG_SUFFIX -t $DOCKERHUB_IMAGE_BASE:$TAG_SUFFIX -t $NEXUS_IMAGE_BASE:$TAG_SUFFIX"
            fi

            if [ -z "$TAGS" ]; then
              echo "No matching tag, skipping build."
              exit 0
            fi

            mkdir -p target/default-plugins

            SLIM_TAGS=$(echo "$TAGS" | sed -E 's|-t ([^ ]*)|-t \1-slim|g')

            docker buildx build --build-arg build=false \
              --platform linux/amd64,linux/arm64/v8,linux/ppc64le,linux/riscv64,linux/s390x \
              --target slim \
              $SLIM_TAGS \
              --push .

            docker buildx build --build-arg build=false \
              --platform linux/amd64,linux/arm64/v8,linux/ppc64le,linux/riscv64,linux/s390x \
              --target full \
              $TAGS \
              --push .
          '''
        }
      }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. PLUGIN RELEASE  (create version-bump commits and git tags for all plugins)
    // ─────────────────────────────────────────────────────────────────────────
    stage('plugin-release') {
      agent {
        docker {
          image mavenDockerImage
          args mavenDockerArgs
        }
      }
      when {
        beforeAgent true
        branch 'v*'
      }
      steps {
        withCredentials([gitUsernamePassword(credentialsId: '93f7e7d3-8f74-4744-a785-518fc4d55314',
                 gitToolName: 'git-tool')]) {
          sh 'git submodule update --init -- plugins/'
          sh '''#!/bin/bash -xe
              VERSION="${BUILD_VERSION}"
              TAG="v${VERSION}"

              # Replace all ${revision} and dev-SNAPSHOT across all plugin poms in one pass
              find plugins -name 'pom.xml' -not -path '*/target/*' \
                | xargs sed -i \
                  -e 's/\${revision}/'"${VERSION}"'/g' \
                  -e 's/dev-SNAPSHOT/'"${VERSION}"'/g'

              for plugin_dir in plugins/*/; do
                [ -f "${plugin_dir}pom.xml" ] || continue
                (
                  cd "${plugin_dir}"

                  # Stage all modified pom files and commit if anything changed
                  find . -name 'pom.xml' -not -path '*/target/*' | xargs git add
                  git diff --cached --quiet || git commit -m "Release ${VERSION}"

                  git tag -a "${TAG}" -m "Release ${VERSION}" \
                    || echo "Tag ${TAG} already exists in $(pwd), skipping"
                  git push origin "${TAG}" \
                    || echo "Tag ${TAG} already pushed in $(pwd), skipping"
                )
              done
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