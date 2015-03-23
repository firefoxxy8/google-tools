package google.support
import access.util.RLInternalAccessHelper
import com.google.api.ads.adwords.lib.client.AdWordsSession
import com.google.api.client.auth.oauth2.BearerToken
import com.google.api.client.auth.oauth2.Credential
import groovy.sql.Sql
/**
 * Copy Right 2014 of Reach Local Inc.,
 */
class AdWordsSessionRLBuilder {

    RLInternalAccessHelper _accessHelper   // to get google (OAuth2) access token
    Sql sqlToGetAuthInfo                   // to get the auth info, i.e. masterUsername, devToken, clientCustomerId

    AdWordsSession buildAdWordSessionWithWpeaId(Long wpeaId) {
        buildAdWordSessionWith("where wpea.idWebPublisherExternalAccount = :wpeaId", [wpeaId:wpeaId])
    }

    AdWordsSession buildAdWordSessionWithClientCustomerId(Long clientCustomerId) {
        buildAdWordSessionWith("where wpea.pea_account_id = :externalAccountId", [externalAccountId: clientCustomerId])
    }

    AdWordsSession buildAdWordSessionWithWpcId(Long wpcId) {
        println "wpcId:${wpcId}"
        buildAdWordSessionWithCampaign("where wpc.idWebPublisherCampaign = :wpcId", [wpcId:wpcId])
    }

    AdWordsSession buildAdWordSessionWithPublisherCampaignId(Long publisherCampaignId) {
        buildAdWordSessionWithCampaign("where wpc.wpc_publisher_identifier = :publisherCampaignId", [publisherCampaignId: publisherCampaignId])
    }

    AdWordsSession buildAdWordSessionWithCampaign(String whereClause, Map<String, Long> params) {
        def authInfoQuery = """
        select wpea.pea_account_id externalAccountId, master.pea_username masterUsername, master.pea_key1 devToken
        from WebPublisherExternalAccount wpea
        join WebPublisherExternalAccount master on wpea.`WebPublisherExternalAccount_idWebPublisherExternalAccount_par_FK` = master.`idWebPublisherExternalAccount`
        join WebPublisherCampaign wpc on wpc.WebPublisherExternalAccount_idWebPublisherExternalAccount_FK = wpea.idWebPublisherExternalAccount
        ${whereClause}
        """
        def authInfo = sql.firstRow(authInfoQuery, params)
        buildAdWordsSessionWithAuthInfo(authInfo)
    }

    AdWordsSession buildAdWordsSessionWithAuthInfo(authInfo) {
        def googleAccessToken = accessHelper.getGoogleAccessToken(authInfo.masterUsername).access_token
        def googleCredential = new Credential(BearerToken.authorizationHeaderAccessMethod())
        googleCredential.accessToken = googleAccessToken

        println "${authInfo.masterUsername}, ${authInfo.externalAccountId} ${authInfo.devToken}"

        new AdWordsSession.Builder()
                .withDeveloperToken(authInfo.devToken)
                .withOAuth2Credential(googleCredential)
                .withClientCustomerId(authInfo.externalAccountId)
                .withUserAgent("ReachLocalOAuthClient")
                .build()
    }

    AdWordsSession buildAdWordSessionWith(String whereClause, Map<String, Long> params) {
        def authInfoQuery = """
        select wpea.pea_account_id externalAccountId, master.pea_username masterUsername, master.pea_key1 devToken
        from WebPublisherExternalAccount wpea
        join WebPublisherExternalAccount master on wpea.`WebPublisherExternalAccount_idWebPublisherExternalAccount_par_FK` = master.`idWebPublisherExternalAccount`
        ${whereClause}
        """
        def authInfo = sql.firstRow(authInfoQuery, params)
        if(!authInfo) {
            println "can not find credentials with ${params}"
            return null
        }
        def googleAccessToken = accessHelper.getGoogleAccessToken(authInfo.masterUsername).access_token
        def googleCredential = new Credential(BearerToken.authorizationHeaderAccessMethod())
        googleCredential.accessToken = googleAccessToken

        println "${authInfo.masterUsername}, ${authInfo.externalAccountId} ${authInfo.devToken}"
        new AdWordsSession.Builder()
                .withDeveloperToken(authInfo.devToken)
                .withOAuth2Credential(googleCredential)
                .withClientCustomerId(authInfo.externalAccountId)
                .withUserAgent("ReachLocalOAuthClient")
                .build()
    }

    AdWordsSession buildMCCAdWordSession(String mccLogin) {
        def authInfoQuery = """
        select master.pea_account_id externalAccountId, master.pea_username masterUsername, master.pea_key1 devToken
        from WebPublisherExternalAccount master
        where master.pea_username = ${mccLogin}
        """
        def authInfo = sql.firstRow(authInfoQuery)
        def googleAccessToken = accessHelper.getGoogleAccessToken(authInfo.masterUsername).access_token
        def googleCredential = new Credential(BearerToken.authorizationHeaderAccessMethod())
        googleCredential.accessToken = googleAccessToken

        new AdWordsSession.Builder()
                .withDeveloperToken(authInfo.devToken)
                .withOAuth2Credential(googleCredential)
                .withClientCustomerId(authInfo.externalAccountId)
                .withUserAgent("ReachLocalOAuthClient")
                .build()
    }

    RLInternalAccessHelper getAccessHelper() {
        if(_accessHelper == null) {
            _accessHelper = RLInternalAccessHelper.instance
        }
        _accessHelper
    }

    Sql getSql() {
        if(!sqlToGetAuthInfo) {
            sqlToGetAuthInfo = accessHelper.getAnalyticSql()   // if not passed in, default to analytics USA
        }
        sqlToGetAuthInfo
    }
}
