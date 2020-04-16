
oc delete project notation-svc-dev notation-svc-acp notation-svc-prd  notation-svc-cicd
sleep 10
oc new-project notation-svc-dev --display-name="Dev" --description="dev"
sleep 2
oc new-project notation-svc-acp --display-name="Testing" --description="test"
sleep 2
oc new-project notation-svc-prd --display-name="Production" --description="production"
sleep 2
oc new-project notation-svc-cicd --display-name="CICD" --description="CICD environment"
sleep 2
oc project notation-svc-cicd 
#oc new-app jenkins-ephemeral
#oc create bc -f notation-svc-cicd-bc.yml