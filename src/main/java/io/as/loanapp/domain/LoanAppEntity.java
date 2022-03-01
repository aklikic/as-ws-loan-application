package io.as.loanapp.domain;

import com.akkaserverless.javasdk.eventsourcedentity.EventSourcedEntity;
import com.akkaserverless.javasdk.eventsourcedentity.EventSourcedEntity.Effect;
import com.akkaserverless.javasdk.eventsourcedentity.EventSourcedEntityContext;
import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import io.as.loanapp.api.LoanAppApi;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

/** An event sourced entity. */
public class LoanAppEntity extends AbstractLoanAppEntity {

  public static final String ERROR_NOT_FOUND = "Not found";
  public static final String ERROR_WRONG_STATUS = "Wrong status";

  @SuppressWarnings("unused")
  private final String entityId;

  public LoanAppEntity(EventSourcedEntityContext context) {
    this.entityId = context.entityId();
  }

  @Override
  public LoanAppDomain.LoanAppDomainState emptyState() {
    return LoanAppDomain.LoanAppDomainState.getDefaultInstance();
  }

  @Override
  public Effect<Empty> submit(LoanAppDomain.LoanAppDomainState currentState, LoanAppApi.SubmitCommand submitCommand) {
    if(currentState.equals(LoanAppDomain.LoanAppDomainState.getDefaultInstance())) {
      LoanAppDomain.Submitted event = LoanAppDomain.Submitted.newBuilder()
              .setLoanAppId(submitCommand.getLoanAppId())
              .setClientName(submitCommand.getClientName())
              .setClientSurname(submitCommand.getClientSurname())
              .setClientSsn(submitCommand.getClientSsn())
              .setClientEmail(submitCommand.getClientEmail())
              .setClientMonthlyIncomeCents(submitCommand.getClientMonthlyIncomeCents())
              .setLoanAmountCents(submitCommand.getLoanAmountCents())
              .setLoanDurationMonths(submitCommand.getLoanDurationMonths())
              .setUpdateTimestamp(Timestamps.fromMillis(System.currentTimeMillis()))
              .build();
      return effects().emitEvent(event).thenReply(__ -> Empty.getDefaultInstance());
    }else if(currentState.getStatus() == LoanAppDomain.LoanAppDomainStatus.STATUS_WAITING_FOR_REVIEW)
      return effects().reply(Empty.getDefaultInstance());
    else
      return effects().error(ERROR_WRONG_STATUS);
  }

  @Override
  public Effect<LoanAppApi.LoanAppState> get(LoanAppDomain.LoanAppDomainState currentState, LoanAppApi.GetCommand getCommand) {
    if(currentState.equals(LoanAppDomain.LoanAppDomainState.getDefaultInstance()))
      return effects().error(ERROR_NOT_FOUND);
    return effects().reply(map(currentState));
  }

  private LoanAppApi.LoanAppState map(LoanAppDomain.LoanAppDomainState state){
    return  LoanAppApi.LoanAppState.newBuilder()
            .setClientName(state.getClientName())
            .setClientSurname(state.getClientSurname())
            .setClientSsn(state.getClientSsn())
            .setClientEmail(state.getClientEmail())
            .setClientMonthlyIncomeCents(state.getClientMonthlyIncomeCents())
            .setLoanAmountCents(state.getLoanAmountCents())
            .setLoanDurationMonths(state.getLoanDurationMonths())
            .setLastUpdateTimestamp(state.getLastUpdateTimestamp())
            .setStatus(map(state.getStatus()))
            .setDeclineReason(state.getDeclineReason())
            .build();
  }
  private LoanAppApi.LoanAppStatus map(LoanAppDomain.LoanAppDomainStatus status){
    return LoanAppApi.LoanAppStatus.forNumber(status.getNumber());
  }

  @Override
  public Effect<Empty> startReview(LoanAppDomain.LoanAppDomainState currentState, LoanAppApi.StartReviewCommand startReviewCommand) {
    if(currentState.equals(LoanAppDomain.LoanAppDomainState.getDefaultInstance()))
      return effects().error(ERROR_NOT_FOUND);
    if(currentState.getStatus() == LoanAppDomain.LoanAppDomainStatus.STATUS_WAITING_FOR_REVIEW){
      LoanAppDomain.ReviewStarted event = LoanAppDomain.ReviewStarted.newBuilder()
              .setLoanAppId(startReviewCommand.getLoanAppId())
              .setUpdateTimestamp(Timestamps.fromMillis(System.currentTimeMillis())).build();
      return effects().emitEvent(event).thenReply(__ -> Empty.getDefaultInstance());

    }else if(currentState.getStatus() == LoanAppDomain.LoanAppDomainStatus.STATUS_IN_REVIEW)
      return effects().reply(Empty.getDefaultInstance());
    else
      return effects().error(ERROR_WRONG_STATUS);

  }

