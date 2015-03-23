package call.extension.script
import access.util.RLInternalAccessHelper
import feed.FeedFinder
import feed.FeedItemDetailsPrint
import google.support.AdWordsSessionRLBuilder
/**
 * Copy Right 2014 of Reach Local Inc.,
 */
long WPC_ID = 961118L

def accessHelper = RLInternalAccessHelper.instance
def sessionBuilder = new AdWordsSessionRLBuilder(_accessHelper: accessHelper, sqlToGetAuthInfo: accessHelper.getAnalyticSql("USA"))
def CEFeedFinder = new FeedFinder()
def session = sessionBuilder.buildAdWordSessionWithWpcId(WPC_ID)

def feeds = CEFeedFinder.getActiveUSERFeedsWith(session)

feeds.findAll{it.name.contains("CallExt")}.each { feed ->
    println "id:${feed.id} ${feed.name + " "+ feed.status}"
    feed.attributes.each { attribute ->
        println "${attribute.id + " " + attribute.name}"
    }
    CEFeedFinder.findAllFeedItemsByFeedId(session, feed.id).each {
        FeedItemDetailsPrint.printItem(it)
    }
}
