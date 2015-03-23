package feed

import com.google.api.ads.adwords.axis.factory.AdWordsServices
import com.google.api.ads.adwords.axis.v201406.cm.*
import com.google.api.ads.adwords.lib.client.AdWordsSession
import feed.model.CallExtensionField
import feed.model.FeedItemFilterableField
import feed.model.FeedItemSelectableField

import java.rmi.RemoteException

/**
 * Copy Right 2014 of Reach Local Inc.,
 */
class FeedItemAction {

    def deleteFeedItem(AdWordsSession session, FeedItem feedItem) {
        FeedItemServiceInterface feedItemService = new AdWordsServices().get(session, FeedItemServiceInterface)
        def returnVal = feedItemService.mutate([new FeedItemOperation(operand: feedItem, operator: Operator.REMOVE)] as FeedItemOperation[])
        returnVal.value
    }

    def deleteAllFeedItemsOfFeed(AdWordsSession session, Long feedId) {
        FeedItemServiceInterface feedItemService = new AdWordsServices().get(session, FeedItemServiceInterface)
        FeedItemPage FeedItemPage = feedItemService.get(new Selector(fields: FeedItemSelectableField.values().collect{it.value }.toArray(),
                                                                        predicates: [new Predicate(FeedItemFilterableField.FEED_ID.value,
                                                                                        PredicateOperator.EQUALS,
                                                                                        [feedId.toString()] as String[])].toArray()
                                                                                            ) // close for Selector
                                                                                        ) // close for FeedService.get
        def retVals = []
        FeedItemPage.entries.each { item ->
            def retVal = feedItemService.mutate([new FeedItemOperation(operand: item, operator: Operator.REMOVE)] as FeedItemOperation[])
            retVals << retVal
        }
        retVals
    }

    static FeedItem addCallExtFeedItem(AdWordsSession session, Feed feed, String phoneNumber, String countryCode) {

        FeedItemOperation[] feedItemOperations = createCallExtFeedItemOperations(feed, phoneNumber, countryCode);
        FeedItemServiceInterface feedItemService = new AdWordsServices().get(session, FeedItemServiceInterface.class);
        try {
            println("Creating FeedItems.");
            FeedItemReturnValue feedItemReturnValue = feedItemService.mutate(feedItemOperations);
            return feedItemReturnValue.getValue(0);
        } catch (ApiException e) {
            println("API errors encountered while provisioning feedItems for Client {}. {}",
                    session.getClientCustomerId(), e.message);
            throw e;
        } catch (RemoteException e) {
            throw new RuntimeException("Failed to provision feedItems.", e);
        }
    }

    static FeedItemOperation[] createCallExtFeedItemOperations(Feed feed, String phoneNumber, String countryCode) {
        FeedItemAttributeValue phoneNumberAttributeValue = new FeedItemAttributeValue(feedAttributeId: CallExtensionField.PHONE_NUMBER.value , stringValue: phoneNumber)
        FeedItemAttributeValue countryCodeAttributeValue = new FeedItemAttributeValue(feedAttributeId: CallExtensionField.COUNTRY_CODE.value , stringValue: countryCode)
        FeedItemAttributeValue trackedAttributeValue = new FeedItemAttributeValue(feedAttributeId: CallExtensionField.TRACKED.value , booleanValue: Boolean.FALSE)
        FeedItemAttributeValue displayPhoneOnlyAttributeValue = new FeedItemAttributeValue(feedAttributeId: CallExtensionField.ONLY.value , booleanValue: Boolean.FALSE)
        FeedItemAttributeValue[] callExtFeedAttributeValues = [phoneNumberAttributeValue, countryCodeAttributeValue, trackedAttributeValue, displayPhoneOnlyAttributeValue].toArray(FeedItemAttributeValue[])

        FeedItem callExtFeedItem = new FeedItem(feedId: feed.id, attributeValues: callExtFeedAttributeValues)

        FeedItemOperation callExtFeedItemOperation = new FeedItemOperation(operator: Operator.ADD, operand: callExtFeedItem)
        [callExtFeedItemOperation].toArray(FeedItemOperation[])
    }
}
