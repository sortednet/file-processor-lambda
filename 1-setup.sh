#!/bin/bash


# could setup the ECR here too ???
TEMPLATE="ecr-cfn-template"
aws cloudformation deploy --template-file ecr-cfn-template.yaml --stack-name file-extractor-ecr --capabilities CAPABILITY_NAMED_IAM