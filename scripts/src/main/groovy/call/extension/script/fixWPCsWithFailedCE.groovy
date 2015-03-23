package call.extension.script

import access.util.RLInternalAccessHelper
import call.extension.model.CallExtensionMigrationLog
import com.google.api.ads.adwords.axis.v201406.cm.*
import com.google.api.ads.adwords.lib.client.AdWordsSession
import com.google.common.base.Stopwatch
import feed.CampaignFeedAction
import feed.FeedAction
import feed.FeedFinder
import feed.FeedItemAction
import feed.model.CallExtensionField
import google.support.AdWordsSessionRLBuilder
import groovy.sql.Sql
import model.RLPlatform

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Copy Right 2014 of Reach Local Inc.,
 */
RLInternalAccessHelper accessHelper = RLInternalAccessHelper.instance
DateFormat df = new SimpleDateFormat("yyyy-MM-dd")
AdWordsSessionRLBuilder sessionBuilder = new AdWordsSessionRLBuilder(_accessHelper: accessHelper)
FeedFinder feedFinder = new FeedFinder()

Sql sqlToLog = accessHelper.devLocalSql
def previousRunWpcIds = getPreviousRunWpcIds(sqlToLog)

def queryWPCsWithCEProblem = """
select wpc.idWebPublisherCampaign wpcId, wpc.wpc_publisher_identifier publisherCampaignId, wpea.pea_account_id clientCustomerId,
master.pea_key1 devToken, master.pea_username masterUsername,
countryCode.ISO country, pp.pp_replacement_phone phone,
bc.time_request_start, bc.time_start, bc.time_end,
bct.type_name bc_type, bcs.status_name bc_status, status_message
from WebPublisherCampaign wpc
    join WebPublisherExternalAccount wpea on wpc.WebPublisherExternalAccount_idWebPublisherExternalAccount_FK = wpea.idWebPublisherExternalAccount
    join WebPublisherExternalAccount master on wpea.WebPublisherExternalAccount_idWebPublisherExternalAccount_par_FK = master.idWebPublisherExternalAccount
    join Advertiser adv on adv.`idAdvertiser` = wpea.`Advertiser_idAdvertiser_FK`
    join CountryCode countryCode on adv.`advertiser_country` = countryCode.`UN_letter`
    join PhoneProvision pp on wpc.`idWebPublisherCampaign` = pp.`WebPublisherCampaign_idWebPublisherCampaign_FK`
    join BotCommand bc on wpc.`idWebPublisherCampaign` = bc.`WebPublisherCampaign_idWebPublisherCampaign_FK`
    join BotCommandStatus bcs on bc.BotCommandStatus_idBotCommandStatus_FK = bcs.idBotCommandStatus
    join BotCommandType bct on bc.BotCommandType_idBotCommandType_FK = bct.idBotCommandType
where wpea.WebPublisher_idWebPublisher_FK in (1, 27)
    and wpc.wpc_publisher_identifier is not NULL
    and wpc.`LocalBusinessAdSetting_idLocalBusinessAdSetting_FK` in (2, 6)
    and bc.time_start > ${df.parse(args[0])}
    and bc.`status_message` like '%Unexpected FeedAttribute%'
order by bc.time_start DESC;
"""
File dupCEError = new File("/Users/lawrence.dai/GoogleCEDupError/dupCEError-${df.format(new Date())}.log")
def stopwatch = new Stopwatch()
stopwatch.start()

