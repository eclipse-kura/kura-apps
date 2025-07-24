node {
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
    }

    stage('Build Kura-apps') {
        def mavenBuildType = 'deploy'
        if (!env.BRANCH_IS_PRIMARY) {
            echo 'Skipping deploy for non-main branch'
            mavenBuildType = 'install'
        }

        timeout(time: 2, unit: 'HOURS') {
            dir('kura-apps') {
                withMaven(jdk: 'temurin-jdk17-latest', maven: 'apache-maven-3.9.6', options: [artifactsPublisher(disabled: true)]) {
                    sh "mvn clean ${mavenBuildType}"
                }
            }
        }
    }

    stage('Sonar') {
        timeout(time: 2, unit: 'HOURS') {
            dir("kura-apps") {
                withMaven(jdk: 'temurin-jdk17-latest', maven: 'apache-maven-3.9.6', options: [artifactsPublisher(disabled: true)]) {
                    withCredentials([string(credentialsId: 'sonarcloud-token-kura-apps', variable: 'SONARCLOUD_TOKEN')]) {
                        withSonarQubeEnv {
                            sh '''
                                mvn sonar:sonar \
                                    -Dmaven.test.failure.ignore=true \
                                    -Dsonar.organization=eclipse-kura \
                                    -Dsonar.host.url=${SONAR_HOST_URL} \
                                    -Dsonar.token=${SONARCLOUD_TOKEN} \
                                    -Dsonar.pullrequest.branch=${CHANGE_BRANCH} \
                                    -Dsonar.pullrequest.base=${CHANGE_TARGET} \
                                    -Dsonar.pullrequest.key=${CHANGE_ID}\
                                    -Dsonar.java.binaries='target/' \
                                    -Dsonar.core.codeCoveragePlugin=jacoco \
                                    -Dsonar.projectKey=eclipse-kura_kura-apps \
                                    -Dsonar.exclusions=tests/**/*.java
                            '''
                        }
                    }
                }
            }
        }
    }
}

// No need to occupy a node
stage('quality-gate') {
    // Sonar quality gate
    timeout(time: 30, unit: 'MINUTES') {
        withCredentials([string(credentialsId: 'sonarcloud-token-kura-apps', variable: 'SONARCLOUD_TOKEN')]) {
            def qg = waitForQualityGate()
            if (qg.status != 'OK') {
                error "Pipeline aborted due to sonar quality gate failure: ${qg.status}"
            }
        }
    }
}
