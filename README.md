# Loan application service

## Prerequisite
Java 11<br>
Apache Maven 3.6 or higher<br>
[Akka Serverless CLI](https://developer.lightbend.com/docs/akka-serverless/akkasls/install-akkasls.html) <br>
Docker 19.03 or higher<br>
Container registry with public access (like Docker Hub)<br>
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
## Implement integration test
<i><b>Tip</b></i>: Check content in `step-1` git tag