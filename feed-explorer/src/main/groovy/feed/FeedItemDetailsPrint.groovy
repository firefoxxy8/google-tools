package feed
import com.google.api.ads.adwords.axis.v201406.cm.FeedItem
/**
 * Copy Right 2014 of Reach Local Inc.,
 */
class FeedItemDetailsPrint {

    static def printItem(FeedItem item) {
        println "ITEM id:${item.feedItemId}, ${item.status} Attributes:---------------"
        item.attributeValues.each{ value ->
            print "${value.feedAttributeId}: ${value.stringValue?:""} ${value.booleanValue!=null?value.booleanValue:""}\t"
        }
        println()
        item.validationDetails?.each{ value ->
            println "MAPPING ${value.feedMappingId}"
            println "STATUS ${value.validationStatus}"
            value.validationErrors.each{ error ->
                println "${error.feedAttributeIds} ${error.validationErrorCode}"
            }
        }
    }
}
