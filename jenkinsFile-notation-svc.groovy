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
def namespace_dev = "notation-svc-dev"
def namespace_acp = "notation-svc-acp"
def namespace_prd = "notation-svc-prd"		
def appname = "dmn-svc-notation"
def image_version = "1.0.0"


node('maven') {
  stage 'checkout'
     git branch: sourceRef, url: sourceUrl, credentialsId: githubId
  stage 'build'
    ech "building"
    // sh "${mvnCmd} clean install -DskipTests=true"
  stage 'test'
  echo "test "
  // sh "${mvnCmd} test"
  stage 'deployInDev'
    echo "building container image"
    sh "${mvnCmd} clean package -Dquarkus.kubernetes.deploy=true -Dquarkus.kubernetes.deployment-target=openshift -Dquarkus.container-image.registry=image-registry.openshift-image-registry.svc:5000 -Dquarkus.container-image.build=true -Dquarkus.container-image.push=true"
    sh "oc process -f ./template/dmn-svc.yml  --param APP_NAME=${artifact} --param APP_VERSION=${version} --param IMAGE_VERSION=${image_version} | oc apply -n ${namespace_dev} -f -"
    echo "promoting image"
	  sh "oc tag ${namespace_dev}/${artifact}:${version} ${namespace_acp}/${artifact}:${image_version}"
  stage 'deployInAcp'
    echo "creating openshift deployment objects"
    sh "oc process -f ./template/dmn-svc.yml --param APP_NAME=${artifact} --param APP_VERSION=${version} --param IMAGE_VERSION=${image_version} | oc apply -n ${namespace_acp} -f -"
}

