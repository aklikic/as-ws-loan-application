package io.as.loanapp.api;

import com.akkaserverless.javasdk.testkit.EventSourcedResult;
import com.akkaserverless.javasdk.testkit.junit.AkkaServerlessTestKitResource;
import com.google.protobuf.Empty;
import io.as.loanapp.Main;
import io.as.loanapp.domain.LoanAppDomain;
import io.as.loanapp.domain.LoanAppEntity;
import io.as.loanapp.domain.LoanAppEntityTestKit;
import io.as.loanapp.view.LoanAppByStatus;
import io.as.loanapp.view.LoanAppByStatusClient;
import io.as.loanapp.view.LoanAppByStatusModel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.TimeUnit.*;
import static org.junit.Assert.*;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

// Example of an integration test calling our service via the Akka Serverless proxy
// Run all test classes ending with "IntegrationTest" using `mvn verify -Pit`
public class LoanAppEntityIntegrationTest {

  /**
   * The test kit starts both the service container and the Akka Serverless proxy.
   */
  @ClassRule
  public static final AkkaServerlessTestKitResource testKit =
    new AkkaServerlessTestKitResource(Main.createAkkaServerless());

  /**
   * Use the generated gRPC client to call the service through the Akka Serverless proxy.
   */
  private final LoanAppService client;
  private final LoanAppByStatus view;

  public LoanAppEntityIntegrationTest() {
    client = testKit.getGrpcClient(LoanAppService.class);
    view = testKit.getGrpcClient(LoanAppByStatus.class);
  }

  private LoanAppApi.SubmitCommand create(String loanAppId, long monthlyIncomeCents, long loanAmountCents, int loanDurationMonths){
    return LoanAppApi.SubmitCommand.newBuilder()
            .setLoanAppId(loanAppId)
            .setClientName("John")
            .setClientSurname("Doe")
            .setClientEmail("john@doe.io")
            .setClientSsn("123456789")
            .setClientMonthlyIncomeCents(monthlyIncomeCents)
            .setLoanAmountCents(loanAmountCents)
            .setLoanDurationMonths(loanDurationMonths)
            .build();
  }
  private LoanAppApi.SubmitCommand create(String loanAppId){
    return create(loanAppId,1000,500,24);
  }

  private void assertGet(String loanAppId, LoanAppApi.LoanAppStatus status) throws Exception{
    LoanAppApi.LoanAppState loanApp = client.get(LoanAppApi.GetCommand.newBuilder().setLoanAppId(loanAppId).build()).toCompletableFuture().get(5,SECONDS);
    assertNotNull(loanApp);
    assertEquals(status,loanApp.getStatus());
  }
  @Test
  public void submitSuccess() throws Exception {

    String loanAppId = UUID.randomUUID().toString();
    client.submit(create(loanAppId)).toCompletableFuture().get(5, SECONDS); //note use get for every call to get sequential deterministic results
    assertGet(loanAppId, LoanAppApi.LoanAppStatus.STATUS_WAITING_FOR_REVIEW);
  }

  @Test
  public void submitOnAlreadySubmittedEntity() throws Exception {
    String loanAppId = UUID.randomUUID().toString();
    client.submit(create(loanAppId)).toCompletableFuture().get(5, SECONDS);
    assertGet(loanAppId, LoanAppApi.LoanAppStatus.STATUS_WAITING_FOR_REVIEW);
    client.submit(create(loanAppId)).toCompletableFuture().get(5, SECONDS);
    assertGet(loanAppId, LoanAppApi.LoanAppStatus.STATUS_WAITING_FOR_REVIEW);
  }

  @Test
  public void startReviewSuccess() throws Exception {
    String loanAppId = UUID.randomUUID().toString();
    client.submit(create(loanAppId)).toCompletableFuture().get(5, SECONDS);
    assertGet(loanAppId, LoanAppApi.LoanAppStatus.STATUS_WAITING_FOR_REVIEW);
    client.startReview(LoanAppApi.StartReviewCommand.newBuilder().setLoanAppId(loanAppId).build()).toCompletableFuture().get(5, SECONDS);
    assertGet(loanAppId, LoanAppApi.LoanAppStatus.STATUS_IN_REVIEW);
  }

