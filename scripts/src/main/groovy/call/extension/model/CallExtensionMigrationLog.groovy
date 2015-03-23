package call.extension.model

import model.RLPlatform

/**
 * Copy Right 2014 of Reach Local Inc.,
 */
class CallExtensionMigrationLog {
    RLPlatform platform
    Long wpcId
    String publisherCampaignId
    String phoneNumber
    String countryCode
    Long callExtId
    String errorMessage
    String clientCustomerId
    String masterUsername
    String devToken
    Date created
    Date lastModified
    String commandType
    String commandStatus
    String commandStatusMessage
    Date timeStart
    Date timeEnd
}
