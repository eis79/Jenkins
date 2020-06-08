def catalogApiServers = '****'
def mainApiServers = '****'
def frontServers = '****'
def checkoutApiServers = '****'
def productApiServers = '****'
def cabinetApiServers = '****'
def commonApiServers = '****'
pipeline {
    agent { label 'JenkinsPL' }
    stages {
        stage ("Build Applications") {
            parallel {
                stage('Build MAIN-API') {
                    when {
                        expression { env.MAIN_API_BRANCH != 'undefined' }
                    }
                    steps {
                            buildPhpApp('main-api', env.MAIN_API_BRANCH, 'git@gitlab-ce.local:xxx-front-api/main-page-api.git')
                    }
                    post {
                        success {
                            script {
                                env.mainApiBuild = 'success'
                            }
                        }
                    }
                }
                stage('Build Catalog-API') {
                    when {
                        expression { env.API_BRANCH != 'undefined' }
                    }
                    steps {
                            buildPhpApp('catalog-api', env.API_BRANCH, 'git@gitlab-ce.local:xxx-front-apis-xl/fronty.git')
                    }
                    post {
                        success {
                            script {
                                env.catalogApiBuild = 'success'
                            }
                        }
                    }
                }
                stage('Build Checkout-API') {
                    when {
                        expression { env.CHECKOUT_API_BRANCH != 'undefined' }
                    }
                    steps {
                            buildPhpApp('checkout-api', env.CHECKOUT_API_BRANCH, 'git@gitlab-ce.local:xxx-front-api/checkout-api.git')
                    }
                    post {
                        success {
                            script {
                                env.checkoutApiBuild = 'success'
                            }
                        }
                    }
                }
                stage('Build Product-API') {
                    when {
                        expression { env.PRODUCT_API_BRANCH != 'undefined' }
                    }
                    steps {
                            buildPhpApp('product-api', env.PRODUCT_API_BRANCH, 'git@gitlab-ce.local:xxx-front-api/cart-api.git')
                    }
                    post {
                        success {
                            script {
                                env.productApiBuild = 'success'
                            }
                        }
                    }
                }
                stage('Build Cabinet-API') {
                    when {
                        expression { env.CABINET_API_BRANCH != 'undefined' }
                    }
                    steps {
                            buildPhpApp('cabinet-api', env.CABINET_API_BRANCH, 'git@gitlab-ce.local:xxx-front-api/cabinet-api.git')
                    }
                    post {
                        success {
                            script {
                                env.cabinetApiBuild = 'success'
                            }
                        }
                    }
                }
                stage('Build COMMON-API') {
                    when {
                        expression { env.COMMON_API_BRANCH != 'undefined' }
                    }
                    steps {
                            buildPhpApp('common-api', env.COMMON_API_BRANCH, 'git@gitlab-ce.local:xxx-front-api/common-api.git')
                    }
                    post {
                        success {
                            script {
                                env.commonApiBuild = 'success'
                            }
                        }
                    }
                }
                stage('Build Front') {
                    when {
                        expression { env.FRONT_BRANCH != 'undefined' }
                    }
                    steps {
                        buildNodeJsApp('front', env.FRONT_BRANCH, 'git@gitlab-ce.local:xxx-front-xl/rz-skeleton.git')
                    }
                    post {
                        success {
                            script {
                                env.frontBuild = 'success'
                            }
                        }
                    }
                }
            }
        }
        stage('Push catalog-api to repo') {
            when {
                expression { env.catalogApiBuild == 'success' }
            }
            steps {
                pushToProdRepo('catalog-api')
            }
            post {
                success {
                    script {
                        env.catalogApiPush = 'success'
                    }
                }
            }
        }
        stage('Push main-api to repo') {
            when {
                expression { env.mainApiBuild == 'success' }
            }
            steps {
                pushToProdRepo('main-api')
            }
            post {
                success {
                    script {
                        env.mainApiPush = 'success'
                    }
                }
            }
        }
        stage('Push checkout-api to repo') {
            when {
                expression { env.checkoutApiBuild == 'success' }
            }
            steps {
                pushToProdRepo('checkout-api')
            }
            post {
                success {
                    script {
                        env.checkoutApiPush = 'success'
                    }
                }
            }
        }
        stage('Push product-api to repo') {
            when {
                expression { env.productApiBuild == 'success' }
            }
            steps {
                pushToProdRepo('product-api')
            }
            post {
                success {
                    script {
                        env.productApiPush = 'success'
                    }
                }
            }
        }
        stage('Push cabinet-api to repo') {
            when {
                expression { env.cabinetApiBuild == 'success' }
            }
            steps {
                pushToProdRepo('cabinet-api')
            }
            post {
                success {
                    script {
                        env.cabinetApiPush = 'success'
                    }
                }
            }
        }
        stage('Push common-api to repo') {
            when {
                expression { env.commonApiBuild == 'success' }
            }
            steps {
                pushToProdRepo('common-api')
            }
            post {
                success {
                    script {
                        env.commonApiPush = 'success'
                    }
                }
            }
        }
        stage('Push front to repo') {
            when {
                expression { env.frontBuild == 'success' }
            }
            steps {
                pushToProdRepo('front')
            }
            post {
                success {
                    script {
                        env.frontPush = 'success'
                    }
                }
            }
        }
        stage('Deploy MAIN-API') {
            when {
                expression { env.mainApiPush == 'success' }
            }
            steps {
                deployApp('main-api', "${mainApiServers}")
            }
        }
        stage('Deploy Catalog-API') {
            when {
                expression { env.catalogApiPush == 'success' }
            }
            steps {
                deployApp('catalog-api', catalogApiServers)
            }
        }
        stage('Deploy CHECKOUT-API') {
            when {
                expression { env.checkoutApiPush == 'success' }
            }
            steps {
                deployApp('checkout-api', "${checkoutApiServers}")
            }
        }
        stage('Deploy PRODUCT-API') {
            when {
                expression { env.productApiPush == 'success' }
            }
            steps {
                deployApp('product-api', "${productApiServers}")
            }
        }
        stage('Deploy CABINET-API') {
            when {
                expression { env.cabinetApiPush == 'success' }
            }
            steps {
                deployApp('cabinet-api', "${cabinetApiServers}")
            }
        }
        stage('Deploy COMMON-API') {
            when {
                expression { env.commonApiPush == 'success' }
            }
            steps {
                deployApp('common-api', "${commonApiServers}")
            }
        }
        stage('Deploy Front') {
            when {
                expression { env.frontPush == 'success' }
            }
            steps {
                deployApp('front', frontServers)
            }
        }
    }
}