  @Test
  public void startReviewOnNonSubmittedEntity() throws Exception {
    String loanAppId = UUID.randomUUID().toString();
    ExecutionException exception = assertThrows(ExecutionException.class, () -> client.startReview(LoanAppApi.StartReviewCommand.newBuilder().setLoanAppId(loanAppId).build()).toCompletableFuture().get(5, SECONDS));
    assertEquals(StatusRuntimeException.class,exception.getCause().getClass());
    Status status = ((StatusRuntimeException)exception.getCause()).getStatus();
    assertEquals(LoanAppEntity.ERROR_NOT_FOUND,status.getDescription());
  }

  @Test
  public void approveReviewSuccess() throws Exception {
    String loanAppId = UUID.randomUUID().toString();
    client.submit(create(loanAppId)).toCompletableFuture().get(5, SECONDS);
    assertGet(loanAppId, LoanAppApi.LoanAppStatus.STATUS_WAITING_FOR_REVIEW);
    client.startReview(LoanAppApi.StartReviewCommand.newBuilder().setLoanAppId(loanAppId).build()).toCompletableFuture().get(5, SECONDS);
    assertGet(loanAppId, LoanAppApi.LoanAppStatus.STATUS_IN_REVIEW);
    client.approveReview(LoanAppApi.ApproveReviewCommand.newBuilder().setLoanAppId(loanAppId).build()).toCompletableFuture().get(5, SECONDS);
    assertGet(loanAppId, LoanAppApi.LoanAppStatus.STATUS_APPROVED);
  }

  @Test
  public void declineReviewSuccess() throws Exception {
    String loanAppId = UUID.randomUUID().toString();
    client.submit(create(loanAppId)).toCompletableFuture().get(5, SECONDS);
    assertGet(loanAppId, LoanAppApi.LoanAppStatus.STATUS_WAITING_FOR_REVIEW);
    client.startReview(LoanAppApi.StartReviewCommand.newBuilder().setLoanAppId(loanAppId).build()).toCompletableFuture().get(5, SECONDS);
    assertGet(loanAppId, LoanAppApi.LoanAppStatus.STATUS_IN_REVIEW);
    client.declineReview(LoanAppApi.DeclineReviewCommand.newBuilder().setLoanAppId(loanAppId).build()).toCompletableFuture().get(5, SECONDS);
    assertGet(loanAppId, LoanAppApi.LoanAppStatus.STATUS_DECLINED);
  }

  @Test
  public void viewTest() throws Exception {
    String loanAppId = UUID.randomUUID().toString();
    client.submit(create(loanAppId)).toCompletableFuture().get(5, SECONDS);
    assertView(LoanAppApi.LoanAppStatus.STATUS_WAITING_FOR_REVIEW,1);
    client.startReview(LoanAppApi.StartReviewCommand.newBuilder().setLoanAppId(loanAppId).build()).toCompletableFuture().get(5, SECONDS);
    assertView( LoanAppApi.LoanAppStatus.STATUS_IN_REVIEW,1);
    client.approveReview(LoanAppApi.ApproveReviewCommand.newBuilder().setLoanAppId(loanAppId).build()).toCompletableFuture().get(5, SECONDS);
    assertView(LoanAppApi.LoanAppStatus.STATUS_APPROVED,1);
    assertView(LoanAppApi.LoanAppStatus.STATUS_DECLINED,0);

    String loanAppId2 = UUID.randomUUID().toString();
    client.submit(create(loanAppId2)).toCompletableFuture().get(5, SECONDS);
    String loanAppId3 = UUID.randomUUID().toString();
    client.submit(create(loanAppId3)).toCompletableFuture().get(5, SECONDS);
    assertView(LoanAppApi.LoanAppStatus.STATUS_WAITING_FOR_REVIEW,2);
    assertView(LoanAppApi.LoanAppStatus.STATUS_IN_REVIEW,0);

  }

  private void assertView(LoanAppApi.LoanAppStatus status, int expectedResults) throws Exception{
    Thread.sleep(1000);//needed for the view eventual consistent update
    LoanAppByStatusModel.GetLoanAppsByStatusResponse response =
            view.getLoanAppsByStatus(LoanAppByStatusModel.GetLoanAppsByStatusRequest.newBuilder()
                    .setStatusId(status.getNumber()).build())
                    .toCompletableFuture().get(5,SECONDS);
    assertNotNull(response);
    assertEquals(expectedResults,response.getResultsCount());
    assertFalse(response.getResultsList().stream().filter(l->l.getStatus()!=status).findAny().isPresent());
  }
}
