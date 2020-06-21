pipiline {
    agent{node('master')}
    stages {
        stage('Clear workspace, fetch repository') {
            steps {
                script {
                    cleanWs()
                    withCredentials([
                                    usernamePassword(credentialsId: 'srv_sudo',
                                    usernameVariable: 'username',
                                    passwordVariable: 'password')
                    ]) {
                        try {
                            sh "echo '${password}' | sudo -S docker stop ys_image"
                            sh "echo '${password}' | sudo -S docker container rm ys_image"
                        } catch (Exception e) {
                            print 'No container for ys_image'
                        }
                    }
                }
                script {
                    echo 'Start download project'
                    checkout([$class                           : 'GitSCM',
                              branches                         : [[name: '*/master']],
                              doGenerateSubmoduleConfigurations: false,
                              extensions                       : [[$class           : 'RelativeTargetDirectory',
                                                                   relativeTargetDir: 'auto']],
                              submoduleCfg                     : [],
                              userRemoteConfigs                : [[credentialsId: 'SergeyYakuninGit', url: 'https://github.com/Sambro2105/devopshwrepo.git']]])
                }
            }
        }
        stage('Build docker image') {
            steps {
                script {
                    withCredentials([
                            usernamePassword(credentialsId: 'srv_sudo',
                                    usernameVariable: 'username',
                                    passwordVariable: 'password')
                    ]) {
                        sh "echo '${password}' | sudo -S docker build ${WORKSPACE}/auto -t ys_nginx"
                    }
                }
            }
        }
        stage('Run docker image') {
            steps {
                script {
                    withCredentials([
                            usernamePassword(credentialsId: 'srv_sudo',
                                    usernameVariable: 'username',
                                    passwordVariable: 'password')
                    ]) {
                        sh "echo '${password}' | sudo -S docker run -d -p 7569:80 --name ys_image -v /home/adminci/is_mount_dir:/stat ys_nginx"
                    }
                }
            }
        }
        stage('Write statistics to files') {
            steps {
                script {
                    withCredentials([
                            usernamePassword(credentialsId: 'srv_sudo',
                                    usernameVariable: 'username',
                                    passwordVariable: 'password')
                    ]) {
                        sh "echo '${password}' | sudo -S docker exec -t ys_image bash -c 'df -h > /stat/stats.txt'"
                        sh "echo '${password}' | sudo -S docker exec -t ys_image bash -c 'top -n 1 -b >> /stat/stats.txt'"
                    }
                }
            }
        }
    }
}