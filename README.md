
# Overview

When a message is received, its contents are printed with the type from the message attribute 'contentType'

The cloudformation sets up 3 lambdas, one processes contentType=a, once contentType=b and one contentType=c





# Build
run 
```shell
./2-build.sh version push
```
This will build 'version' (eg 0.0.9) and push to ECR

# Deploy
Edit `cfn-template.yaml` to make sure the image version matches the one built
run 
```shell
./3-deploy.sh
```

Note, the cloudformation creates the S3 bucket - arguably, this should be done externally and the name/arn passed in.


## removing stack
The stack does not remove cleanly if the bucket is not empty - need to empty first then delete the bucket.


# Testing
Copy a file to s3 - output should be made to cloud watch
aws s3 cp test-file.txt s3://file-input-sorted-net/test-file3.txt