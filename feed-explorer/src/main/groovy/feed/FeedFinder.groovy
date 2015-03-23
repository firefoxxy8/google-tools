package feed

import com.google.api.ads.adwords.axis.factory.AdWordsServices
import com.google.api.ads.adwords.axis.v201406.cm.*
import com.google.api.ads.adwords.lib.client.AdWordsSession
import google.support.AdWordsSessionRLBuilder
import feed.model.*

/**
 * Copy Right 2014 of Reach Local Inc.,
 */
class FeedFinder extends AdWordsSessionRLBuilder {

    Feed[] findAllUserOriginFeedsByWpeaId(Long wpeaId){
        def session = buildAdWordSessionWithWpeaId(wpeaId)

        getUSERFeedsWith(session)
    }

    Feed[] findAllUserOriginFeedsByClientCustomerId(Long clientCustomerId){
        def session = buildAdWordSessionWithClientCustomerId(clientCustomerId)

        getUSERFeedsWith(session)
    }

    CustomerFeed[] findAllCustomerFeedLocationExtBy(Long clientCustomerId){
        def session = buildAdWordSessionWithClientCustomerId(clientCustomerId)

        getCustomerFeedsLocationExtWith(session)
    }

    CampaignFeed[] findAllCampaignFeedsByPublisherCampaignId(Long publisherCampaignId) {
        def session = buildAdWordSessionWithPublisherCampaignId(publisherCampaignId)

        getCampaignFeedsWith(session, publisherCampaignId)
    }

    CampaignFeed[] findCampaignLocationExtFeedsByPublisherCampaignId(Long publisherCampaignId) {
        def session = buildAdWordSessionWithPublisherCampaignId(publisherCampaignId)

        getCampaignLocationExtFeedsWith(session, publisherCampaignId)
    }

    AdGroupFeed[] findAdGroupLocationExtFeedsByPublisherAdGroupId(AdWordsSession session, Long publisherAdGroupId) {

        AdGroupFeedServiceInterface AdGroupFeedService = new AdWordsServices().get(session, AdGroupFeedServiceInterface)
        AdGroupFeedPage adGroupFeedPage = AdGroupFeedService.get(new Selector(fields: AdGroupFeedSelectableField.values().collect {it.value}.toArray(),
                predicates: [
                        new Predicate(AdGroupFeedSelectableField.STATUS.value,
                                PredicateOperator.EQUALS,
                                ["ENABLED"] as String[]),
                        new Predicate(AdGroupFeedSelectableField.PLACEHOLDER_TYPES.value,
                                PredicateOperator.EQUALS,
                                ["7"] as String[]),
                        new Predicate(AdGroupFeedSelectableField.AD_GROUP_ID.value,
                                PredicateOperator.EQUALS,
                                [publisherAdGroupId.toString()] as String[])
                ].toArray(Predicate[])
        ) // close for Selector
        )
        adGroupFeedPage.entries
    }

    FeedItem[] findAllFeedItemsByFeedId(AdWordsSession session, Long feedId) {
        FeedItemServiceInterface feedItemService = new AdWordsServices().get(session, FeedItemServiceInterface)
        FeedItemPage FeedItemPage = feedItemService.get(new Selector(fields: FeedItemSelectableField.values().collect {it.value}.toArray(),
                                                                predicates: [new Predicate(FeedItemFilterableField.FEED_ID.value,
                                                                                    PredicateOperator.EQUALS,
                                                                                    [feedId.toString()] as String[]),
                                                                             new Predicate(CampaignFeedSelectableField.STATUS.value,
                                                                                     PredicateOperator.EQUALS,
                                                                                     ["ENABLED"] as String[])
                                                                ].toArray(Predicate[])
                                                            ) // close for Selector
                                                        ) // close for FeedService.get
        FeedItemPage.entries
    }

    FeedItem[] findFeedItemById(AdWordsSession session, Long feedItemId) {
        FeedItemServiceInterface feedItemService = new AdWordsServices().get(session, FeedItemServiceInterface)
        FeedItemPage FeedItemPage = feedItemService.get(new Selector(fields: FeedItemSelectableField.values().collect {it.value}.toArray(),
                predicates: [new Predicate(FeedItemFilterableField.FEED_ITEM_ID.value,
                        PredicateOperator.EQUALS,
                        [feedItemId.toString()] as String[]),
                             new Predicate(CampaignFeedSelectableField.STATUS.value,
                                     PredicateOperator.EQUALS,
                                     ["ENABLED"] as String[])
                ].toArray(Predicate[])
        ) // close for Selector
        ) // close for FeedService.get
        FeedItemPage.entries
    }

