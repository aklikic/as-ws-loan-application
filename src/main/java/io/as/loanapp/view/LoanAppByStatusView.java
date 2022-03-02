package io.as.loanapp.view;

import com.akkaserverless.javasdk.view.View;
import com.akkaserverless.javasdk.view.ViewContext;
import com.google.protobuf.Any;
import io.as.loanapp.api.LoanAppApi;
import io.as.loanapp.domain.LoanAppDomain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// This class was initially generated based on the .proto definition by Akka Serverless tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class LoanAppByStatusView extends AbstractLoanAppByStatusView {

  private static Logger log = LoggerFactory.getLogger(LoanAppByStatusView.class);
  public LoanAppByStatusView(ViewContext context) {}

  @Override
  public LoanAppByStatusModel.LoanAppViewState emptyState() {
    return LoanAppByStatusModel.LoanAppViewState.getDefaultInstance();
  }

  @Override
  public View.UpdateEffect<LoanAppByStatusModel.LoanAppViewState> onSubmitted(
    LoanAppByStatusModel.LoanAppViewState state, LoanAppDomain.Submitted submitted) {
    log.info("onSubmitted: {}",submitted.getLoanAppId());
    LoanAppByStatusModel.LoanAppViewState newState = LoanAppByStatusModel.LoanAppViewState.newBuilder()
            .setLoanAppId(submitted.getLoanAppId())
            .setClientSsn(submitted.getClientSsn())
            .setStatus(LoanAppApi.LoanAppStatus.STATUS_WAITING_FOR_REVIEW)
            .setStatusId(LoanAppApi.LoanAppStatus.STATUS_WAITING_FOR_REVIEW.getNumber())
            .setLastUpdateTimestamp(submitted.getUpdateTimestamp())
            .build();
    return effects().updateState(newState);
  }

  @Override
  public View.UpdateEffect<LoanAppByStatusModel.LoanAppViewState> onReviewStarted(
    LoanAppByStatusModel.LoanAppViewState state, LoanAppDomain.ReviewStarted reviewStarted) {
    log.info("onReviewStarted: {}/{}",reviewStarted.getLoanAppId(),state.getStatus());
    LoanAppByStatusModel.LoanAppViewState newState = state.toBuilder()
            .setStatus(LoanAppApi.LoanAppStatus.STATUS_IN_REVIEW)
            .setStatusId(LoanAppApi.LoanAppStatus.STATUS_IN_REVIEW.getNumber())
            .setLastUpdateTimestamp(reviewStarted.getUpdateTimestamp())
            .build();
    return effects().updateState(newState);
  }
  @Override
  public View.UpdateEffect<LoanAppByStatusModel.LoanAppViewState> onReviewApproved(
    LoanAppByStatusModel.LoanAppViewState state, LoanAppDomain.ReviewApproved reviewApproved) {
    LoanAppByStatusModel.LoanAppViewState newState = state.toBuilder()
            .setStatus(LoanAppApi.LoanAppStatus.STATUS_APPROVED)
            .setStatusId(LoanAppApi.LoanAppStatus.STATUS_APPROVED.getNumber())
            .setLastUpdateTimestamp(reviewApproved.getUpdateTimestamp())
            .build();
    return effects().updateState(newState);
  }
  @Override
  public View.UpdateEffect<LoanAppByStatusModel.LoanAppViewState> onReviewDeclined(
    LoanAppByStatusModel.LoanAppViewState state, LoanAppDomain.ReviewDeclined reviewDeclined) {
    LoanAppByStatusModel.LoanAppViewState newState = state.toBuilder()
            .setStatus(LoanAppApi.LoanAppStatus.STATUS_DECLINED)
            .setStatusId(LoanAppApi.LoanAppStatus.STATUS_DECLINED.getNumber())
            .setLastUpdateTimestamp(reviewDeclined.getUpdateTimestamp())
            .build();
    return effects().updateState(newState);
  }
  @Override
  public View.UpdateEffect<LoanAppByStatusModel.LoanAppViewState> ignoreOtherEvents(
    LoanAppByStatusModel.LoanAppViewState state, Any any) {
    return effects().ignore();
  }
}