  @Override
  public Effect<Empty> approveReview(LoanAppDomain.LoanAppDomainState currentState, LoanAppApi.ApproveReviewCommand approveReviewCommand) {
    if(currentState.equals(LoanAppDomain.LoanAppDomainState.getDefaultInstance()))
      return effects().error(ERROR_NOT_FOUND);
    if(currentState.getStatus() == LoanAppDomain.LoanAppDomainStatus.STATUS_IN_REVIEW){
      LoanAppDomain.ReviewApproved event = LoanAppDomain.ReviewApproved.newBuilder()
              .setLoanAppId(approveReviewCommand.getLoanAppId())
              .setUpdateTimestamp(Timestamps.fromMillis(System.currentTimeMillis())).build();
      return effects().emitEvent(event).thenReply(__ -> Empty.getDefaultInstance());

    }else if(currentState.getStatus() == LoanAppDomain.LoanAppDomainStatus.STATUS_APPROVED)
      return effects().reply(Empty.getDefaultInstance());
    else
      return effects().error(ERROR_WRONG_STATUS);
  }

  @Override
  public Effect<Empty> declineReview(LoanAppDomain.LoanAppDomainState currentState, LoanAppApi.DeclineReviewCommand declineReviewCommand) {
    if(currentState.equals(LoanAppDomain.LoanAppDomainState.getDefaultInstance()))
      return effects().error(ERROR_NOT_FOUND);
    if(currentState.getStatus() == LoanAppDomain.LoanAppDomainStatus.STATUS_IN_REVIEW){
      LoanAppDomain.ReviewDeclined event = LoanAppDomain.ReviewDeclined.newBuilder()
              .setLoanAppId(declineReviewCommand.getLoanAppId())
              .setReason(declineReviewCommand.getReason())
              .setUpdateTimestamp(Timestamps.fromMillis(System.currentTimeMillis())).build();
      return effects().emitEvent(event).thenReply(__ -> Empty.getDefaultInstance());

    }else if(currentState.getStatus() == LoanAppDomain.LoanAppDomainStatus.STATUS_DECLINED)
      return effects().reply(Empty.getDefaultInstance());
    else
      return effects().error(ERROR_WRONG_STATUS);
  }

  @Override
  public LoanAppDomain.LoanAppDomainState submitted(LoanAppDomain.LoanAppDomainState currentState, LoanAppDomain.Submitted submitted) {
    return LoanAppDomain.LoanAppDomainState.newBuilder()
            .setClientName(submitted.getClientName())
            .setClientSurname(submitted.getClientSurname())
            .setClientSsn(submitted.getClientSsn())
            .setClientEmail(submitted.getClientEmail())
            .setClientMonthlyIncomeCents(submitted.getClientMonthlyIncomeCents())
            .setLoanAmountCents(submitted.getLoanAmountCents())
            .setLoanDurationMonths(submitted.getLoanDurationMonths())
            .setLastUpdateTimestamp(submitted.getUpdateTimestamp())
            .setStatus(LoanAppDomain.LoanAppDomainStatus.STATUS_WAITING_FOR_REVIEW)
            .build();
  }
  @Override
  public LoanAppDomain.LoanAppDomainState reviewStarted(LoanAppDomain.LoanAppDomainState currentState, LoanAppDomain.ReviewStarted reviewStarted) {
    return currentState.toBuilder().setStatus(LoanAppDomain.LoanAppDomainStatus.STATUS_IN_REVIEW).setLastUpdateTimestamp(reviewStarted.getUpdateTimestamp()).build();
  }

  @Override
  public LoanAppDomain.LoanAppDomainState reviewApproved(LoanAppDomain.LoanAppDomainState currentState, LoanAppDomain.ReviewApproved reviewApproved) {
    return currentState.toBuilder().setStatus(LoanAppDomain.LoanAppDomainStatus.STATUS_APPROVED).setLastUpdateTimestamp(reviewApproved.getUpdateTimestamp()).build();
  }
  @Override
  public LoanAppDomain.LoanAppDomainState reviewDeclined(LoanAppDomain.LoanAppDomainState currentState, LoanAppDomain.ReviewDeclined reviewDeclined) {
    return currentState.toBuilder().setStatus(LoanAppDomain.LoanAppDomainStatus.STATUS_DECLINED).setDeclineReason(reviewDeclined.getReason()).setLastUpdateTimestamp(reviewDeclined.getUpdateTimestamp()).build();
  }

}
