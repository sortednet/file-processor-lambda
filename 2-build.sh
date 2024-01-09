#!/bin/bash -eux

# Usage
# Build latest but do not push
# ./2-build.sh
# Build and push version 0.0.1
# ./2-build.sh 0.0.1 push

# Compile and copy the dependencies to target/dependencies
mvn clean test dependency:copy-dependencies -DincludeScope=runtime

IMAGE_TAG=latest

if [[ $# != 0 ]]; then
  IMAGE_TAG=$1
fi

AWS_ACCT=573128978443
AWS_REGION=ap-southeast-2

IMAGE_NAME=file-processor
IMAGE_REGISTRY=${AWS_ACCT}.dkr.ecr.${AWS_REGION}.amazonaws.com
IMAGE=${IMAGE_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}

docker build --platform linux/amd64 -t ${IMAGE} .

# Specify any second param to push the image
if [[ $# == 2 ]]; then
  aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${AWS_ACCT}.dkr.ecr.${AWS_REGION}.amazonaws.com
  docker push ${IMAGE}
fi