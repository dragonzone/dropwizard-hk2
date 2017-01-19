#!Jenkinsfile

// Project Config
def buildEnvironmentImage = "maven:3.3.9-jdk-8"
def buildableBranchRegex = ".*" // ( PRs are in the form 'PR-\d+' )
def deployableBranchRegex = "master"

// Maven Config
def mavenArgs = "-B -U -Dci=true"
def mavenValidateProjectGoals = "clean initialize"
def mavenNonDeployGoals = "verify gpg:sign"
def mavenDeployGoals = "install gpg:sign deploy:deploy -DdeployAtEnd=true -DupdateReleaseInfo=true"
def requireTests = false
def globalMavenSettingsConfig = "maven-dragonZone"

// Exit if we shouldn't be building
if (!env.BRANCH_NAME.matches(buildableBranchRegex)) {
    echo "Branch ${env.BRANCH_NAME} is not buildable, aborting."
    return
}

// Pipeline Definition
node("docker") {
    // Prepare the docker image to be used as a build environment
    def buildEnv = docker.image(buildEnvironmentImage)
    def isDeployableBranch = env.BRANCH_NAME.matches(deployableBranchRegex)

    stage("Prepare Build Environment") {
        buildEnv.pull()
    }

    buildEnv.inside {
        withEnv(["HOME=${env.WORKSPACE}"]) {
            withMaven(globalMavenSettingsConfig: globalMavenSettingsConfig) {
                /*
                 * Clone the repository and make sure that the pom.xml file is structurally valid and has a GAV
                 */
                stage("Checkout & Initialize Project") {
                    checkout scm
                    sh "git reset --hard && git clean -f"
                    sh "mvn ${mavenArgs} ${mavenValidateProjectGoals}"
                }

                // Get Git Information
                def gitUrl = sh(returnStdout: true, script: 'git remote show origin').trim()
                def gitSha1 = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
                def gitAuthor = "${env.CHANGE_AUTHOR ? env.CHANGE_AUTHOR : sh(returnStdout: true, script: 'git log -1 --format="%aN" HEAD').trim()}"
                def gitAuthorEmail = "${env.CHANGE_AUTHOR_EMAIL ? env.CHANGE_AUTHOR_EMAIL : sh(returnStdout: true, script: 'git log -1 --format="%aE" HEAD').trim()}"
                sh "git config --global user.name ${gitAuthor}"
                sh "git config --global user.email ${gitAuthorEmail}"
                def gitInfo = (gitUrl =~ '.*/([^/]+)/([^/]+).git')[0]
                def gitOrg = gitInfo[1]
                def gitRepo = gitInfo[2]

                // Set Build Information
                def pom = readMavenPom(file: "pom.xml")
                def artifactId = pom.artifactId
                def versionWithBuild = pom.version.replace("-SNAPSHOT", ".${env.BUILD_NUMBER}")
                def version = "${versionWithBuild}-${gitSha1.take(6)}"
                def tag = "${artifactId}-${isDeployableBranch ? versionWithBuild : version}"
                currentBuild.displayName = "${artifactId}-${version}"
                currentBuild.description = gitAuthor

                /*
                 * Use the maven-release-plugin to verify that the pom is ready for release (no snapshots) and update the
                 * version. We don't push changes here, because we will push the tag after the build if it succeeds. We
                 * also set the preparationGoals to initialize so that we don't do a build here, just pom updates.
                 */
                stage("Validate Project") {
                    sh "mvn ${mavenArgs} release:prepare -Dresume=false -Darguments=\"${mavenArgs}\" -DpushChanges=false -DpreparationGoals=initialize -Dtag=${tag} -DreleaseVersion=${version} -DdevelopmentVersion=${pom.version}"
                }

                // Actually build the project
                stage("Build Project") {
                    try {
                        withCredentials([string(credentialsId: 'gpg-keyname', variable: 'GPG_KEYNAME'), file(credentialsId: 'gpg-secring', variable: 'GPG_SECRING'), file(credentialsId: 'gpg-pubring', variable: 'GPG_PUBRING')]) {
                            sh "mvn ${mavenArgs} release:perform -DlocalCheckout=true -Dgoals=\"${isDeployableBranch ? mavenDeployGoals : mavenNonDeployGoals}\" -Darguments=\"${mavenArgs} -Dgpg.defaultKeyring=false -Dgpg.keyname=$GPG_KEYNAME -Dgpg.publicKeyring=$GPG_PUBRING -Dgpg.secretKeyring=$GPG_SECRING\""
                        }
                        archiveArtifacts 'target/checkout/**/pom.xml'

                        if (isDeployableBranch) {
                            try {
                                withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: scm.userRemoteConfigs[0].credentialsId, usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD']]) {
                                    sh("git config credential.username ${env.GIT_USERNAME}")
                                    sh("git config credential.helper '!echo password=\$GIT_PASSWORD; echo'")
                                    sh("GIT_ASKPASS=true git push origin ${tag}")
                                }
                            } finally {
                                sh("git config --unset credential.username")
                                sh("git config --unset credential.helper")
                            }
                        }
                    } finally {
                        junit allowEmptyResults: !requireTests, testResults: "target/checkout/**/target/surefire-reports/TEST-*.xml"
                    }
                }
                if (isDeployableBranch) {
                    stage("Stage to Maven Central") {
                        try {
                            sh "cd target/checkout && mvn ${mavenArgs} nexus-staging:deploy-staged"

                            input message: 'Publish to Central?', ok: 'Publish'

                            sh "cd target/checkout && mvn ${mavenArgs} nexus-staging:release"
                        } catch (err) {
                            sh "cd target/checkout && mvn ${mavenArgs} nexus-staging:drop"
                            throw err
                        }
                    }
                }
            }
        }
    }
}
