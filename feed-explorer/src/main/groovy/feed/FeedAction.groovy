package feed

import com.google.api.ads.adwords.axis.factory.AdWordsServices
import com.google.api.ads.adwords.axis.v201406.cm.*
import com.google.api.ads.adwords.lib.client.AdWordsSession
import feed.model.CallExtensionField
import feed.model.PlaceholderType

/**
 * Copy Right 2014 of Reach Local Inc.,
 */
class FeedAction {
    static Feed removeFeed(AdWordsSession session, Feed feed) {
        FeedServiceInterface feedService = new AdWordsServices().get(session, FeedServiceInterface)
        FeedOperation[] operations = [new FeedOperation(operator: Operator.REMOVE, operand: feed)].toArray(FeedOperation[])
        def feedReturnValue = feedService.mutate(operations)
        feedReturnValue.getValue(0)
    }

    static Feed addCallExtFeedAndAttributeMapping(AdWordsSession session) {
        FeedServiceInterface feedService = new AdWordsServices().get(session, FeedServiceInterface)
        Feed feed = createCallExtFeed()
        FeedOperation[] operations = [new FeedOperation(operator: Operator.ADD, operand: feed)].toArray(FeedOperation[])
        def feedReturnValue = feedService.mutate(operations)
        feed = feedReturnValue.getValue(0)

        createAndProvisionCallExtFeedMapping(session, feed)  // have to use returned feed (from feedService invocation) so that there is id in it
        feed
    }

    static void createAndProvisionCallExtFeedMapping(session, Feed feed) {
        FeedMapping callExtFeedMapping = new FeedMapping(feedId: feed.id, placeholderType: PlaceholderType.CALL.value, attributeFieldMappings: createCallFeedAttributeMappings(feed))
        FeedMappingServiceInterface feedMappingService = new AdWordsServices().get(session, FeedMappingServiceInterface)
        FeedMappingOperation[] feedMappingOperations = [new FeedMappingOperation(operator: Operator.ADD, operand: callExtFeedMapping)].toArray(FeedMappingOperation[])
        feedMappingService.mutate(feedMappingOperations)
    }

    private static Feed createCallExtFeed() {
        new Feed(name: "Default CallExt Feed", origin: FeedOrigin.USER, attributes: createCallFeedAttributes())
    }

    private static FeedAttribute[] createCallFeedAttributes() {
        FeedAttribute[] feedAttributes = new FeedAttribute[4];
        feedAttributes[0] = new FeedAttribute();
        feedAttributes[0].setType(FeedAttributeType.STRING);
        feedAttributes[0].setName(CallExtensionField.PHONE_NUMBER.getAttribute());

        feedAttributes[1] = new FeedAttribute();
        feedAttributes[1].setType(FeedAttributeType.STRING);
        feedAttributes[1].setName(CallExtensionField.COUNTRY_CODE.getAttribute());

        feedAttributes[2] = new FeedAttribute();
        feedAttributes[2].setType(FeedAttributeType.BOOLEAN);
        feedAttributes[2].setName(CallExtensionField.TRACKED.getAttribute());

        feedAttributes[3] = new FeedAttribute();
        feedAttributes[3].setType(FeedAttributeType.BOOLEAN);
        feedAttributes[3].setName(CallExtensionField.ONLY.getAttribute());

        return feedAttributes;
    }

    private static AttributeFieldMapping[] createCallFeedAttributeMappings(Feed feed) throws IllegalArgumentException {
        List<AttributeFieldMapping> attributeFieldMappingList = new ArrayList<AttributeFieldMapping>();

        for (FeedAttribute fa : feed.getAttributes()) {
            CallExtensionField callExtensionField = CallExtensionField.fromAttribute(fa.getName());
            AttributeFieldMapping attributeFieldMapping = new AttributeFieldMapping();
            attributeFieldMapping.setFieldId(callExtensionField.getValue());
            attributeFieldMapping.setFeedAttributeId(fa.getId());
            attributeFieldMappingList.add(attributeFieldMapping);
        }

        return attributeFieldMappingList.toArray(new AttributeFieldMapping[attributeFieldMappingList.size()]);
    }
}
