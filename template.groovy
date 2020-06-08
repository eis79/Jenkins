def apiServers = '10.10.16.21,10.10.16.82' //set ip-addresses separated by comma
def curBranch = env.BRANCH //branch from job
def curProject = 'zzzzz' //set project name as string
def curGit = 'git@gitlab.xxx.company:orders-service/zzzzz.git' //set gitlab repository(ssh)
def JenkinsNode = 'PLD' //set jenkins node label on which project will build

pipeline {
    agent { label "${JenkinsNode}" }
        parameters {
            gitParameter(branch: '', branchFilter: '.*', defaultValue: 'master',
            name: 'BRANCH', quickFilterEnabled: false,
            selectedValue: 'TOP', sortMode: 'DESCENDING_SMART', tagFilter: '*',
            type: 'PT_TAG', useRepository: "${curGit}")
        }
    stages {
        stage('Build PHP-API') {
            when {
                expression { "${curBranch}" != 'undefined' } //build app if branch defined
            }
            steps {
                buildPhpApp("${curProject}", "${curBranch}", "${curGit}") //call function, send project name, branch, and git repository
            }
            post {
                success {
                    script {
                        echo "${curProject} build succeed, start push"
                        env.apiBuild = 'success' //set env variable for next stage to be started
                    }
                }
                failure {
                    echo "${curProject} build failed"
                }
            }
        }
        stage('Push PHP-API to repo') { //push to repository if build succeed
            when {
                expression { env.apiBuild == 'success' }
            }
            steps {
                pushToProdRepo("${curProject}")
            }
            post {
                success {
                    script {
                        echo "${curProject} push succeed, start deploy to ${apiServers}"
                        env.apiPush = 'success'
                    }
                }
                failure {
                    echo "${curProject} push failed"
                }
            }
        }
        stage('Deploy PHP-API to servers') { //deploy to server if push succeed
            when {
                expression { env.apiPush == 'success' }
            }
            steps {
                deployApp("${curProject}", "${apiServers}")
            }
            post {
                success {
                    echo "${curProject} successfully deployed to ${apiServers}"
                }
                failure {
                    echo "${curProject} deploy failed on ${apiServers}"
                }
            }
        }
    }
}
//build php application from git repository
def buildPhpApp(String project, String branch, String repo) {
    ws("${workspace}/$project/") {
        cleanWs()
        checkout poll: false, scm: [$class: 'GitSCM', branches: [[name: "$branch"]], doGenerateSubmoduleConfigurations: false, userRemoteConfigs: [[credentialsId: '14280e0d-f57d-43d4-9ca0-858b0ff158cf', url: "$repo"]]]
        sh "wget https://getcomposer.org/download/1.8.6/composer.phar && php composer.phar install"
        sh "git clone git@gitlab.xxx.company:devops/devops.git && mkdir -p ${project}/var/www/$project/ && mv devops/${project}/DEBIAN ${project}/DEBIAN && rm -rf devops"
        sh 'sudo find . -name ".git*" -print0 | xargs -0 rm -rf'
        sh 'cp -r $(ls | grep -v ' + "$project" + ') ' + "$project" + '/var/www/' + "$project" + '/'
        sh 'sed -i \'s/Version: 1/Version:' + "${BUILD_TIME}" + '/\' ' + "$project" + '/DEBIAN/control'
        sh 'mv ' + "$project" + ' ' + "$project" + '_' + "$BUILD_TIME" + ' && fakeroot dpkg-deb --build ' + "$project" + '_' + "$BUILD_TIME"
    }
}
//push deb package to repository
def pushToProdRepo(String project) {
    ws("${workspace}/$project/") {
        sh 'scp *.deb jenkins@repo.dev.xxx.com.ua:/opt/repo/prod-pld-ord'
        sh 'ssh jenkins@repo.dev.xxx.com.ua "cd /opt/repo/prod-pld-ord && dpkg-scanpackages -m . /dev/null | gzip -9c > Packages.gz"'
    }
}
//deploying deb package to servers separated by comma in loop
def deployApp(String project, String servers) {
    depl = servers.split(",")
        depl.each {
            sh 'ssh -o StrictHostKeyChecking=no -p 10022 ' + it + ' "sudo apt-get update && sudo apt-get install ' + "${project}=" + "${BUILD_TIME}" + '"'
            sh 'ssh -o StrictHostKeyChecking=no -p 10022 ' + it + ' "sudo /etc/init.d/supervisor restart"'
    }
}