['GBR', 'CAN', 'JPN', 'EUR', 'AUS', 'USA'].each { PLATFORM ->  //'GBR', 'CAN', 'JPN', 'EUR', 'AUS', 'USA'
    sqlToGetWPC = accessHelper.getAnalyticSql(PLATFORM)
    def allWpcsWithCallExtProblem = []
    sqlToGetWPC.eachRow(queryWPCsWithCEProblem) { row ->
        allWpcsWithCallExtProblem << new CallExtensionMigrationLog(platform: RLPlatform.fromValue(PLATFORM), wpcId: row.wpcId, publisherCampaignId: row.publisherCampaignId,
                clientCustomerId: row.clientCustomerId, devToken:row.devToken, masterUsername: row.masterUsername,
                phoneNumber:row.phone, countryCode:row.country,
                commandType:row.bc_type, commandStatus:row.bc_status, commandStatusMessage:row.status_message,
                timeStart:row.time_start, timeEnd:row.time_end)
    }
    dupCEError.withOutputStream { os->
        allWpcsWithCallExtProblem.findAll { wpc -> previousRunWpcIds[PLATFORM]?.contains(wpc.wpcId)}.each { CallExtensionMigrationLog it->
            os << "DUP found, ${it.platform}: ${it.wpcId}\n"
        }
    }
    allWpcsWithCallExtProblem.removeAll { wpc -> previousRunWpcIds[PLATFORM]?.contains(wpc.wpcId)}

    allWpcsWithCallExtProblem.each {CallExtensionMigrationLog log ->
        println "${log.commandType}, ${log.commandStatus}, ${log.timeStart}-${log.timeEnd}\n"   // ${log.commandStatusMessage}\n
        AdWordsSession session = sessionBuilder.buildAdWordsSessionWithAuthInfo(masterUsername: log.masterUsername, externalAccountId: log.clientCustomerId, devToken: log.devToken)
        def processErrorMessage = new StringBuilder("")
        // find old format CE feed and delete log
        feedFinder.getActiveUSERFeedsWith(session).findAll{it.name.contains("CallExt")}.each { feed ->
            println "(USER) feedId:${feed.id} ${feed.name + " " + feed.status}"
            if (feedAttributesBad(feed.attributes)) {
                println "Find bad attribute in CE field name"
                feed.attributes.each { FeedAttribute attribute ->
                    println "${attribute.id + " " + attribute.name}"
                }
                Feed removedFeed = FeedAction.removeFeed(session, feed)
                println "Removed feed id:${removedFeed.id} ${removedFeed.name + " " + removedFeed.status}"
                processErrorMessage << "Removed feed id:${removedFeed.id} ${removedFeed.name + " " + removedFeed.status}"
            }
        }
        // find good/new format CE, if can't create one
        Feed goodCEFeed = findGoodUSERCEFeed(session, feedFinder)
        if(!goodCEFeed) {
            println "addCallExtFeedAndAttributeMapping"
            goodCEFeed = FeedAction.addCallExtFeedAndAttributeMapping(session)
            processErrorMessage << " create CalExt Feed"
        }
        // find campaignFeed on google
        def campaignId = Long.valueOf(log.publisherCampaignId)
        println "Check on Google AdWords: CampaignId:${campaignId}"
        CampaignFeed[] callExtCampaignFeeds = feedFinder.getCampaignCallExtFeedsWith(session, campaignId)
        try {
            if (callExtCampaignFeeds && callExtCampaignFeeds.length == 1) {
                def feedId = callExtCampaignFeeds[0].feedId
                def feedItemId = (callExtCampaignFeeds[0].matchingFunction.getRhsOperand(0) as ConstantOperand[])[0].longValue
                log.callExtId = feedItemId
                def foundCEMsg = "ONE Existing CE Campfeed found, campId:${campaignId}, feedId:${feedId}, feedItemId:${feedItemId}"
                println foundCEMsg
                processErrorMessage << foundCEMsg
                FeedItem callExtItem = feedFinder.findFeedItemById(session, feedItemId)[0]
                def foundPhoneMsg = "phoneNumber: " + callExtItem.getAttributeValues(0).stringValue + "countryCode: " + callExtItem.getAttributeValues(1).stringValue
                println foundPhoneMsg
                processErrorMessage << foundPhoneMsg
            } else {
                if (callExtCampaignFeeds) {
                    def somethingWrong = "Something wrong, there is ${callExtCampaignFeeds?.length} CallExtCampaignFeed, feedIds:${callExtCampaignFeeds?.collect { it.feedId }.join(",")}"
                    println somethingWrong
                    processErrorMessage << somethingWrong
                } else {
                    println "Can't find CE Campaign feed, for campId:${campaignId}"
                    // add CampaignFeed here with FeedMapping (add if does not exist) and FeedItem
                    if(!feedFinder.getFeedMappingOf(session, goodCEFeed))
                        FeedAction.createAndProvisionCallExtFeedMapping(session, goodCEFeed)

                    FeedItem newCallExtFeedItem = FeedItemAction.addCallExtFeedItem(session, goodCEFeed, log.phoneNumber.replaceAll("#", ""), log.countryCode)
                    def callExtAddedMsg = "${log.wpcId} added Call Extension in google, id: ${newCallExtFeedItem.feedItemId}"
                    println callExtAddedMsg
                    processErrorMessage << callExtAddedMsg
                    CampaignFeedAction.addCallExtCampaignFeed(session, goodCEFeed.id, campaignId, newCallExtFeedItem.feedItemId)
                }
            }
        } catch (any) {
            processErrorMessage << any.message
        } finally {
            log.errorMessage = processErrorMessage.toString()
            saveMigrationLog(sqlToLog, log)
        }
        println "\n\n"
    }
}

stopwatch.stop()
println "${new Date()} it took ${stopwatch.elapsed(java.util.concurrent.TimeUnit.SECONDS)} seconds"

private boolean feedAttributesBad(FeedAttribute[] attributes) {
    boolean hasBadAttribute = false
    attributes.each {
        try{
            CallExtensionField.fromAttribute(it.name)
        } catch(IllegalArgumentException ex) {
            hasBadAttribute = true
        }
    }
    return hasBadAttribute
}

Feed findGoodUSERCEFeed(session, FeedFinder feedFinder) {
    Feed[] feeds = feedFinder.getActiveUSERFeedsWith(session)
    feeds.find { it.name.contains("CallExt") && it.attributes.size() == 4 && !feedAttributesBad(it.attributes)}
}

private void saveMigrationLog(Sql sqlToLog, CallExtensionMigrationLog migrationLog) {
    Sql sql = sqlToLog
    sql.executeInsert("insert into CallExtensionMigrationLog (platform, wpcId, publisher_campaign_id, phone_number, country_code, call_extension_id, error_message, " +
            "client_customer_id, master_username, dev_token, created," +
            "command_type, command_status, command_status_message, time_start, time_end) " +
            " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
            [ migrationLog.platform.name(), migrationLog.wpcId, migrationLog.publisherCampaignId, migrationLog.phoneNumber, migrationLog.countryCode, migrationLog.callExtId, migrationLog.errorMessage,
              migrationLog.clientCustomerId, migrationLog.masterUsername, migrationLog.devToken, migrationLog.created,
              migrationLog.commandType, migrationLog.commandStatus, migrationLog.commandStatusMessage, migrationLog.timeStart, migrationLog.timeEnd])
}

def getPreviousRunWpcIds(sqlToLog) {
    def platPrevRunWpcIds = [:]
    def dryRunWpcIds
    sqlToLog.eachRow("select platform, wpcId from CallExtensionMigrationLog") {
        dryRunWpcIds = platPrevRunWpcIds[it.platform]
        if(!dryRunWpcIds) {
            dryRunWpcIds = []
            platPrevRunWpcIds[it.platform] = dryRunWpcIds
        }
        dryRunWpcIds << (long) it.wpcId
    }
    platPrevRunWpcIds
}
