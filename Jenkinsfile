def label = "worker-${UUID.randomUUID().toString()}"

podTemplate(label: label, containers: [
 // containerTemplate(name: 'jnlp', image: 'customnamespace/jnlp-slave:latest', args: '${computer.jnlpmac} ${computer.name}'),
  //containerTemplate(name: 'gradle', image: 'gradle:4.5.1-jdk9', command: 'cat', ttyEnabled: true),
  containerTemplate(name: 'gradle', image: 'frekele/gradle', command: 'cat', ttyEnabled: true),
  containerTemplate(name: 'docker', image: 'docker', command: 'cat', ttyEnabled: true),
  containerTemplate(name: 'kubectl', image: 'lachlanevenson/k8s-kubectl:v1.8.8', command: 'cat', ttyEnabled: true),
  containerTemplate(name: 'helm', image: 'lachlanevenson/k8s-helm:latest', command: 'cat', ttyEnabled: true)
],
volumes: [
  hostPathVolume(mountPath: '/home/gradle/.gradle', hostPath: '/tmp/jenkins/.gradle'),
  hostPathVolume(mountPath: '/var/run/docker.sock', hostPath: '/var/run/docker.sock')
]) {
  node(label) {
    def myRepo = checkout scm
    def gitCommit = myRepo.GIT_COMMIT
    def gitBranch = myRepo.GIT_BRANCH
    def shortGitCommit = "${gitCommit[0..10]}"
    def previousGitCommit = sh(script: "git rev-parse ${gitCommit}~", returnStdout: true)
 
    stage('Build-Iiiiii') {
      container('gradle') {
          sh """
            pwd
            echo "GIT_BRANCH=${gitBranch}" >> /etc/environment
            echo "GIT_COMMIT=${gitCommit}" >> /etc/environment
            cd src/adservice
            pwd
            ./gradlew installDist
            """
      }
    }
    stage('Create Docker images') {
      container('docker') {
        withCredentials([[$class: 'UsernamePasswordMultiBinding',
          credentialsId: 'dockerhub',
          usernameVariable: 'wuhua988',
          passwordVariable: 'wang123456']]) {
          sh """
            pwd
            docker login -u wuhua988 -p wang123456 index.docker.io
            docker build -t wuhua988/my-image:${gitCommit} .
            docker push wuhua988/my-image:${gitCommit}
            """
        }
      }
    }
    stage('Run kubectl') {
      container('kubectl') {
        sh "kubectl get pods"
      }
    }
    stage('Run helm') {
      container('helm') {
        sh "helm list"
      }
    }
  }
}
