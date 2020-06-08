pipeline {
    agent { label 'SPLITD' }
    options {
        ansiColor 'xterm'
        buildDiscarder(logRotator(daysToKeepStr: '14', numToKeepStr: '14'))
    }
    environment { VERSION = "${BUILD_TIME}" }
    parameters {
        gitParameter name: 'BRANCH_SPLITTER',
                    branch: '', 
                    branchFilter: 'origin/(.*)', 
                    defaultValue: 'master', 
                    description: '',  
                    quickFilterEnabled: false, 
                    selectedValue: 'NONE', 
                    sortMode: 'ASCENDING', 
                    tagFilter: '*', 
                    type: 'PT_BRANCH',
                    useRepository: 'git@gitlab-ce.local:splitter-team/splitter.git'
        booleanParam name: 'USE_BRANCH_BENDER_PROMO', defaultValue: false, description: 'Do you need Bender plugin?'
        booleanParam name: 'COMPOSER_UPDATE', defaultValue: false, description: 'Do you need composer update?'
        booleanParam name: 'RESTART', defaultValue: false, description: 'Do you need restart container?'

        gitParameter name: 'BRANCH_BENDER_PROMO',
                    branch: '', 
                    branchFilter: 'origin/(.*)', 
                    defaultValue: 'master', 
                    description: 'composer require splitter-modules/bender.promo',  
                    listSize: '5',
                    quickFilterEnabled: true,
                    selectedValue: 'NONE', 
                    sortMode: 'DESCENDING', 
                    tagFilter: '*', 
                    type: 'PT_BRANCH',
                    useRepository: 'git@gitlab-ce.local:splitter-modules/bender.promo.git' 

    }
    stages {
        stage('Clone Repository') { 
            steps {
                cleanWs()
                git branch: "${params.BRANCH_BENDER_PROMO}", url: 'git@gitlab-ce.local:splitter-modules/bender.promo.git', credentialsId: '14280e0d-f57d-43d4-9ca0-858b0ff158cf'
                sh "find . -name \".git*\" -print0 | xargs -0 rm -rf"                
                git branch: "${params.BRANCH_SPLITTER}", url: 'git@gitlab-ce.local:splitter-team/splitter.git', credentialsId: '14280e0d-f57d-43d4-9ca0-858b0ff158cf'
            }
        }
        stage('Update repo') { 
            steps {
                sh '''
                ssh -o StrictHostKeyChecking=no -p 10022 $TARGET_HOST "cd /var/www/splitter-docker&&git pull"
                
                
                
                '''
            }
        }
        stage('Restart container') {
            when {
                expression { params.RESTART == true }
            } 
            steps {
                sh '''
                ssh -o StrictHostKeyChecking=no -p 10022 $TARGET_HOST "cd /var/www/splitter-docker/&& bash docker.sh -t restart"

                '''
            }
        }
        
        stage('Composer Requrie') {
            when {
                expression { params.USE_BRANCH_BENDER_PROMO == true }
                expression { params.COMPOSER_UPDATE != true}
            } 
            steps {
                sh '''
                ssh -p 10022 $TARGET_HOST "docker exec  splitter-php bash -c 'cd /var/www/splitter&&git reset --hard&&git checkout ${BRANCH_SPLITTER}&&git pull&&composer require splitter-modules/bender.promo:'dev-${BRANCH_BENDER_PROMO} as dev-master'&&composer deploy'"
                ssh -p 10022 $TARGET_HOST "docker exec  splitter-php bash -c 'cd /var/www/splitter&&./yii migrate --interactive=0'"
                '''
            }
        }
        stage('Composer Requrie and Update') {
            when {
                expression { params.USE_BRANCH_BENDER_PROMO == true }
                expression { params.COMPOSER_UPDATE == true}
            } 
            steps {
                sh '''
                ssh -p 10022 $TARGET_HOST "docker exec  splitter-php bash -c 'cd /var/www/splitter&&git reset --hard&&git checkout ${BRANCH_SPLITTER}&&git pull&&composer require splitter-modules/bender.promo:'dev-${BRANCH_BENDER_PROMO} as dev-master'&&php composer.phar update splitter-modules/bender.* --with-dependencies&&composer deploy'"
                ssh -p 10022 $TARGET_HOST "docker exec  splitter-php bash -c 'cd /var/www/splitter&&./yii migrate --interactive=0'"
                '''
            }
        }
    
    }
}
