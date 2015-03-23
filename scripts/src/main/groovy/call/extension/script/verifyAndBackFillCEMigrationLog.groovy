package call.extension.script
import access.util.RLInternalAccessHelper
import call.extension.model.CallExtensionMigrationLog
import com.google.api.ads.adwords.axis.v201406.cm.CampaignFeed
import com.google.api.ads.adwords.axis.v201406.cm.ConstantOperand
import com.google.api.ads.adwords.axis.v201406.cm.FeedItem
import com.google.api.ads.adwords.lib.client.AdWordsSession
import com.google.common.base.Stopwatch
import feed.FeedFinder
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
def previousRunLogs = getPreviousRunLogs(sqlToLog)

FeedFinder feedFinder = new FeedFinder()

def stopwatch = new Stopwatch()
stopwatch.start()

def allWpcGrpByAcct = previousRunLogs.groupBy {it.clientCustomerId}

GParsPool.withPool(10) {
allWpcGrpByAcct.each { acctId, wpcsWithCallExt ->
       try {
           CallExtensionMigrationLog wpcWithCallExt = wpcsWithCallExt.get(0)
           println "masterUsername: ${wpcWithCallExt.masterUsername}, clientCustomerId: ${wpcWithCallExt.clientCustomerId}, devToken: ${wpcWithCallExt.devToken}"

           AdWordsSession session = sessionBuilder.buildAdWordsSessionWithAuthInfo(masterUsername: wpcWithCallExt.masterUsername, externalAccountId: wpcWithCallExt.clientCustomerId, devToken: wpcWithCallExt.devToken)

           wpcsWithCallExt.each { CallExtensionMigrationLog callExt ->
               String errorMessage = ""
               try {
                   def campaignId = Long.valueOf(callExt.publisherCampaignId)
                   println "CampaignId:${campaignId}"
                   CampaignFeed[] callExtCampaignFeeds = feedFinder.getCampaignCallExtFeedsWith(session, campaignId)
                   if(callExtCampaignFeeds && callExtCampaignFeeds.length == 1) {
                       def feedId = callExtCampaignFeeds[0].feedId
                       def feedItemId = (callExtCampaignFeeds[0].matchingFunction.getRhsOperand(0) as ConstantOperand[])[0].longValue
                       println "ONE Existing CE campfeed found, campId:${campaignId}, feedId:${feedId}, feedItemId:${feedItemId}"
                       FeedItem callExtItem = feedFinder.findFeedItemById(session, feedItemId)[0]
                       callExt.phoneNumber = callExtItem.getAttributeValues(0).stringValue
                       callExt.countryCode = callExtItem.getAttributeValues(1).stringValue
                       callExt.callExtId = feedItemId
                   } else {
                       errorMessage = "Can't find CE Campaign feed"
                       if(callExtCampaignFeeds)
                            println "Something wrong, there is ${callExtCampaignFeeds?.length} CallExtCampaignFeed, feedIds:${callExtCampaignFeeds?.collect{it.feedId}.join(",")}"
                       else
                            println "Can't find CE Campaign feed, campId:${campaignId}"
                   }
               } catch (ex) {
                   println "Exception when add feedItem and campaignFeed"
                   ex.printStackTrace()
                   errorMessage = ex.message
               } finally {
                   callExt.errorMessage = callExt.errorMessage? callExt.errorMessage << errorMessage : errorMessage
                   updateMigrationLog(sqlToLog, callExt)
               }
           }
       } catch (ex) {
           println "Exception when removing old CE feed and creating new CE feed"
           ex.printStackTrace()
       }
    }
}
stopwatch.stop()
println "${new Date()} it took ${stopwatch.elapsed(java.util.concurrent.TimeUnit.SECONDS)} seconds"

private void updateMigrationLog(Sql sqlToLog, CallExtensionMigrationLog migrationLog) {
    Sql sql = sqlToLog
    sql.executeUpdate("update CallExtensionMigrationLog set phone_number=?, country_code=?, call_extension_id=?, error_message=? where wpcId=? ",
            [ migrationLog.phoneNumber, migrationLog.countryCode, migrationLog.callExtId, migrationLog.errorMessage, migrationLog.wpcId])
}

def getPreviousRunLogs(sqlToLog) {
    def previousLogs = []
    sqlToLog.eachRow("select * from CallExtensionMigrationLog where phone_number is null and error_message = ''") { row ->
        previousLogs << new CallExtensionMigrationLog(platform: RLPlatform.fromValue(row.platform), wpcId:row.wpcId, publisherCampaignId:row.publisher_campaign_id,
                clientCustomerId:row.client_customer_id, devToken:row.dev_token, masterUsername: row.master_username,
                countryCode:row.country_code, phoneNumber:row.phone_number, created: row.created, errorMessage: row.error_message)
    }
    previousLogs
}