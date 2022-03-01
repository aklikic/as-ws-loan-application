package io.as.loanapp.domain;

import com.akkaserverless.javasdk.eventsourcedentity.EventSourcedEntity;
import com.akkaserverless.javasdk.eventsourcedentity.EventSourcedEntityContext;
import com.akkaserverless.javasdk.testkit.EventSourcedResult;
import com.google.protobuf.Empty;
import io.as.loanapp.api.LoanAppApi;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class LoanAppEntityTest {

  private LoanAppApi.SubmitCommand create(String loanAppId){
    return create(loanAppId,1000,500,24);
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


  @Test
  public void submitTest() {
    String loanAppId = UUID.randomUUID().toString();
    LoanAppEntityTestKit testKit = LoanAppEntityTestKit.of(loanAppId,LoanAppEntity::new);//note loanAppId has to be used to differentiate tests
    submitLoan(testKit,loanAppId);
  }

  private void assertGet(LoanAppEntityTestKit testKit,String loanAppId, LoanAppApi.LoanAppStatus status){
    EventSourcedResult<LoanAppApi.LoanAppState> getResult = testKit.get(LoanAppApi.GetCommand.newBuilder().setLoanAppId(loanAppId).build());
    assertFalse(getResult.didEmitEvents());
    assertEquals(status,getResult.getReply().getStatus());
  }

  private void submitLoan(LoanAppEntityTestKit testKit,String loanAppId){
    EventSourcedResult<Empty> result = testKit.submit(create(loanAppId));
    LoanAppDomain.Submitted event = result.getNextEventOfType(LoanAppDomain.Submitted.class);
    assertEquals(event.getLoanAppId(),loanAppId);
    assertGet(testKit,loanAppId, LoanAppApi.LoanAppStatus.STATUS_WAITING_FOR_REVIEW);
  }

  private void startReview(LoanAppEntityTestKit testKit,String loanAppId){
    EventSourcedResult<Empty> result = testKit.startReview(LoanAppApi.StartReviewCommand.newBuilder().setLoanAppId(loanAppId).build());
    LoanAppDomain.ReviewStarted event = result.getNextEventOfType(LoanAppDomain.ReviewStarted.class);
    assertEquals(event.getLoanAppId(),loanAppId);
    assertGet(testKit,loanAppId, LoanAppApi.LoanAppStatus.STATUS_IN_REVIEW);
  }


  @Test
  public void startReviewTest() {
    String loanAppId = UUID.randomUUID().toString();
    LoanAppEntityTestKit testKit = LoanAppEntityTestKit.of(loanAppId,LoanAppEntity::new);
    submitLoan(testKit,loanAppId);
    startReview(testKit,loanAppId);
  }


  @Test
  public void approveReviewTest() {
    String loanAppId = UUID.randomUUID().toString();
    LoanAppEntityTestKit testKit = LoanAppEntityTestKit.of(loanAppId,LoanAppEntity::new);

    submitLoan(testKit,loanAppId);
    startReview(testKit,loanAppId);
    EventSourcedResult<Empty> result = testKit.approveReview(LoanAppApi.ApproveReviewCommand.newBuilder().setLoanAppId(loanAppId).build());
    LoanAppDomain.ReviewApproved event = result.getNextEventOfType(LoanAppDomain.ReviewApproved.class);
    assertEquals(event.getLoanAppId(),loanAppId);
    assertGet(testKit,loanAppId, LoanAppApi.LoanAppStatus.STATUS_APPROVED);
  }


  @Test
  public void declineReviewTest() {
    LoanAppEntityTestKit testKit = LoanAppEntityTestKit.of(LoanAppEntity::new);
    String loanAppId = UUID.randomUUID().toString();
    submitLoan(testKit,loanAppId);
    startReview(testKit,loanAppId);
    EventSourcedResult<Empty> result = testKit.declineReview(LoanAppApi.DeclineReviewCommand.newBuilder().setLoanAppId(loanAppId).setReason("some reason").build());
    LoanAppDomain.ReviewDeclined event = result.getNextEventOfType(LoanAppDomain.ReviewDeclined.class);
    assertEquals(event.getLoanAppId(),loanAppId);
    assertGet(testKit,loanAppId, LoanAppApi.LoanAppStatus.STATUS_DECLINED);
  }

}
