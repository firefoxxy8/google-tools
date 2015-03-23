package call.extension.script
import access.util.RLInternalAccessHelper
import call.extension.model.CallExtensionMigrationLog
import com.google.api.ads.adwords.axis.v201406.cm.CampaignFeed
import com.google.api.ads.adwords.axis.v201406.cm.Feed
import com.google.api.ads.adwords.axis.v201406.cm.FeedItem
import com.google.api.ads.adwords.lib.client.AdWordsSession
import com.google.common.base.Stopwatch
import feed.CampaignFeedAction
import feed.FeedAction
import feed.FeedFinder
import feed.FeedItemAction
import google.support.AdWordsSessionRLBuilder
import groovy.sql.Sql
import groovyx.gpars.GParsPool
import model.RLPlatform

/**
  * Copy Right 2014 of Reach Local Inc.,
 */
RLInternalAccessHelper accessHelper = RLInternalAccessHelper.instance
AdWordsSessionRLBuilder sessionBuilder = new AdWordsSessionRLBuilder(_accessHelper: accessHelper)
Sql sqlToLog = accessHelper.devLocalSql
def previousRunWpcIds = getPreviousRunWpcIds(sqlToLog)

FeedFinder feedFinder = new FeedFinder()

queryWPCsWithCredential = """
select distinct(wpc.idWebPublisherCampaign) wpcId, wpc.wpc_publisher_identifier publisherCampaignId, master.pea_key1 devToken, master.pea_username masterUsername, wpea.pea_account_id clientCustomerId,
    countryCode.ISO country, pp.`pp_replacement_phone` phone
from WebPublisherCampaign wpc
    join SubCampaign sc on wpc.SubCampaign_idSubCampaign_FK = sc.idSubCampaign
    join WebPublisherExternalAccount wpea on wpc.WebPublisherExternalAccount_idWebPublisherExternalAccount_FK = wpea.idWebPublisherExternalAccount
--     join Advertiser adv on adv.`idAdvertiser` = wpea.`Advertiser_idAdvertiser_FK`
    join WebPublisherExternalAccount master on wpea.WebPublisherExternalAccount_idWebPublisherExternalAccount_par_FK = master.idWebPublisherExternalAccount
    join Campaign c on c.`idCampaign` = sc.`Campaign_idCampaign_FK`
    join Page p on c.`idCampaign`=p.Campaign_idCampaign_FK
    join CountryCode countryCode on p.`page_company_country` = countryCode.`UN_letter`
    join PhoneProvision pp on wpc.`idWebPublisherCampaign` = pp.`WebPublisherCampaign_idWebPublisherCampaign_FK`
    join BotCommand bc on wpc.`idWebPublisherCampaign` = bc.`WebPublisherCampaign_idWebPublisherCampaign_FK`
where wpea.WebPublisher_idWebPublisher_FK in (1, 27)
    and wpc.wpc_publisher_identifier is not NULL
    and master.pea_account_type = 'master'
--     and wpc.wpc_end_date is not null
    and wpc.`LocalBusinessAdSetting_idLocalBusinessAdSetting_FK` in (2, 6)
    and bc.time_start > '2014-06-01'
    and bc.`status_message` like '%Failed adding phone extension%'
order by clientCustomerId ASC;
"""
def stopwatch = new Stopwatch()
stopwatch.start()
['GBR', 'CAN', 'JPN', 'EUR', 'AUS'].each { PLATFORM ->  //'GBR', 'CAN', 'JPN', 'EUR', 'AUS', 'USA'
    sqlToGetWPC = accessHelper.getAnalyticSql(PLATFORM)
    def allWpcsWithCallExtProblem = []
    sqlToGetWPC.eachRow(queryWPCsWithCredential) { row ->
        allWpcsWithCallExtProblem << new CallExtensionMigrationLog(platform: RLPlatform.fromValue(PLATFORM), wpcId:row.wpcId, publisherCampaignId:row.publisherCampaignId,
                    clientCustomerId:row.clientCustomerId, devToken:row.devToken, masterUsername: row.masterUsername,
                    countryCode:row.country, phoneNumber:row.phone, created: new Date())
    }
    allWpcsWithCallExtProblem.removeAll { wpc -> previousRunWpcIds[PLATFORM]?.contains(wpc.wpcId)}

    def allWpcGrpByAcct = allWpcsWithCallExtProblem.groupBy {it.clientCustomerId}

    GParsPool.withPool(10) {
    allWpcGrpByAcct.each { acctId, wpcsWithCallExt ->
           try {
               CallExtensionMigrationLog wpcWithCallExt = wpcsWithCallExt.get(0)
               println "masterUsername: ${wpcWithCallExt.masterUsername}, clientCustomerId: ${wpcWithCallExt.clientCustomerId}, devToken: ${wpcWithCallExt.devToken}"

               AdWordsSession session = sessionBuilder.buildAdWordsSessionWithAuthInfo(masterUsername: wpcWithCallExt.masterUsername, externalAccountId: wpcWithCallExt.clientCustomerId, devToken: wpcWithCallExt.devToken)
               Feed feedToRemove = findDeprecatedCEFeed(session, feedFinder)
               if (!feedToRemove) {
                   println "No deprecated CE found for ${acctId}"
               } else {
                   Feed removedFeed = FeedAction.removeFeed(session, feedToRemove)
                   println "Removed id:${removedFeed.id} ${removedFeed.name + " " + removedFeed.status}"

                   // only do below actions if there was BAD/old format CallExt.
                   Feed goodCEFeed = findGoodCEFeed(session, feedFinder)
                   if(!goodCEFeed)
                       goodCEFeed = FeedAction.addCallExtFeedAndAttributeMapping(session)

                   wpcsWithCallExt.each { CallExtensionMigrationLog callExt ->
                       String errorMessage = ""
                       try {
                           def campaignId = Long.valueOf(callExt.publisherCampaignId)
                           CampaignFeed[] callExtCampaignFeeds = feedFinder.getCampaignCallExtFeedsWith(session, campaignId)
                           if (callExtCampaignFeeds) {
                               callExtCampaignFeeds.each { callExtCampaignFeed ->
                                   CampaignFeedAction.removeCallExtCampaignFeed(session, callExtCampaignFeed.feedId, campaignId)
                                   println "Existing CE campfeed found, delete for campId:${campaignId}, feedId:${callExtCampaignFeed.feedId}"
                               }
                           }

                           FeedItem newCallExtFeedItem = FeedItemAction.addCallExtFeedItem(session, goodCEFeed, callExt.phoneNumber.replaceAll("#", ""), callExt.countryCode)
                           println "${callExt.wpcId} added Call Extension in google, id: ${newCallExtFeedItem.feedItemId}"
                           CampaignFeedAction.addCallExtCampaignFeed(session, goodCEFeed.id, campaignId, newCallExtFeedItem.feedItemId)
                       } catch (ex) {
                           println "Exception when add feedItem and campaignFeed"
                           ex.printStackTrace()
                           errorMessage = ex.message
                       } finally {
                           callExt.errorMessage = errorMessage
                           saveMigrationLog(sqlToLog, callExt)
                       }
                   }
               }
           } catch (ex) {
               println "Exception when removing old CE feed and creating new CE feed"
               ex.printStackTrace()
           }
        }
        }
}
stopwatch.stop()
println "${new Date()} it took ${stopwatch.elapsed(java.util.concurrent.TimeUnit.SECONDS)} seconds"

