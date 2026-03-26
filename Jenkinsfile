def mavenDockerImage = 'maven:3-eclipse-temurin-21'
def mavenDockerArgs = '-v $HOME/.m2:/var/maven/.m2:z -v $HOME/.config:/var/maven/.config -v $HOME/.sonar:/var/maven/.sonar -u 1000 -e _JAVA_OPTIONS=-Duser.home=/var/maven -e MAVEN_CONFIG=/var/maven/.m2'

pipeline {
  agent any

  options {
    buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '15', daysToKeepStr: '90', numToKeepStr: '')
    disableConcurrentBuilds()
    timeout(time: 45, unit: 'MINUTES')
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
          reuseNode true
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
            sh "mvn clean install -U -Drevision=\$BUILD_VERSION -DskipTests -Dcheckstyle.skip=true -Djacoco.skip=true -P '!local-development' --no-transfer-progress"
            archiveArtifacts artifacts: 'target/*.war, target/*.jar, install/db/goobi.sql', fingerprint: true, onlyIfSuccessful: true
          }
        }
        stage('plugins') {
          steps {
            script {
              if (env.TAG_NAME || env.BRANCH_NAME == 'master') {
                // Update workflow-base parent version from dev-SNAPSHOT to the actual build version
                sh "sed -i '/<parent>/,/<\\/parent>/s|<version>dev-SNAPSHOT</version>|<version>'\$BUILD_VERSION'</version>|' plugins/goobi-plugin-*/pom.xml"
              }
            }
            // Plugin poms use ${revision} placeholder for their own version, default is dev-SNAPSHOT.
            sh "mvn -f plugins/pom.xml clean install -U -T 1C -DskipTests -Dcheckstyle.skip=true -Djacoco.skip=true -Drevision=\$BUILD_VERSION -P '!local-development' --no-transfer-progress"
            // Collect default plugin JARs into a staging dir for the Docker image (release only)
            script {
              if (env.TAG_NAME || env.BRANCH_NAME == 'master' || env.BRANCH_NAME == 'develop' || env.BRANCH_NAME?.endsWith('_docker')) {
                sh '''#!/bin/bash -xe
                    mkdir -p target/default-plugins/plugins/{opac,GUI,step,dashboard,statistics} \
                             target/default-plugins/lib \
                             target/default-plugins/config

                    # opac
                    cp plugins/goobi-plugin-opac-pica/module-base/target/plugin-opac-pica-base.jar target/default-plugins/plugins/opac/
                    cp plugins/goobi-plugin-opac-marc/module-base/target/plugin-opac-marc-base.jar target/default-plugins/plugins/opac/

                    # step: file-upload
                    cp plugins/goobi-plugin-step-file-upload/module-base/target/plugin-step-file-upload-base.jar target/default-plugins/plugins/step/
                    cp plugins/goobi-plugin-step-file-upload/module-gui/target/plugin-step-file-upload-gui.jar   target/default-plugins/plugins/GUI/
                    cp plugins/goobi-plugin-step-file-upload/install/plugin_intranda_step_fileUpload.xml         target/default-plugins/config/

                    # step: imageqa
                    cp plugins/goobi-plugin-step-imageqa/module-base/target/plugin-step-imageqa-base.jar target/default-plugins/plugins/step/
                    cp plugins/goobi-plugin-step-imageqa/module-gui/target/plugin-step-imageqa-gui.jar   target/default-plugins/plugins/GUI/
                    cp plugins/goobi-plugin-step-imageqa/install/plugin_intranda_step_imageQA.xml        target/default-plugins/config/

                    # dashboard-extended
                    cp plugins/goobi-plugin-dashboard-extended/module-gui/target/plugin-dashboard-extended-gui.jar   target/default-plugins/plugins/GUI/
                    cp plugins/goobi-plugin-dashboard-extended/module-base/target/plugin-dashboard-extended-base.jar target/default-plugins/plugins/dashboard/
                    cp plugins/goobi-plugin-dashboard-extended/module-api/target/plugin-dashboard-extended-api.jar   target/default-plugins/lib/
                    cp plugins/goobi-plugin-dashboard-extended/module-lib/target/plugin-dashboard-extended-lib.jar   target/default-plugins/lib/
                    cp plugins/goobi-plugin-dashboard-extended/install/plugin_intranda_dashboard_extended.xml        target/default-plugins/config/

                    # rest-intranda
                    cp plugins/goobi-plugin-rest-intranda/module-api/target/plugin-rest-intranda-api.jar target/default-plugins/lib/

                    # statistics-intranda
                    cp plugins/goobi-plugin-statistics-intranda/module-gui/target/plugin-statistics-intranda-gui.jar   target/default-plugins/plugins/GUI/
                    cp plugins/goobi-plugin-statistics-intranda/module-base/target/plugin-statistics-intranda-base.jar target/default-plugins/plugins/statistics/
                    cp plugins/goobi-plugin-statistics-intranda/install/statistics_template.pdf                        target/default-plugins/plugins/statistics/
                    cp plugins/goobi-plugin-statistics-intranda/install/statistics_template.xlsx                       target/default-plugins/plugins/statistics/
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
                archiveArtifacts artifacts: 'target/checkstyle-result.xml', allowEmptyArchive: true
                script {
                  def strict = (env.TAG_NAME != null || env.BRANCH_NAME == 'master') && !${NO_STRICT_CHECKSTYLE}
                  recordIssues(
                    id: 'checkstyle-core',
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
              reuseNode true
            }
          }
          stages {
            stage('test') {
              steps {
                unstash 'm2-goobi'
                sh 'mkdir -p /var/maven/.m2/repository/io/goobi/workflow && cp -r m2-goobi/. /var/maven/.m2/repository/io/goobi/workflow/'
                unstash 'build-output'
                script {
                  if (env.TAG_NAME || env.BRANCH_NAME == 'master') {
                    sh "sed -i '/<parent>/,/<\\/parent>/s|<version>dev-SNAPSHOT</version>|<version>'\$BUILD_VERSION'</version>|' plugins/goobi-plugin-*/pom.xml"
                  }
                }
                script {
                  def strict = env.TAG_NAME != null || env.BRANCH_NAME == 'master'
                  def cmd = "mvn -f plugins/pom.xml test -T 1C -Dmaven.main.skip=true -Drevision=\$BUILD_VERSION -P '!local-development' --no-transfer-progress -fae"
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
            stage('checkstyle') {
              steps {
                sh "mvn -f plugins/pom.xml checkstyle:checkstyle -T 1C -Drevision=\$BUILD_VERSION -Dcheckstyle.skip=false -Dmaven.main.skip=true --no-transfer-progress"
                archiveArtifacts artifacts: 'plugins/**/target/checkstyle-result.xml', allowEmptyArchive: true
                script {
                  def strict = (env.TAG_NAME != null || env.BRANCH_NAME == 'master') && !${NO_STRICT_CHECKSTYLE}
                  recordIssues(
                    id: 'checkstyle-plugins',
                    enabledForFailure: true,
                    aggregatingResults: false,
                    tools: [checkStyle(pattern: 'plugins/**/target/checkstyle-result.xml', reportEncoding: 'UTF-8')],
                    qualityGates: [[threshold: 1, type: 'TOTAL', unstable: !strict]]
                  )
                }
              }
            }
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
          reuseNode true
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
    // 5. DEPLOY  (base + core + plugins to Nexus)
    //    core:    master, develop, v*
    //    plugins: master, v*
    // ─────────────────────────────────────────────────────────────────────────
    stage('deploy') {
      agent {
        docker {
          image mavenDockerImage
          args mavenDockerArgs
          reuseNode true
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
        sh 'mvn deploy -f config/workflow-base/pom.xml -Dmaven.main.skip=true -Dmaven.test.skip=true -Drevision=$BUILD_VERSION -U --no-transfer-progress'
        script {
          if (env.TAG_NAME || env.BRANCH_NAME == 'master') {
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
            SLIM_TAGS=""
            if [ -n "$TAG_NAME" ]; then
              TAGS="-t $GHCR_IMAGE_BASE:$TAG_NAME -t $DOCKERHUB_IMAGE_BASE:$TAG_NAME -t $NEXUS_IMAGE_BASE:$TAG_NAME"
              SLIM_TAGS="-t $GHCR_IMAGE_BASE:$TAG_NAME-slim -t $DOCKERHUB_IMAGE_BASE:$TAG_NAME-slim -t $NEXUS_IMAGE_BASE:$TAG_NAME-slim"
            elif [ "$GIT_BRANCH" = "origin/master" ] || [ "$GIT_BRANCH" = "master" ]; then
              TAGS="-t $GHCR_IMAGE_BASE:latest -t $DOCKERHUB_IMAGE_BASE:latest -t $NEXUS_IMAGE_BASE:latest"
              SLIM_TAGS="-t $GHCR_IMAGE_BASE:latest-slim -t $DOCKERHUB_IMAGE_BASE:latest-slim -t $NEXUS_IMAGE_BASE:latest-slim"
            elif [ "$GIT_BRANCH" = "origin/develop" ] || [ "$GIT_BRANCH" = "develop" ]; then
              TAGS="-t $GHCR_IMAGE_BASE:dev -t $DOCKERHUB_IMAGE_BASE:dev -t $NEXUS_IMAGE_BASE:dev"
              SLIM_TAGS="-t $GHCR_IMAGE_BASE:dev-slim -t $DOCKERHUB_IMAGE_BASE:dev-slim -t $NEXUS_IMAGE_BASE:dev-slim"
            elif echo "$GIT_BRANCH" | grep -q "_docker$"; then
              TAGS="-t $NEXUS_IMAGE_BASE:docker-dev"
              SLIM_TAGS="-t $NEXUS_IMAGE_BASE:docker-dev-slim"
            fi

            if [ -z "$TAGS" ]; then
              echo "No matching tag, skipping build."
              exit 0
            fi

            if [ -n "$TAG_NAME" ] || [ "$GIT_BRANCH" = "origin/master" ] || [ "$GIT_BRANCH" = "master" ]; then
              PLATFORMS="linux/amd64,linux/arm64/v8,linux/ppc64le,linux/riscv64,linux/s390x"
            else
              PLATFORMS="linux/amd64,linux/arm64/v8"
            fi

            CACHE="--cache-from type=registry,ref=$NEXUS_IMAGE_BASE:buildcache --cache-to type=registry,ref=$NEXUS_IMAGE_BASE:buildcache,mode=max"

            mkdir -p target/default-plugins

            docker buildx build --build-arg build=false \
              --platform $PLATFORMS \
              --target slim \
              $CACHE \
              $SLIM_TAGS \
              --push .

            docker buildx build --build-arg build=false \
              --platform $PLATFORMS \
              --target full \
              $CACHE \
              $TAGS \
              --push .
          '''
        }
      }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. UPDATE COLLECTION  (master only: advance core submodule pointer in collection)
    // ─────────────────────────────────────────────────────────────────────────
    stage('update-collection') {
      when {
        branch 'master'
      }
      agent any
      steps {
        withCredentials([gitUsernamePassword(credentialsId: '93f7e7d3-8f74-4744-a785-518fc4d55314', gitToolName: 'git-tool')]) {
          sh '''#!/bin/bash -xe
            WORK_DIR=$(mktemp -d)
            git clone --depth 1 --branch master "$COLLECTION_REPO_URL" "$WORK_DIR"
            cd "$WORK_DIR"
            git submodule update --init --remote -- goobi-workflow-core
            if git status --porcelain -- goobi-workflow-core | grep -q .; then
              git add goobi-workflow-core
              git commit -m "Update goobi-workflow-core to latest master"
              git push origin master
            else
              echo "Submodule already up to date."
            fi
            rm -rf "$WORK_DIR"
          '''
        }
      }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // 8. PLUGIN RELEASE  (create version-bump commits and git tags for all plugins)
    // ─────────────────────────────────────────────────────────────────────────
    stage('plugin-release') {
      agent {
        docker {
          image mavenDockerImage
          args mavenDockerArgs
          reuseNode true
        }
      }
      when {
        beforeAgent true
        branch 'v*'
      }
      steps {
        withCredentials([gitUsernamePassword(credentialsId: '93f7e7d3-8f74-4744-a785-518fc4d55314',
                 gitToolName: 'git-tool')]) {
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


    // ─────────────────────────────────────────────────────────────────────────
    // 9. TRIGGER COLLECTION  (downstream trigger for develop and master)
    // ─────────────────────────────────────────────────────────────────────────
    stage('trigger-collection') {
      when {
        beforeAgent true
        anyOf {
          branch 'master'
          branch 'develop'
        }
      }
      steps {
        script {
          build job: 'goobi-workflow-collection/master',
                parameters: [string(name: 'UPSTREAM_BRANCH', value: env.BRANCH_NAME)],
                wait: false
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