#!/usr/bin/env groovy

node {

  stage('Checkout') {
    checkout scm
  }

  stage('Build') {
    docker.image('zenoss/build-tools:0.0.10').inside() { 
      withMaven() {
        sh "mvn -f java package"
      }
    }
  }
  stage('Publish') {
/*
    def remote = [:]
    withFolderProperties {
      withCredentials( [sshUserPrivateKey(credentialsId: 'PUBLISH_SSH_KEY', keyFileVariable: 'identity', passphraseVariable: '', usernameVariable: 'userName')] ) {
        remote.name = env.PUBLISH_SSH_HOST
        remote.host = env.PUBLISH_SSH_HOST
        remote.user = userName
        remote.identityFile = identity
        remote.allowAnyHosts = true

        def whl_ver = sh( returnStdout: true, script: "awk '/^VERSION/{print \$3}' makefile" ).trim()
        sshPut remote: remote, from: 'dist/pynetsnmp-' + whl_ver + '-py2-none-any.whl', into: env.PUBLISH_SSH_DIR
      }
    }
*/
  }
}
