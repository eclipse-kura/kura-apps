podTemplate(inheritFrom: 'basic', yaml: '''
spec:
  containers:
  - name: "jnlp"
    resources:
      limits:
        cpu: "2000m"
        memory: "4Gi"
      requests:
        cpu: "1000m"
        memory: "3Gi"
''') {
    node(POD_LABEL) {
        properties([
            disableConcurrentBuilds(abortPrevious: true),
            buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '2', daysToKeepStr: '', numToKeepStr: '5')),
            gitLabConnection('gitlab.eclipse.org'),
            [$class: 'RebuildSettings', autoRebuild: false, rebuildDisabled: false],
            [$class: 'JobLocalConfiguration', changeReasonComment: '']
        ])

        deleteDir()

        stage('prepare') {
            dir('kura-apps') {
                checkout scm
            }

            dir('kura') {
                git branch: 'develop', changelog: false, poll: false, url: 'https://github.com/eclipse-kura/kura.git'
            }
        }

        stage('Build Kura target-platform') {
            timeout(time: 1, unit: 'HOURS') {
                dir('kura') {
                    withMaven(jdk: 'temurin-jdk17-latest', maven: 'apache-maven-3.9.6') {
                        sh 'mvn -f target-platform/pom.xml clean install -Pno-mirror -Pcheck-exists-plugin -Dmaven.test.skip=true'
                    }
                }
            }
        }

        stage('Build Kura core') {
            timeout(time: 2, unit: 'HOURS') {
                dir('kura') {
                    withMaven(jdk: 'temurin-jdk17-latest', maven: 'apache-maven-3.9.6') {
                        sh 'mvn -f kura/pom.xml -Dsurefire.rerunFailingTestsCount=3 clean install -Pcheck-exists-plugin -Dmaven.test.skip=true'
                    }
                }
            }
        }

        stage('Build Kura target-definition') {
            timeout(time: 2, unit: 'HOURS') {
                dir('kura') {
                    withMaven(jdk: 'temurin-jdk17-latest', maven: 'apache-maven-3.9.6') {
                        sh 'mvn -f kura/distrib/pom.xml clean install -Ptarget-definition -Dmaven.test.skip=true'
                    }
                }
            }
        }

        stage('Build Kura-apps') {
            timeout(time: 2, unit: 'HOURS') {
                dir('kura-apps') {
                    withMaven(jdk: 'temurin-jdk17-latest', maven: 'apache-maven-3.9.6') {
                        sh 'mvn clean install'
                    }
                }
            }
        }
    }
}
