# Loan application service

## Prerequisite
Java 11<br>
Apache Maven 3.6 or higher<br>
[Akka Serverless CLI](https://developer.lightbend.com/docs/akka-serverless/akkasls/install-akkasls.html) <br>
Docker 19.03 or higher<br>
Container registry with public access (like Docker Hub)<br>
Access to the `gcr.io/akkaserverless-public` container registry
cURL<br>
IDE / editor

## Create kickstart maven project
[Quick start template](https://docs.akkaserverless.dev/java/quickstart-template.html)
```
mvn archetype:generate \
-DarchetypeGroupId=com.akkaserverless \
-DarchetypeArtifactId=akkaserverless-maven-archetype \
-DarchetypeVersion=0.10.5
```

Define value for property 'groupId': `io.as`<br>
Define value for property 'artifactId': `loan-application`<br>
Define value for property 'version' 1.0-SNAPSHOT: :<br>
Define value for property 'package' io.as: : `io.as.loanapp`<br>

## Import generated project in your IDE/editor
Go through example proto files in `src/main/proto` folder:<br>
`counter_api.proto` <br>
`domain/counter_domain.proto`

<i><b>Delete all proto files after done</b></i>

## Define API data structure and endpoints (GRPC)
Create `io/as/loanapp/api` folder in `src/main/proto` folder. <br>
Create `loan_app_api.proto` in `src/main/proto/io/as/loanapp/api` folder. <br>
Create: <br>
- state
- commands
- service

<i><b>Tip</b></i>: Check content in `step-1` git tag  

## Define persistence (domain) data structure  (GRPC)
Create `io/as/loanapp/doman` folder in `src/main/proto` folder. <br>
Create `loan_app_domain.proto` in `src/main/proto/io/as/loanapp/domain` folder. <br>
Create: <br>
- state
- events

<i><b>Tip</b></i>: Check content in `step-1` git tag
## Add codegen annotations in API data structure and endpoints (GRPC)
In `src/main/proto/io/as/loanapp/api/loan_app_api.proto` add AkkaServerless codegen annotations to GRPC service
```
service LoanAppService {
```
```
option (akkaserverless.codegen) = {
    event_sourced_entity: {
      name: "io.as.loanapp.domain.LoanAppEntity"
      entity_type: "loanapp"
      state: "io.as.loanapp.domain.LoanAppDomainState"
      events: [
        "io.as.loanapp.domain.Submitted",
        "io.as.loanapp.domain.ReviewStarted",
        "io.as.loanapp.domain.ReviewApproved",
        "io.as.loanapp.domain.ReviewDeclined"
      ]
    }
  };
```
```
...
```
<i><b>Note</b></i>: `event_sourced_entity.name` has to be a unique name 
## Compile kickstart maven project to trigger codegen
```
mvn compile
```

Compile will generate help classes (`target/generated-*` folders) and skeleton classes<br><br>
Business logic:<br>
`src/main/java/io/as/loanapp/Main`<br>
`src/main/java/io/as/loanapp/domain/LoanAppEntity`<br>
<br>
Unit tests:<br>
`src/test/java/io/as/loanapp/domain/LoanAppEntityTest`<br>
Integration tests:<br>
`src/it/java/io/as/loanapp/api/LoanAppEntityIntegrationTest`<br>

## Implement entity skeleton class
<i><b>Tip</b></i>: Check content in `step-1` git tag

## Implement unit test
<i><b>Tip</b></i>: Check content in `step-1` git tag

## Run unit test
```
mvn test
```
## Implement integration test
<i><b>Tip</b></i>: Check content in `step-1` git tag

## Run integration test
```
mvn verify -Pit
```

<i><b>Note</b></i>: Integration tests uses [TestContainers](https://www.testcontainers.org/) to span integration environment so it could require some time to download required containers.
Also make sure docker is running.

## Run locally

In project root folder there is `docker-compose.yaml` for running `akkaserverless proxy` and (optionally) `google pubsub emulator`.
<i><b>Tip</b></i>: If you do not require google pubsub emulator then comment it out in `docker-compose.yaml`
```
docker-compose up
```

Start the service:

```
mvn compile exec:exec
```

## Test service locally
Submit loan application:
```
curl -XPOST -d '{
  "loan_app_id": "537e52b8-1732-11ec-9621-0242ac130002",
  "client_name": "John",
  "client_surname": "Doe",
  "client_ssn": "123456",
  "client_email": "john@doe.io",
  "client_monthly_income_cents": 60000,
  "loan_amount_cents": 20000,
  "loan_duration_months": 12
}' http://localhost:9000/loanapp/537e52b8-1732-11ec-9621-0242ac130002 -H "Content-Type: application/json"
```

Get loan application:
```
curl -XGET http://localhost:9000/loanapp/537e52b8-1732-11ec-9621-0242ac130002 -H "Content-Type: application/json"
```

Start review:
```
curl -XPUT http://localhost:9000/loanapp/537e52b8-1732-11ec-9621-0242ac130002/review/start -H "Content-Type: application/json"
```

Approve review:
```
curl -XPUT http://localhost:9000/loanapp/537e52b8-1732-11ec-9621-0242ac130002/review/approve -H "Content-Type: application/json"
```

## Package

<i><b>Note</b></i>: Make sure you have updated `dockerImage` in your `pom.xml` and that your local docker is authenticated with your docker container registry

```
mvn package
```

Result:

`
[INFO] BUILD SUCCESS
`
<br><br>

Push docker image to docker repository:
```
mvn docker:push
```

## Register for Akka Serverless account or Login with existing account
[Login, Register, Register via Google](https://console.akkaserverless.com/p/login)

## akkasls CLI
Validate version:
```
akkasls version
```
Login (need to be logged in the Akka Serverless Console in web browser):
```
akkasls auth login
```
Create new project:
```
akkasls projects new loan-application --region <REGION>
```
<i><b>Note</b></i>: Replace `<REGION>` with desired region

List projects:
```
akkasls projects list
```
Set project:
```
akkasls config set project loan-application
```
## Deploy service
```
akkasls service deploy loan-application my-docker-repo/loan-application:1.0-SNAPSHOT
```
<i><b>Note</b></i>: Replace `my-docker-repo` with your docker repository

List services:
```
NAME    AGE   REPLICAS   STATUS   DESCRIPTION   
loan-application   13m   1          Ready 
```
## Expose service
```
akkasls services expose loan-application
```
Result:
`
Service 'loan-application' was successfully exposed at: somehost.akkaserverless.app
`
<br><br>
Get service host:
```
akkasls services get loan-application | grep Host
```
Result
`
Host:           somehost.akkaserverless.app
`
## Test service in production
Submit loan application:
```
curl -XPOST -d '{
  "loan_app_id": "537e52b8-1732-11ec-9621-0242ac130002",
  "client_name": "John",
  "client_surname": "Doe",
  "client_ssn": "123456",
  "client_email": "john@doe.io",
  "client_monthly_income_cents": 60000,
  "loan_amount_cents": 20000,
  "loan_duration_months": 12
}' https://odd-mud-5285.us-east1.akkaserverlessapps.com/loanapp/537e52b8-1732-11ec-9621-0242ac130002 -H "Content-Type: application/json"
```

Get loan application:
```
curl -XGET https://odd-mud-5285.us-east1.akkaserverlessapps.com/loanapp/537e52b8-1732-11ec-9621-0242ac130002 -H "Content-Type: application/json"
```

Start review:
```
curl -XPUT https://odd-mud-5285.us-east1.akkaserverlessapps.com/loanapp/537e52b8-1732-11ec-9621-0242ac130002/review/start -H "Content-Type: application/json"
```

Approve review:
```
curl -XPUT https://odd-mud-5285.us-east1.akkaserverlessapps.com/loanapp/537e52b8-1732-11ec-9621-0242ac130002/review/approve -H "Content-Type: application/json"
```