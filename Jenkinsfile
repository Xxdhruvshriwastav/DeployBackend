pipeline {
    agent any

    environment {
        // ── Docker Hub / Registry ────────────────────────────────────────────────
        DOCKER_REGISTRY       = 'docker.io'
        DOCKER_CREDENTIALS_ID = 'docker-hub-credentials'   // Jenkins credential ID
        DOCKER_IMAGE_PREFIX   = 'hireconnect'

        // ── Versioning ──────────────────────────────────────────────────────────
        // BRANCH_NAME is only populated in Multibranch pipelines.
        // For regular pipeline jobs fall back to GIT_BRANCH, then 'main'.
        IMAGE_TAG = "${(env.BRANCH_NAME ?: env.GIT_BRANCH ?: 'main').replaceAll('origin/', '')}-${env.BUILD_NUMBER}"

        // ── Maven ────────────────────────────────────────────────────────────────
        MAVEN_OPTS = '-XX:+TieredCompilation -XX:TieredStopAtLevel=1'
    }

    tools {
        maven 'Maven-3.9'       // Must match the name in Jenkins → Global Tool Configuration
        jdk   'JDK-21'          // Must match the name in Jenkins → Global Tool Configuration
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 60, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    stages {

        // ── 1. Checkout ──────────────────────────────────────────────────────────
        stage('Checkout') {
            steps {
                echo '📥  Checking out source code…'
                checkout scm
            }
        }

        // ── 2. Check Docker availability ─────────────────────────────────────────
        // Sets DOCKER_AVAILABLE=true/false so downstream stages can skip gracefully.
        stage('Check Docker') {
            steps {
                script {
                    def dockerCheck = sh(
                        script: 'which docker > /dev/null 2>&1 && echo "true" || echo "false"',
                        returnStdout: true
                    ).trim()
                    env.DOCKER_AVAILABLE = dockerCheck
                    if (dockerCheck == 'true') {
                        echo '✅  Docker is available — image build & push stages will run.'
                    } else {
                        echo '⚠️  Docker not found on this agent — Docker stages will be SKIPPED.'
                        echo '    To enable Docker: mount the Docker socket into the Jenkins container.'
                        echo '    See: https://www.jenkins.io/doc/book/installing/docker/'
                    }
                }
            }
        }

        // ── 3. Build + Test all services ────────────────────────────────────────
        stage('Build & Test Microservices') {
            matrix {
                axes {
                    axis {
                        name    'SERVICE'
                        values  'eureka-server',
                                'api-gateway',
                                'auth-service',
                                'profile-service',
                                'job-service',
                                'application-service',
                                'notification-service',
                                'payment-service',
                                'subscription-service',
                                'interview-service',
                                'analytics-service'
                    }
                }
                stages {
                    stage('Maven Build') {
                        steps {
                            echo "🏗️  Building ${SERVICE}…"
                            dir("${SERVICE}") {
                                // catchError: a test failure marks this branch FAILURE and
                                // overall build UNSTABLE — but does NOT stop Docker stages.
                                catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                                    // -DskipITs skips Spring Boot integration tests that need
                                    // a live DB/Eureka — unit tests (Mockito) still run.
                                    sh 'mvn clean package -B -DskipITs'
                                }
                            }
                        }
                        post {
                            always {
                                // Path is relative to workspace root; dir() does NOT
                                // affect junit — so we must include SERVICE in the glob.
                                junit allowEmptyResults: true,
                                      testResults: "${SERVICE}/target/surefire-reports/*.xml"
                            }
                        }
                    }
                }
            }
        }

        // ── 4. Docker Build ──────────────────────────────────────────────────────
        stage('Docker Build All Images') {
            when {
                // Run when Docker is present AND build is not an outright FAILURE
                // (UNSTABLE = some tests failed but we still want Docker images built)
                expression {
                    return env.DOCKER_AVAILABLE == 'true' &&
                           currentBuild.currentResult != 'FAILURE'
                }
            }
            matrix {
                axes {
                    axis {
                        name    'SERVICE'
                        values  'eureka-server',
                                'api-gateway',
                                'auth-service',
                                'profile-service',
                                'job-service',
                                'application-service',
                                'notification-service',
                                'payment-service',
                                'subscription-service',
                                'interview-service',
                                'analytics-service'
                    }
                }
                stages {
                    stage('Docker Build') {
                        steps {
                            echo "🐳  Building Docker image for ${SERVICE}…"
                            dir("${SERVICE}") {
                                sh """
                                    DOCKER_BUILDKIT=0 docker build \\
                                        --network host \\
                                        -t ${DOCKER_IMAGE_PREFIX}-${SERVICE}:${IMAGE_TAG} \\
                                        -t ${DOCKER_IMAGE_PREFIX}-${SERVICE}:latest \\
                                        .
                                """
                            }
                        }
                    }
                }
            }
        }

        // ── 5. Docker Push (only on main / master) ───────────────────────────────
        stage('Docker Push All Images') {
            when {
                allOf {
                    // Only run when Docker CLI is present
                    expression { return env.DOCKER_AVAILABLE == 'true' }
                    anyOf {
                        branch 'main'
                        branch 'master'
                        // Also allow when GIT_BRANCH is main (regular pipeline jobs)
                        expression { return (env.GIT_BRANCH ?: '').contains('main') }
                    }
                }
            }
            steps {
                echo "📤  Pushing images to registry sequentially to avoid proxy overload…"
                withCredentials([usernamePassword(
                        credentialsId: "${DOCKER_CREDENTIALS_ID}",
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS')]) {
                    sh """
                        echo "\$DOCKER_PASS" | docker login ${DOCKER_REGISTRY} \\
                            -u "\$DOCKER_USER" --password-stdin
                        
                        SERVICES="eureka-server api-gateway auth-service profile-service job-service application-service notification-service payment-service subscription-service interview-service analytics-service"
                        
                        for SERVICE in \$SERVICES; do
                            echo "==> Pushing \$SERVICE..."
                            docker tag ${DOCKER_IMAGE_PREFIX}-\$SERVICE:${IMAGE_TAG} \\
                                \$DOCKER_USER/${DOCKER_IMAGE_PREFIX}-\$SERVICE:${IMAGE_TAG}
                            docker tag ${DOCKER_IMAGE_PREFIX}-\$SERVICE:${IMAGE_TAG} \\
                                \$DOCKER_USER/${DOCKER_IMAGE_PREFIX}-\$SERVICE:latest

                            docker push \$DOCKER_USER/${DOCKER_IMAGE_PREFIX}-\$SERVICE:${IMAGE_TAG}
                            docker push \$DOCKER_USER/${DOCKER_IMAGE_PREFIX}-\$SERVICE:latest
                        done
                    """
                }
            }
        }

        // ── 6. Deploy via docker compose (V2) ────────────────────────────────────
        stage('Deploy') {
            when {
                allOf {
                    expression { return env.DOCKER_AVAILABLE == 'true' }
                    anyOf {
                        branch 'main'
                        branch 'master'
                        expression { return (env.GIT_BRANCH ?: '').contains('main') }
                    }
                }
            }
            steps {
                echo '🚀  Deploying all services with docker compose…'
                sh """
                    # Try docker compose V2 (built-in plugin), fallback to docker-compose V1
                    if docker compose version > /dev/null 2>&1; then
                        COMPOSE_CMD="docker compose"
                    else
                        COMPOSE_CMD="docker-compose"
                    fi
                    IMAGE_TAG=${IMAGE_TAG} \$COMPOSE_CMD up -d --remove-orphans || true
                """
            }
        }

    }   // end stages

    post {
        success {
            echo "✅  Pipeline completed successfully — build #${env.BUILD_NUMBER}"
        }
        failure {
            echo "❌  Pipeline failed — check stage logs above."
        }
        always {
            // Remove dangling images; safe even when Docker is not installed
            sh 'docker image prune -f || true'
            cleanWs()
        }
    }
}
