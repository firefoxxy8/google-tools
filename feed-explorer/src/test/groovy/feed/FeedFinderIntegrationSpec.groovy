package feed
import spock.lang.Specification
/**
 * Copy Right 2014 of Reach Local Inc.,
 */
class FeedFinderIntegrationSpec extends Specification {
//  QA US NX1
//    public static final long CLIENT_CUSTOMER_ID_GOOGLE = 7345602820L
//    public static final long WPEA_ID_REACHLOCAL = 279843L
//    public static final long PUBLISHER_CAMPAIGN_ID = 246437291L     // city, 245678891
//    public static final long PUBLISHER_ADGROUP_ID = 14599541047L     // city
//    public static final long PUBLISHER_CAMPAIGN_ID = 245678891L   // National
//analytics
    public static final long CLIENT_CUSTOMER_ID_GOOGLE = 4188471530L
    public static final long WPEA_ID_REACHLOCAL = 280375L
    public static final long PUBLISHER_CAMPAIGN_ID = 221062466L

    def finder = new FeedFinder()
    def feedItemFunc = new FeedItemAction()
//
//    def "should be able to find feed items from a given ad group feed with ad group id" () {
//        given:
//            def ccId = CLIENT_CUSTOMER_ID_GOOGLE
//            def adGroupId = PUBLISHER_ADGROUP_ID
//            def session = finder.buildAdWordSessionWithClientCustomerId(ccId)
//        when:
//            def adGroupFeeds = finder.findAdGroupLocationExtFeedsByPublisherAdGroupId(session, adGroupId)
//        then:
//            adGroupFeeds?.length > 0
//        when:
//            def feedItems = finder.findAllFeedItemsByFeedId(session, adGroupFeeds[0].feedId)
//        then:
//            feedItems?.length > 0
//            feedItems?.each { item -> printItem(item) }
//    }

//    def "should be able to delete feed items from a given customer feeds" () {
//        given: "a valid pea_account_id of google wpea in QA_US_NX1 "
//            def ccId = CLIENT_CUSTOMER_ID_GOOGLE
//            def session = finder.buildAdWordSessionWithClientCustomerId(ccId)
//            def customerFeeds = finder.findAllCustomerFeedLocationExtBy(ccId)
//        when:
//            def returnVal = feedItemFunc.deleteAllFeedItemsOfFeed(session, customerFeeds[0].feedId)
//        then:
//            returnVal
//    }
//
//    def "should be able to find feed items from a given customer feeds" () {
//        given: "a valid pea_account_id of google wpea in QA_US_NX1 "
//           def ccId = CLIENT_CUSTOMER_ID_GOOGLE
//           def session = finder.buildAdWordSessionWithClientCustomerId(ccId)
//           def customerFeeds = finder.findAllCustomerFeedLocationExtBy(ccId)
//        when:
//            def feedItems = finder.findAllFeedItemsByFeedId(session, customerFeeds[0].feedId)
//        then:
//            feedItems?.length > 0
//            feedItems?.each { item -> printItem(item) }
//    }

    def "should be able to find all customer feeds of a client customer id (google)" () {
        given: "a valid pea_account_id of google wpea in QA_US_NX1 "
            def ccId = CLIENT_CUSTOMER_ID_GOOGLE
        when:
            def customerFeeds = finder.findAllCustomerFeedLocationExtBy(ccId)
        then:
            customerFeeds?.length
    }

    def "should be able to find all campaign feeds of a publisher campaign id (google)" () {
        given: "a valid pea_account_id of google wpea in analytics/QA_US_NX1 "
            def campaignId = PUBLISHER_CAMPAIGN_ID
        when:
            def campaignFeeds = finder.findAllCampaignFeedsByPublisherCampaignId(campaignId)
        then:
            campaignFeeds?.length
    }

    def "should be able to find location extension campaign feeds of a publisher campaign id (google)" () {
        given: "a valid pea_account_id of google wpea in QA_US_NX1 "
            def campaignId = PUBLISHER_CAMPAIGN_ID
        when:
            def campaignFeeds = finder.findCampaignLocationExtFeedsByPublisherCampaignId(campaignId)
        then:
            campaignFeeds?.length
    }

    def "should be able to find all feeds of a client customer id (google)" () {
        given: "a valid pea_account_id of google wpea in QA_US_NX1 "
             def ccId = CLIENT_CUSTOMER_ID_GOOGLE
        when:
            def feeds = finder.findAllUserOriginFeedsByClientCustomerId(ccId)
        then:
            feeds?.length > 0
    }

    def "should be able to find all feeds of a wpea id (reachlocal)" () {
        given: "a valid pea_account_id of google wpea in QA_US_NX1 "
            def wpeaId = WPEA_ID_REACHLOCAL
        when:
            def feeds = finder.findAllUserOriginFeedsByWpeaId(wpeaId)
        then:
            feeds?.length > 0
    }

    def "should be able to build a adwords session from a client customer id (google)" () {
        given: "a valid pea_account_id of google wpea in QA_US_NX1 "
            def ccId = CLIENT_CUSTOMER_ID_GOOGLE
        when:
            def session = finder.buildAdWordSessionWithClientCustomerId(ccId)
        then:
            session
    }

    def "should be able to build a adwords session from a wpea id (reachlocal)" () {
        given: "a valid wpea_id in QA_US_NX1 "
            def wpeaId = WPEA_ID_REACHLOCAL
        when:
            def session = finder.buildAdWordSessionWithWpeaId(wpeaId)
        then:
            session
    }
}
