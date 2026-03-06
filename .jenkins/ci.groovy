@Library('add-ons-shared-libs@plugin/apps') _

node {
    continuousIntegrationPipeline(
        buildType: "deploy",
        sonar: [
            enable: true,
            projectKey: "eclipse-kura_kura-apps",
            tokenId: "sonarcloud-token-kura-apps",
            exclusions: "kura-examples/tests/**/*,kura-addon-prototypes/tests/**/***/*.xml,**/*.yml",
            testExclusions: "**/*"
        ],
    )
}
