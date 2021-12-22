pipeline {
    agent any
    
    environment {
        PROJECT = "emobility-echarging"
        PROJECT_FOLDER = "data-collectors/${PROJECT}"
        ARTIFACT_NAME = "dc-${PROJECT}-driwe"
        DOCKER_IMAGE = '755952719952.dkr.ecr.eu-west-1.amazonaws.com/dc-emobility-echarging'
        DOCKER_TAG = "test-$BUILD_NUMBER"
        DATACOLLECTORS_CLIENT_SECRET = credentials('keycloak-datacollectors-secret')
        AUTH_TOKEN = credentials('driwe_auth_token')
        JAVA_OPTIONS = "-Xms128m -Xmx512m"
    }

    stages {
        stage('Configure') {
            steps {
                sh """
                    cd ${PROJECT_FOLDER}
                    echo 'COMPOSE_PROJECT_NAME=${ARTIFACT_NAME}' > .env
                    echo 'DOCKER_IMAGE=${DOCKER_IMAGE}' >> .env
                    echo 'DOCKER_TAG=${DOCKER_TAG}' >> .env
                    echo 'LOG_LEVEL=debug' >> .env
                    echo 'ARTIFACT_NAME=${ARTIFACT_NAME}' >> .env
                    echo 'authorizationUri=https://auth.opendatahub.testingmachine.eu/auth' >> .env
                    echo 'tokenUri=https://auth.opendatahub.testingmachine.eu/auth/realms/noi/protocol/openid-connect/token' >> .env 
                    echo 'clientId=odh-mobility-datacollector' >> .env
                    echo 'clientName=odh-mobility-datacollector' >> .env
                    echo 'clientSecret=${DATACOLLECTORS_CLIENT_SECRET}' >> .env
                    echo 'scope=openid' >> .env 
                    echo -n 'provenance_version=' >> .env
                    xmlstarlet sel -N pom=http://maven.apache.org/POM/4.0.0 -t -v '/pom:project/pom:version' pom.xml >> .env
                    echo '' >> .env
                    echo 'provenance_name=${ARTIFACT_NAME}' >> .env 
                    echo 'BASE_URI=https://share.opendatahub.testingmachine.eu/json' >> .env
                    echo 'endpoint_host=www.driwe.club' >> .env
                    echo 'endpoint_port=443' >> .env
                    echo 'endpoint_ssl=yes' >> .env
                    echo 'endpoint_path=/api/stations/metadata?auth-token=${AUTH_TOKEN}' >> .env
                    echo 'app_callerId=NOI-Techpark' >> .env
                    echo 'app_dataOrigin=DRIWE' >> .env
                    echo 'app_period=600' >> .env
                    echo 'JAVA_OPTIONS=${JAVA_OPTIONS}' >> .env
                """
            }
        }
        stage('Test & Build') {
            steps {
                sh """
                    cd ${PROJECT_FOLDER}
                    aws ecr get-login --region eu-west-1 --no-include-email | bash
                    docker-compose --no-ansi -f infrastructure/docker-compose.build.yml build --pull
                    docker-compose --no-ansi -f infrastructure/docker-compose.build.yml push
                """
            }
        }
        stage('Deploy') {
            steps {
               sshagent(['jenkins-ssh-key']) {
                    sh """
                        (cd ${PROJECT_FOLDER}/infrastructure/ansible && ansible-galaxy install -f -r requirements.yml)
                        (cd ${PROJECT_FOLDER}/infrastructure/ansible && ansible-playbook --limit=test deploy.yml --extra-vars "release_name=${BUILD_NUMBER} project_name=${ARTIFACT_NAME}")
                    """
                }
            }
        }
    }
}
