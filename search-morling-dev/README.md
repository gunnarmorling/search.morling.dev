# search.morling.dev

This project contains the serverless search functionality of the blog at https://morling.dev.
It is built using Quarkus and deployed to AWS Lambda.

## Index Update

```
wget -O src/main/resources/META-INF/searchindex.json  https://www.morling.dev/searchindex.json
mvn clean package -DskipTests=true
cp -r target/index src/main/zip.native
```

## Building

```
mvn clean package -Pnative,lambda -DskipTests=true -Dquarkus.native.container-build=true
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

## Deployment Alternatives

### Google Cloud Run

Preparations:

```
gcloud auth login
gcloud config set project search-morling-dev
gcloud auth configure-docker
```

Build:

```
mvn clean package -Pnative -DskipTests=true -Dquarkus.native.container-build=true
docker build -f src/main/docker/Dockerfile.native -t gcr.io/search-morling-dev/search .
```

Deployment:

```
docker push gcr.io/search-morling-dev/search:latest
gcloud run deploy --image gcr.io/search-morling-dev/search --platform managed
```
