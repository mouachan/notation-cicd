  // define commands
  def mvnCmd = "mvnw"
  // injection of environment variables is not done so set them here...
  def sourceRef = "master"
  def sourceUrl = "https://github.com/mouachan/credit"
  def githubId = "github"
  def devProject = "credit"
  def artifact = "dmn-svc-notation"
  def version = "1.0.0"
  def contextDirPath = "notation/dmn-svc-notation"
// namespaces
def namespace_dev = "notation-svc-dev"
def namespace_acp = "notation-svc-acp"
def namespace_prd = "notation-svc-prd"		
def appname = "dmn-svc-notation"
def image_version = "1.0.0"
def registry = "docker-registry.default.svc:5000"


node('maven') {
  stage 'checkout'
     git branch: sourceRef, url: sourceUrl, credentialsId: githubId, contextDir: contextDirPath
  stage 'build'
     sh "${mvnCmd} clean install -DskipTests=true"
  stage 'test'
    sh "${mvnCmd} test"
  stage 'deployInDev'
    echo "building container image"
    sh "${mvnCmd} fabric8:build -Dmaven.test.skip -Dimage.version=latest -Dfabric8.namespace=${namespace_dev}"
    sh "oc process template/dmn-svc-template-template -n notation-cicd --param APP_NAME=${artifact} --param APP_VERSION=${version} --param IMAGE_VERSION=${image_version} | oc apply -n ${namespace_dev} -f -"
    echo "promoting image"
	  sh "oc tag ${namespace_dev}/${artifact}:latest ${namespace_acp}/${artifact}:${image_version}"
  stage 'deployInAcp'
    echo "creating openshift deployment objects"
	  echo "generating ConfigMap"
    sh "oc process template/dmn-svc-template-template -n notation-cicd --param APP_NAME=${artifact} --param APP_VERSION=${version} --param IMAGE_VERSION=${image_version} | oc apply -n ${namespace_acp} -f -"
}

