def label = "worker-${UUID.randomUUID().toString()}"

podTemplate(label: label, containers: [
 // containerTemplate(name: 'jnlp', image: 'customnamespace/jnlp-slave:latest', args: '${computer.jnlpmac} ${computer.name}'),
  //containerTemplate(name: 'gradle', image: 'gradle:4.5.1-jdk9', command: 'cat', ttyEnabled: true),
  containerTemplate(name: 'gradle', image: 'frekele/gradle', command: 'cat', ttyEnabled: true),
  containerTemplate(name: 'docker', image: 'docker', privileged: true, command: 'cat', ttyEnabled: true),
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
 
    stage('Build') {
      container('gradle') {
          sh """
            pwd
            echo "GIT_BRANCH=${gitBranch}" >> /etc/environment
            echo "GIT_COMMIT=${gitCommit}" >> /etc/environment
            cd src/adservice
            """
      }
    }
    // stage('Runing containers') {
    //   container('docker') {
    //     // example to show you can run docker commands when you mount the socket
    //      sh """
    //        hostname
    //        hostname -i
    //        cat /etc/hosts
    //        find / -name gax-bom-1.34.0.pom
    //      """
    //   }
    // }
    stage('Create Docker images') {
      container('docker') {
        //configure registry
        docker.withRegistry('https://475762907367.dkr.ecr.ap-southeast-1.amazonaws.com', 'ecr:ap-southeast-1:my_ecr_id') {
            sh """    
            cd src/adservice
            docker build --network=host -t 475762907367.dkr.ecr.ap-southeast-1.amazonaws.com/adservice:${gitCommit} .
            docker images

            docker push 475762907367.dkr.ecr.ap-southeast-1.amazonaws.com/adservice:${gitCommit}
            """
        }
      }
    }
    // stage('Run kubectl') {
    //   container('kubectl') {
    //     sh "kubectl get pods"
    //   }
    // }
    // stage('Run helm') {
    //   container('helm') {
    //     sh "helm list"
    //   }
    // }
  }
}
