def curBranch = env.BRANCH //branch from job
def curProject = 'api' //set project name as string
def curGit = 'git@gitlab.zzzzz.company:bender/promo-api.git' //set gitlab repository(ssh)
def JenkinsNode = 'xxxxx' //set jenkins node label on which project will build
pipeline {
    agent { label "${JenkinsNode}" }
    options {
        ansiColor 'xterm'
        buildDiscarder(logRotator(daysToKeepStr: '7', numToKeepStr: '7'))
    }
        parameters {
            gitParameter(branch: '', branchFilter: '.*', defaultValue: 'master',
            name: 'BRANCH', quickFilterEnabled: true,
            selectedValue: 'TOP', sortMode: 'DESCENDING_SMART', tagFilter: '*',
            type: 'PT_BRANCH', useRepository: "${curGit}")
            booleanParam name: 'Release', defaultValue: true, description: 'Do you need new release?'
            booleanParam name: 'Rollback', defaultValue: false, description: 'Rollback to old release'
        }
    stages {
        stage('Build PHP-API') {
            when {
                expression { "${curBranch}" != 'undefined' } //build app if branch defined
                expression { params.Release == true }
                expression { params.Rollback == false }
                
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
        
    stage('Deploy PHP-API ') { //push to repository if build succeed
            when {
                expression { env.apiBuild == 'success' }
                
                
            }
            steps {
                deployToProd("${curProject}")
            }
            post {
                success {
                    script {
                        echo "${curProject} deploy success"
                        env.apiDeploy = 'success'
                    }
                }
                failure {
                    echo "${curProject} deploy failed"
                }
            }
        }    
    stage('Rollback') { //push to repository if build succeed
            when {
                expression { params.Release != true }
                expression { params.Rollback == true}
            }
            steps {
                Rollback("${curProject}", "${curBranch}", "${curGit}")
            }
            post {
                success {
                    script {
                        echo "${curProject} rollback succeed"
                        env.apiPush = 'success'
                    }
                }
                failure {
                    echo "${curProject} rollback failed"
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
       sh 'composer  require deployer/recipes --dev'
       sh "dep build preprod  -vvv"
        
    }
}

def Rollback(String project, String branch, String repo) {
    ws("${workspace}/$project/") {
        checkout poll: false, scm: [$class: 'GitSCM', branches: [[name: "$branch"]], doGenerateSubmoduleConfigurations: false, userRemoteConfigs: [[credentialsId: '14280e0d-f57d-43d4-9ca0-858b0ff158cf', url: "$repo"]]]
        sh 'dep rollback preprod -p -vvv'
       
    }
}

def deployToProd(String project) {
    ws("${workspace}/$project/") {
        sh "dep deploy preprod  -p -vvv"
        
    }
}