    FeedMapping[] getFeedMappingOf(AdWordsSession session, Feed feed) {
        FeedMappingServiceInterface feedMappingService = new AdWordsServices().get(session, FeedMappingServiceInterface)
        FeedMappingPage feedMappingPage = feedMappingService.get(new Selector(fields: FeedMappingSelectableField.values().collect {it.value}.toArray(),
                predicates: [new Predicate(FeedMappingSelectableField.FEED_ID.value,
                                     PredicateOperator.EQUALS,
                                     [feed.id.toString()] as String[]),
                             new Predicate(FeedMappingSelectableField.STATUS.value,
                                     PredicateOperator.EQUALS,
                                     ["ENABLED"] as String[])
                            ].toArray(Predicate[])
                ) // close for Selector
        ) // close for FeedService.get
        feedMappingPage.entries
    }

    CampaignFeed[] getCampaignLocationExtFeedsWith(AdWordsSession session, Long publisherCampaignId) {
        CampaignFeedServiceInterface campaignFeedService = new AdWordsServices().get(session, CampaignFeedServiceInterface)
        CampaignFeedPage campaignFeedPage = campaignFeedService.get(new Selector(fields: CampaignFeedSelectableField.values().collect {it.value}.toArray(),
                predicates: [
//                        new Predicate(CampaignFeedSelectableField.STATUS.value,
//                                PredicateOperator.EQUALS,
//                                ["ENABLED"] as String[]),
                        new Predicate(CampaignFeedSelectableField.PLACEHOLDER_TYPES.value,
                                PredicateOperator.EQUALS,
                                ["7"] as String[]),
                        new Predicate(CampaignFeedSelectableField.CAMPAIGN_ID.value,
                                PredicateOperator.EQUALS,
                                [publisherCampaignId.toString()] as String[])
                ].toArray()
        ) // close for Selector
        ) // close for FeedService.get
        campaignFeedPage.entries
    }

    CampaignFeed[] getCampaignCallExtFeedsWith(AdWordsSession session, Long publisherCampaignId) {
        CampaignFeedServiceInterface campaignFeedService = new AdWordsServices().get(session, CampaignFeedServiceInterface)
        CampaignFeedPage campaignFeedPage = campaignFeedService.get(new Selector(fields: CampaignFeedSelectableField.values().collect {it.value}.toArray(),
                predicates: [
                new Predicate(CampaignFeedSelectableField.STATUS.value,
                        PredicateOperator.EQUALS,
                        ["ENABLED"] as String[]),
                new Predicate(CampaignFeedSelectableField.PLACEHOLDER_TYPES.value,
                        PredicateOperator.EQUALS,
                        ["2"] as String[]),
                new Predicate(CampaignFeedSelectableField.CAMPAIGN_ID.value,
                        PredicateOperator.EQUALS,
                        [publisherCampaignId.toString()] as String[])
                                ].toArray()
                ) // close for Selector
        ) // close for FeedService.get
        campaignFeedPage.entries
    }

    CampaignFeed[] getCampaignSiteLinkExtFeedsWith(AdWordsSession session, Long publisherCampaignId) {
        CampaignFeedServiceInterface campaignFeedService = new AdWordsServices().get(session, CampaignFeedServiceInterface)
        CampaignFeedPage campaignFeedPage = campaignFeedService.get(new Selector(fields: CampaignFeedSelectableField.values().collect {it.value}.toArray(),
                predicates: [
                        new Predicate(CampaignFeedSelectableField.STATUS.value,
                                PredicateOperator.EQUALS,
                                ["ENABLED"] as String[]),
                        new Predicate(CampaignFeedSelectableField.PLACEHOLDER_TYPES.value,
                                PredicateOperator.EQUALS,
                                ["1"] as String[]),
                        new Predicate(CampaignFeedSelectableField.CAMPAIGN_ID.value,
                                PredicateOperator.EQUALS,
                                [publisherCampaignId.toString()] as String[])
                                        ].toArray()
                                ) // close for Selector
                ) // close for FeedService.get
        campaignFeedPage.entries
    }

