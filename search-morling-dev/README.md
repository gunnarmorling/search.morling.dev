# search.morling.dev

This project contains the serverless search functionality of the blog at https://morling.dev.
It is built using Quarkus and deployed to AWS Lambda.

## Building

```
mvn clean package -Pnative -DskipTests=true -Dquarkus.native.additional-build-args=--report-unsupported-elements-at-runtime -Dquarkus.native.container-build=true
```

## Deploying

```
sam package --template-file sam.native.yaml --output-template-file packaged.yaml --s3-bucket <S3 deployment bucket>

sam deploy --template-file packaged.yaml --capabilities CAPABILITY_IAM --stack-name search-morling-dev
```

```
aws apigateway update-stage \
--rest-api-id <API ID> \
--stage-name Prod \
--patch-operations \
'op=replace,path=/*/*/throttling/rateLimit,value=25'

aws apigateway update-stage \
--rest-api-id <API ID> \
--stage-name Prod \
--patch-operations \
'op=replace,path=/*/*/throttling/burstLimit,value=50'
```