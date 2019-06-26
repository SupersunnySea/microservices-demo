// def label = "worker-${UUID.randomUUID().toString()}"

// podTemplate(label: label, containers: [
//  // containerTemplate(name: 'jnlp', image: 'customnamespace/jnlp-slave:latest', args: '${computer.jnlpmac} ${computer.name}'),
//   //containerTemplate(name: 'gradle', image: 'gradle:4.5.1-jdk9', command: 'cat', ttyEnabled: true),
//   containerTemplate(name: 'gradle', image: 'frekele/gradle', command: 'cat', ttyEnabled: true),
//   containerTemplate(name: 'docker', image: 'docker', privileged: true, command: 'cat', ttyEnabled: true),
//   containerTemplate(name: 'kubectl', image: 'lachlanevenson/k8s-kubectl:v1.8.8', command: 'cat', ttyEnabled: true),
//   containerTemplate(name: 'helm', image: 'lachlanevenson/k8s-helm:latest', command: 'cat', ttyEnabled: true)
// ],
// volumes: [
// //  hostPathVolume(mountPath: '/etc/resolv.conf', hostPath: '/etc/resolv.conf'),
//   hostPathVolume(mountPath: '/home/gradle/.gradle', hostPath: '/tmp/jenkins/.gradle'),
//   hostPathVolume(mountPath: '/var/run/docker.sock', hostPath: '/var/run/docker.sock')
// ]) {
//   node(label) {
//     def myRepo = checkout scm
//     def gitCommit = myRepo.GIT_COMMIT
//     def gitBranch = myRepo.GIT_BRANCH
//     def shortGitCommit = "${gitCommit[0..10]}"
//     def previousGitCommit = sh(script: "git rev-parse ${gitCommit}~", returnStdout: true)
 
//     stage('Build-Iiiiii') {
//       container('gradle') {
//           sh """
//             pwd
//             echo "GIT_BRANCH=${gitBranch}" >> /etc/environment
//             echo "GIT_COMMIT=${gitCommit}" >> /etc/environment
//             cd src/adservice
//             find /  -name "gax-bom-1.34.0.pom"
//             """
//       }
//     }
//     // stage('Runing containers') {
//     //   container('docker') {
//     //     // example to show you can run docker commands when you mount the socket
//     //      sh """
//     //        hostname
//     //        hostname -i
//     //        cat /etc/hosts
//     //        find / -name gax-bom-1.34.0.pom
//     //      """
//     //   }
//     // }
//     stage('Create Docker images') {
//       container('docker') {
//         withCredentials([usernamePassword(credentialsId: 'd94f2975-2889-4d5a-ba7c-a8ea596c5c07', passwordVariable: 'wang123456', usernameVariable: 'wuhua988')]) {
//           sh """
//             cd src/adservice
//             pwd

//             cat /etc/resolv.conf
//             docker login -u wuhua988 -p wang123456 index.docker.io
//             docker build -t wuhua988/my-image:${gitCommit} .
//             docker push wuhua988/my-image:${gitCommit}
//             """
//         }
//       }
//     }
//     stage('Run kubectl') {
//       container('kubectl') {
//         sh "kubectl get pods"
//       }
//     }
//     stage('Run helm') {
//       container('helm') {
//         sh "helm list"
//       }
//     }
//   }
// }


/**
 * This pipeline will run a Docker image build
 */

def label = "docker-${UUID.randomUUID().toString()}"

podTemplate(label: label, yaml: """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: docker
    image: docker:18.05-dind
    securityContext:
      privileged: true
    command: ['cat']
    tty: true
    volumeMounts:
    - name: dockersock
      mountPath: /var/run/docker.sock
  volumes:
  - name: dockersock
    hostPath:
      path: /var/run/docker.sock
"""
  ) {

  // def image = "jenkins/jnlp-slave"
  node(label) {
    def myRepo = checkout scm
    def gitCommit = myRepo.GIT_COMMIT
    stage('Build Docker image') {
      // git 'https://github.com/jenkinsci/docker-jnlp-slave.git'
      container('docker') {
        withCredentials([usernamePassword(credentialsId: 'd94f2975-2889-4d5a-ba7c-a8ea596c5c07', passwordVariable: 'wang123456', usernameVariable: 'wuhua988')]) {
          sh """
            cd src/adservice
            pwd

            cat /etc/resolv.conf
            docker login -u wuhua988 -p wang123456 index.docker.io
            docker build -t wuhua988/my-image:${gitCommit} .
            docker push wuhua988/my-image:${gitCommit}
            """
        }
      }
    }
  }
}
