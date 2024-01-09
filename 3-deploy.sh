#!/bin/bash -eux

aws cloudformation deploy --template-file cfn-template.yaml --stack-name file-processor --capabilities CAPABILITY_NAMED_IAM