    CampaignFeed[] getCampaignFeedsWith(AdWordsSession session, Long publisherCampaignId) {
        CampaignFeedServiceInterface campaignFeedService = new AdWordsServices().get(session, CampaignFeedServiceInterface)
        CampaignFeedPage campaignFeedPage = campaignFeedService.get(new Selector(fields: CampaignFeedSelectableField.values().collect {it.value}.toArray(),
                                                                                predicates: [
                                                                                        new Predicate(CampaignFeedSelectableField.STATUS.value,
                                                                                                PredicateOperator.EQUALS,
                                                                                                ["ENABLED"] as String[]),
                                                                                        new Predicate(CampaignFeedSelectableField.CAMPAIGN_ID.value,
                                                                                                PredicateOperator.EQUALS,
                                                                                                [publisherCampaignId.toString()] as String[])
                                                                                ].toArray(Predicate[])
                                                                        ) // close for Selector
                                                                    ) // close for FeedService.get
        campaignFeedPage.entries
    }
    CampaignFeed[] getAllCampaignFeedsOfAccount(AdWordsSession session) {
        CampaignFeedServiceInterface campaignFeedService = new AdWordsServices().get(session, CampaignFeedServiceInterface)
        CampaignFeedPage campaignFeedPage = campaignFeedService.get(new Selector(fields: CampaignFeedSelectableField.values().collect {it.value}.toArray(),
                predicates: [new Predicate(CampaignFeedSelectableField.STATUS.value,
                        PredicateOperator.EQUALS,
                        ["ENABLED"] as String[])].toArray(Predicate[])
            ) // close for Selector
        ) // close for FeedService.get
        campaignFeedPage.entries
    }

    CampaignFeed[] getAllCampaignFeedCallExtensions(AdWordsSession session) {
        CampaignFeedServiceInterface campaignFeedService = new AdWordsServices().get(session, CampaignFeedServiceInterface)
        CampaignFeedPage campaignFeedPage = campaignFeedService.get(new Selector(fields: CampaignFeedSelectableField.values().collect {it.value}.toArray(),
                predicates: [new Predicate(CampaignFeedSelectableField.STATUS.value,
                            PredicateOperator.EQUALS,
                            ["ENABLED"] as String[]),
                             new Predicate(CampaignFeedSelectableField.PLACEHOLDER_TYPES.value,
                                     PredicateOperator.EQUALS,
                                     ["2"] as String[])].toArray(Predicate[])
            ) // close for Selector
        ) // close for FeedService.get
        campaignFeedPage.entries
    }

    CustomerFeed[] getCustomerFeedsLocationExtWith(AdWordsSession session) {
        CustomerFeedServiceInterface customerFeedService = new AdWordsServices().get(session, CustomerFeedServiceInterface)
        CustomerFeedPage customerFeedPage = customerFeedService.get(new Selector(fields: CustomerFeedSelectableField.values().collect {it.value}.toArray(),
                                                        predicates: [new Predicate(CustomerFeedSelectableField.STATUS.value,
                                                                                    PredicateOperator.EQUALS,
                                                                                    ["ENABLED"] as String[]),
                                                                     new Predicate(CustomerFeedSelectableField.PLACEHOLDER_TYPES.value,
                                                                                     PredicateOperator.EQUALS,
                                                                                     ["7"] as String[])].toArray(Predicate[])
                                                                                ) // close for Selector
                                                                    ) // close for FeedService.get
        customerFeedPage.entries
    }

    Feed[] getUSERFeedsWith(AdWordsSession session) {
        FeedServiceInterface feedService = new AdWordsServices().get(session, FeedServiceInterface)
        FeedPage feedPage = feedService.get(new Selector(fields: FeedSelectableField.values().collect {it.value}.toArray(),
                                                        predicates: [new Predicate(FeedSelectableField.ORIGIN.value,
                                                                        PredicateOperator.EQUALS,
                                                                        ["USER"] as String[])].toArray(Predicate[])
                                                        ) // close for Selector
                                            ) // close for FeedService.get
        feedPage.entries
    }

    Feed[] getActiveUSERFeedsWith(AdWordsSession session) {
        FeedServiceInterface feedService = new AdWordsServices().get(session, FeedServiceInterface)
        FeedPage feedPage = feedService.get(new Selector(fields: FeedSelectableField.values().collect {it.value}.toArray(),
                predicates: [new Predicate(FeedSelectableField.ORIGIN.value,
                                    PredicateOperator.EQUALS,
                                    ["USER"] as String[]),
                             new Predicate(FeedSelectableField.STATUS.value,
                                     PredicateOperator.EQUALS,
                                     ["ENABLED"] as String[])].toArray(Predicate[])
        ) // close for Selector
        ) // close for FeedService.get
        feedPage.entries
    }

    def static main(args) {
        def finder = new FeedFinder()
        def session = finder.buildAdWordSessionWithClientCustomerId(5723533538L)

        assert session
    }

}