def buildPhpApp(String project, String branch, String repo) {
    ws("${workspace}/$project/") {
        cleanWs()
        checkout poll: false, scm: [$class: 'GitSCM', branches: [[name: "$branch"]], doGenerateSubmoduleConfigurations: false, userRemoteConfigs: [[credentialsId: '14280e0d-f57d-43d4-9ca0-858b0ff158cf', url: "$repo"]]]
        sh "wget https://getcomposer.org/download/1.5.1/composer.phar && php composer.phar install"
        sh "git clone git@gitlab-ce.local:devops/devops.git && mkdir -p ${project}/var/www/$project/ && mv devops/${project}/DEBIAN ${project}/DEBIAN && rm -rf devops"
        sh 'sudo find . -name ".git*" -print0 | xargs -0 rm -rf'
        sh 'cp -r $(ls | grep -v ' + "$project" + ') ' + "$project" + '/var/www/' + "$project" + '/'
        sh 'sed -i \'s/Package: ' + "${project}" + '/Package: ' + "${project}" + '/\' ' + "${project}" + '/DEBIAN/control'
        sh 'sed -i \'s/Version: 1/Version:' + "${BUILD_TIME}" + '/\' ' + "$project" + '/DEBIAN/control'
        sh 'mv ' + "$project" + ' ' + "$project" + '_' + "$BUILD_TIME" + ' && fakeroot dpkg-deb --build ' + "$project" + '_' + "$BUILD_TIME"
    }
}

def buildNodeJsApp(String project, String branch, String repo) {
    ws("${workspace}/$project/") {
        cleanWs()
        checkout poll: false, scm: [$class: 'GitSCM', branches: [[name: "$branch"]], doGenerateSubmoduleConfigurations: false, userRemoteConfigs: [[credentialsId: '14280e0d-f57d-43d4-9ca0-858b0ff158cf', url: "$repo"]]]
        sh 'sed -i "s/rg.ssr.dev.xxx/rg.xxx/" src/environments/environment.prod.ts'
        sh 'sed -i "s/ssr.dev.xxx/xxx/g" src/environments/environment.prod.ts'
        sh 'sed -i "s/catalog-api-dev.xxx/xl-catalog-api-pp.xxx/" src/environments/environment.prod.ts'
        sh 'sed -i "s/main-page-api-dev.xxx/xl-main-api-pp.xxx/" src/environments/environment.prod.ts'
        sh 'sed -i "s/product-api-dev.xxx/product-api-pp.xxx/" src/environments/environment.prod.ts'
        sh 'sed -i "s/checkout-api-dev.xxx/checkout-api-pp.xxx/" src/environments/environment.prod.ts'
        sh 'sed -i "s/cabinet-api-dev.xxx/cabinet-api-pp.xxx/" src/environments/environment.prod.ts'
        sh 'sed -i "s/common-api.xxx/common-api-pp.xxx/g" src/environments/environment.prod.ts'
        sh 'sed -i "s/rz-static-dev.xxx/xl-preprod-static.xxx/g" src/environments/environment.prod.ts'
        sh 'sed -i "s/rz-static-dev.xxx/xl-preprod-static.xxx/" angular.json'
//        sh 'sed -i "s/rz-static-dev.xxx/preprod-static.xxx/" .angular-cli.json'
        sh 'npm ci'
        sh 'npm run generate-tsconfig && npm run clean && npm run modules:update && npm run webpack:styles && npm run build:ssr'
        sh "git clone git@gitlab-ce.local:devops/devops.git"
        sh 'cp -r devops/catalog/front . && mkdir -p front/var/www/front'
        sh 'cp -r dist front/var/www/front/'
        sh 'sed -i "s/Package: fronty/Package: ' + "${project}" + '/" front/DEBIAN/control'
        sh 'sed -i "s/Version: 1/Version: ${BUILD_TIME}/" front/DEBIAN/control'
        sh 'mv front ' + "${project}" + '_$BUILD_TIME'
        sh 'fakeroot dpkg-deb --build ' + "${project}" + '_$BUILD_TIME'
    }
}

def pushToProdRepo(String project) {
    ws("${workspace}/$project/") {
        sh 'scp *.deb jenkins@repo.dev.xxx:/opt/repo/prod-pld'
        sh 'ssh jenkins@repo.dev.xxx "cd /opt/repo/prod-pld && dpkg-scanpackages -m . /dev/null | gzip -9c > Packages.gz"'
    }
}

def deployApp(String project, String servers) {
    depl = servers.split(",")
        depl.each {
            sh 'ssh -o StrictHostKeyChecking=no -p 10022 ' + it + ' "sudo apt-get update && sudo apt-get install ' + "${project}=" + "${BUILD_TIME}" + '"'
            if ("${project}" == 'front') {
                sh 'ssh -o StrictHostKeyChecking=no -p 10022 ' + it + ' "sudo /etc/init.d/rzk-front restart"'
            }
        }
}
