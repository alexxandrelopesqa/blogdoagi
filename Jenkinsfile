pipeline {
  agent any

  options {
    buildDiscarder(logRotator(numToKeepStr: '20'))
    disableConcurrentBuilds()
  }

  parameters {
    choice(name: 'BROWSER', choices: ['chromium', 'firefox', 'webkit'], description: 'Navegador')
    booleanParam(name: 'ATTACH_EVIDENCE', defaultValue: true, description: 'Anexos no Allure')
    string(name: 'BASE_URL', defaultValue: 'https://blog.agibank.com.br', description: 'URL alvo para os testes')
  }

  environment {
    CI = 'true'
    HEADLESS = 'true'
    PLAYWRIGHT_BROWSERS_PATH = '.playwright-browsers'
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Prepare Wrapper') {
      steps {
        sh 'chmod +x mvnw'
      }
    }

    stage('Download Dependencies') {
      steps {
        sh './mvnw -B -q dependency:go-offline'
      }
    }

    stage('Install Playwright Browsers') {
      steps {
        sh './mvnw -B -q exec:java'
      }
    }

    stage('Run Tests') {
      steps {
        withEnv([
          "BROWSER=${params.BROWSER}",
          "ATTACH_EVIDENCE=${params.ATTACH_EVIDENCE}"
        ]) {
          sh "./mvnw -B clean test -Dbase.url=${params.BASE_URL}"
        }
      }
    }

    stage('Generate Allure Report') {
      steps {
        sh './mvnw -B allure:report'
      }
    }
  }

  post {
    always {
      junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
      archiveArtifacts allowEmptyArchive: true, artifacts: 'target/allure-results/**,target/site/allure-maven-plugin/**,target/artifacts/**'
    }
  }
}
