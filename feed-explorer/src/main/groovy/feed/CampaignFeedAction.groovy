package feed
import com.google.api.ads.adwords.axis.factory.AdWordsServices
import com.google.api.ads.adwords.axis.v201406.cm.*
import com.google.api.ads.adwords.lib.client.AdWordsSession
import feed.model.PlaceholderType
/**
 * Copy Right 2014 of Reach Local Inc.,
 */
class CampaignFeedAction {

    static CampaignFeed addCallExtCampaignFeed(AdWordsSession session, Long feedId, Long campaignId, Long feedItemId) {
        CampaignFeed cf = new CampaignFeed(feedId: feedId, campaignId: campaignId, matchingFunction: createFeedItemIdFunction(feedItemId), placeholderTypes: [PlaceholderType.CALL.value].toArray(int[]))
        CampaignFeedOperation[] campaignFeedOperations = [new CampaignFeedOperation(operator: Operator.ADD, operand: cf)].toArray(CampaignFeedOperation[])

        CampaignFeedServiceInterface campaignFeedService = new AdWordsServices().get(session, CampaignFeedServiceInterface)
        campaignFeedService.mutate(campaignFeedOperations).getValue(0)
    }

    static CampaignFeed removeCallExtCampaignFeed(AdWordsSession session, Long feedId, Long campaignId) {
        CampaignFeed cf = new CampaignFeed(feedId: feedId, campaignId: campaignId, placeholderTypes: [PlaceholderType.CALL.value].toArray(int[]))
        CampaignFeedOperation[] campaignFeedOperations = [new CampaignFeedOperation(operator: Operator.REMOVE, operand: cf)].toArray(CampaignFeedOperation[])

        CampaignFeedServiceInterface campaignFeedService = new AdWordsServices().get(session, CampaignFeedServiceInterface)
        campaignFeedService.mutate(campaignFeedOperations).getValue(0)
    }

    static Function createFeedItemIdFunction(long feedItemId) {
        ConstantOperand constantOperand = new ConstantOperand();
        constantOperand.setLongValue(feedItemId);
        constantOperand.setType(ConstantOperandConstantType.LONG);
        FunctionArgumentOperand[] feedItemIdFunArgOperands = [constantOperand].toArray(FunctionArgumentOperand[])
        Function function = new Function(lhsOperand: new RequestContextOperand(contextType: RequestContextOperandContextType.FEED_ITEM_ID),
                                         operator: FunctionOperator.IN,
                                         rhsOperand: feedItemIdFunArgOperands)
        function
    }
}
