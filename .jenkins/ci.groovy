@Library('add-ons-shared-libs@develop') _

node {
    continuousIntegrationPipeline(
        buildType: "deploy",
        sonar: [
            enable: true,
            projectKey: "eclipse-kura_kura-apps",
            tokenId: "sonarcloud-token-kura-apps",
            exclusions: "tests/**/*,**/*.xml,**/*.yml",
            testExclusions: "**/*"
        ],
    )
}
