podTemplate(inheritFrom: 'basic', yaml: '''
spec:
  containers:
  - name: "jnlp"
    resources:
      limits:
        cpu: "2000m"
        memory: "5Gi"
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
            dir('kura') {
                kura_branch = 'develop'
                git branch: "${kura_branch}", changelog: false, poll: false, url: 'https://github.com/eclipse-kura/kura.git'
            }

            dir('kura-apps') {
                kura_apps_branch = sh returnStdout: true, script: 'source ${WORKSPACE}/esf/build-kura.env && echo ${KURA_APPS_BRANCH}'
                kura_apps_branch = kura_apps_branch.trim()
                git branch: "${kura_apps_branch}", changelog: false, poll: false, url: 'https://github.com/eclipse-kura/kura-apps.git'
            }
        }

        stage('build-kura-target-definition') {
            timeout(time: 2, unit: 'HOURS') {
                dir('kura') {
                    withMaven(jdk: 'jdk-17.0.9', maven: 'Maven 3.9 (latest)', mavenLocalRepo: '${WORKSPACE}/.repository',
                mavenOpts: '-Dsettings.security=${WORKSPACE}/.repository/settings-security.xml',
                mavenSettingsConfig: '59e91eef-b32e-4095-afca-bb07841fb26c') {
                        configFileProvider([
                        configFile(fileId: '758a8482-2c10-4417-b4ea-9663e870e0a7',
                        targetLocation: "${WORKSPACE}/.repository/settings-security.xml")]) {
                            sh 'mvn -f kura/distrib/pom.xml clean install -Ptarget-definition -Dmaven.test.skip=true'
                        }
                }
                }
            }
        }

        stage('build-kura-apps') {
            timeout(time: 2, unit: 'HOURS') {
                dir('kura-apps') {
                    withMaven(jdk: 'jdk-17.0.9', maven: 'Maven 3.9 (latest)', mavenLocalRepo: '${WORKSPACE}/.repository',
                mavenOpts: '-Dsettings.security=${WORKSPACE}/.repository/settings-security.xml',
                mavenSettingsConfig: '59e91eef-b32e-4095-afca-bb07841fb26c') {
                        configFileProvider([
                        configFile(fileId: '758a8482-2c10-4417-b4ea-9663e870e0a7',
                        targetLocation: "${WORKSPACE}/.repository/settings-security.xml")]) {
                            sh 'mvn clean install -Dmaven.test.skip=true'
                        }
                }
                }
            }
        }
    }
}
