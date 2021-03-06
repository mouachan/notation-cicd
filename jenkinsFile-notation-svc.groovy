// define commands
def mvnCmd = "./mvnw"
// injection of environment variables is not done so set them here...
def sourceRef = "master"
def sourceUrl = "https://github.com/mouachan/dmn-svc-notation.git"
def githubId = "github"
def devProject = "credit"
def artifact = "dmn-svc-notation"
def version = "1.0.0"
def contextDirPath = "/notation/dmn-svc-notation"
// namespaces
def namespace_cicd = "notation-svc-cicd"
def namespace_dev = "notation-svc-dev"
def namespace_acp = "notation-svc-acp"
def namespace_prd = "notation-svc-prd"		
def appname = "dmn-svc-notation"
def image_version = "latest"


node('maven') {
  stage 'checkout'
     git branch: sourceRef, url: sourceUrl, credentialsId: githubId
  stage 'build'
    echo "building"
     sh "${mvnCmd} clean install -DskipTests=true"
  stage 'test'
    echo "testing "
     sh "${mvnCmd} test"
  stage 'deployInDev'
    echo "building container image"
    sh "oc delete all --selector build=dmn-svc-notation-bc -n ${namespace_cicd}"
        sh "oc delete all --selector build=dmn-svc-notation-bc -n ${namespace_dev}"
    sh "${mvnCmd} clean package  -DskipTests=true"
    sh "oc process -f ./template/dmn-svc-bc.yml  | oc apply -n ${namespace_dev} -f -"
    sh "oc start-build dmn-svc-notation-bc -n ${namespace_dev} --from-file=./target/notation-1.0-SNAPSHOT.jar"
    //sh "oc patch bc/dmn-svc-notation -p \'{\"spec\":{\"strategy\":{\"dockerStrategy\":{\"dockerfilePath\":\"src/main/docker/Dockerfile.jvm\"}}}}\'"     
    echo "promoting image"
	  sh "oc tag ${namespace_dev}/${artifact}:${version} ${namespace_acp}/${artifact}:${image_version}"
  stage 'deployInAcp'
    echo "creating openshift deployment objects"
    sh "oc process -f ./template/dmn-svc.yml --param APP_NAME=${artifact} --param APP_VERSION=${version} --param IMAGE_VERSION=${image_version} | oc apply -n ${namespace_acp} -f -"
}

