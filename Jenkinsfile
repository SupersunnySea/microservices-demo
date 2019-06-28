def label = "worker-${UUID.randomUUID().toString()}"

podTemplate(label: label, containers: [
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

    stage('Unit Test') {
      container('gradle') {
          sh """
            pwd
            echo "GIT_BRANCH=${gitBranch}" >> /etc/environment
            echo "GIT_COMMIT=${gitCommit}" >> /etc/environment
            cd src/adservice
            ./gradlew installDist
            ./gradlew test
            """
      }
    }

    stage('Code coverage') {
      container('gradle') {
          sh """
            pwd
            echo "GIT_BRANCH=${gitBranch}" >> /etc/environment
            echo "GIT_COMMIT=${gitCommit}" >> /etc/environment
            cd src/adservice
            ./gradlew jacocoTestReport

             publishHTML (target: [
                                     reportDir: 'build/reports/jacoco/test/html/',
                                     reportFiles: 'index.html',
                                     reportName: "JaCoCo Report"
                                 ])



            """
            publishHTML([allowMissing: false, alwaysLinkToLastBuild: false, keepAll: true, reportDir: 'build/reports/jacoco/test/html', reportFiles: 'index.html', reportName: 'ADService HTML Report', reportTitles: 'tittle'])

      }
    }


    stage('Create Docker Images') {
      container('docker') {
        //confi
        docker.withRegistry('https://475762907367.dkr.ecr.ap-southeast-1.amazonaws.com', 'ecr:ap-southeast-1:my_ecr_id') {
            sh """    
            cd src/adservice
            docker build --network=host -t 475762907367.dkr.ecr.ap-southeast-1.amazonaws.com/adservice:${gitCommit} .
            docker push 475762907367.dkr.ecr.ap-southeast-1.amazonaws.com/adservice:${gitCommit}
            """
        }
      }
    }
  }
}