private void saveMigrationLog(Sql sqlToLog, CallExtensionMigrationLog migrationLog) {
    Sql sql = sqlToLog
    sql.executeInsert("insert into CallExtensionMigrationLog (platform, wpcId, publisher_campaign_id, phone_number, country_code, call_extension_id, error_message, client_customer_id, master_username, dev_token, created) " +
            " values (?,?,?,?,?,?,?,?,?,?,?)",
            [ migrationLog.platform.name(), migrationLog.wpcId, migrationLog.publisherCampaignId, migrationLog.phoneNumber, migrationLog.countryCode, migrationLog.callExtId, migrationLog.errorMessage,
              migrationLog.clientCustomerId, migrationLog.masterUsername, migrationLog.devToken, migrationLog.created])
}

Feed findDeprecatedCEFeed(session, FeedFinder feedFinder) {
    Feed[] feeds = feedFinder.getActiveUSERFeedsWith(session)
    feeds.find { it.name.contains("CallExt") && it.attributes.size() > 4 }
}

Feed findGoodCEFeed(session, FeedFinder feedFinder) {
    Feed[] feeds = feedFinder.getActiveUSERFeedsWith(session)
    feeds.find { it.name.contains("CallExt") && it.attributes.size() == 4 }
}

def getPreviousRunWpcIds(sqlToLog) {
    def platDryRunWpcIds = [:]
    def dryRunWpcIds
    sqlToLog.eachRow("select platform, wpcId from CallExtensionMigrationLog") {
        dryRunWpcIds = platDryRunWpcIds[it.platform]
        if(!dryRunWpcIds) {
            dryRunWpcIds = []
            platDryRunWpcIds[it.platform] = dryRunWpcIds
        }
        dryRunWpcIds << (long) it.wpcId
    }
    platDryRunWpcIds
}