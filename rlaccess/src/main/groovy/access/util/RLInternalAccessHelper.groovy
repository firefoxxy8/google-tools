package access.util
import groovy.json.JsonSlurper
import groovy.sql.Sql
import org.apache.tomcat.dbcp.dbcp.BasicDataSource
import org.joda.time.DateTime
import wslite.rest.RESTClient

import java.text.SimpleDateFormat

/**
 * Copy Right 2014 of Reach Local Inc.,
 */
class RLInternalAccessHelper {

    static RLInternalAccessHelper rlInternalAccessHelper = new RLInternalAccessHelper()

    static public RLInternalAccessHelper getInstance() {
        return rlInternalAccessHelper
    }

    private RLInternalAccessHelper(){

    }

    enum env {
        Analytics,
        QaUSNX1,
        DevLocal
    }

    def RLACCESS_CONF_PATH = System.getenv("RLACCESS_CONF_PATH")?:System.getProperty("RLACCESS_CONF_PATH")
    def rlAccess = new File(RLACCESS_CONF_PATH).toURL()
    def accessConfig = new ConfigSlurper().parse(rlAccess)

    def activemqConfig = accessConfig.activemq

    Sql getAnalyticSql(platform='USA') {
        def analyticAccess = accessConfig.database.analytics
        Sql analyticSql = Sql.newInstance("jdbc:mysql://${analyticAccess.hostname}:3306/${accessConfig.platformPrefix[platform]}op", "${analyticAccess.username}", "${analyticAccess.password}", "com.mysql.jdbc.Driver")
        analyticSql
    }

    Sql getQaNX1Sql(platform='USA') {
        def qanx1Access = accessConfig.database.qaNX1
        Sql qanx1Sql = Sql.newInstance("jdbc:mysql://${qanx1Access.hostnames[platform]}:3306/${accessConfig.platformPrefix[platform]}op", "${qanx1Access.username}", "${qanx1Access.password}", "com.mysql.jdbc.Driver")
        qanx1Sql
    }

    Sql getDevLocalSql(dbname='test') {
        def devLocal = accessConfig.database.devLocal
        Sql devLocalSql = new Sql(createDataSource("${devLocal.username}", "${devLocal.password}", "jdbc:mysql://${devLocal.hostname}:3306/${dbname}?useUnicode=true&characterEncoding=UTF-8"))
        devLocalSql
    }

    def createDataSource(String username, String password, String dbUrl) {
        new BasicDataSource(driverClassName: 'com.mysql.jdbc.Driver',
                username: username,
                password: password,
                url: dbUrl)
    }

    def getBingAccessToken(mccUsername) {
        int retryCount = 50

        def bingSvcConfig = accessConfig.bingServices
        def reachlocalInternalAccessToken = bingSvcConfig.credentialAccessToken
        def bingSvcRestClient = new RESTClient("${bingSvcConfig.url}")
        while(retryCount-- > 0){
            def resp = bingSvcRestClient.get(path:"/credentials/${URLEncoder.encode(mccUsername, 'UTF-8')}", headers: ["Authorization":"bearer ${reachlocalInternalAccessToken}"])
//        println resp.statusCode
            def respContent = resp.contentAsString
            def accessToken = new JsonSlurper().parseText(respContent)
            if(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(accessToken.expiry).after(new DateTime().plusMinutes(5).toDate()))
                return accessToken
            println "token expires within 5 minutes, ${accessToken.expiry}, retry"
            sleep(6000)
        }
    }

    def getGoogleAccessToken(username) {
        def googleSvcConfig = accessConfig.googleServices
        def reachlocalInternalAccessToken = googleSvcConfig.credentialAccessToken
        def googleSvcRestClient = new RESTClient("${googleSvcConfig.url}")
        def errorMsg
        def resp
        try{
            resp = googleSvcRestClient.get(path:"/credentials/${URLEncoder.encode(username, 'UTF-8')}", headers: ["Authorization":"bearer ${reachlocalInternalAccessToken}"])
        }
        catch(ex) {
            errorMsg =  "exception: ${ex.getMessage()}"
            throw new RuntimeException("Can't get google access token for ${username} ,${errorMsg}")
        }
//        println resp.statusCode
        if(resp.statusCode != 200) {
            throw new RuntimeException("${resp.statusCode} Can't get google access token for ${username}, ${errorMsg}")
        }
        def respContent = resp.contentAsString
        new JsonSlurper().parseText(respContent)
    }


    def getBingMasterCredentialsForCustomerAccount(env environment, accountId) {
        def bingCredentials = []
        def accessSql
        switch(environment) {
            case env.Analytics : accessSql = getAnalyticSql(); break
            case env.QaUSNX1 : accessSql = getQaNX1Sql(); break;
            default: throw new Exception("not supported currently")
        }
        accessSql.eachRow ("select master.pea_username masterUsername, master.pea_key1 devToken from WebPublisherExternalAccount wpea " +
                "join WebPublisherExternalAccount master on wpea.WebPublisherExternalAccount_idWebPublisherExternalAccount_par_FK = master.idWebPublisherExternalAccount " +
                "where wpea.WebPublisher_idWebPublisher_FK=17 and master.pea_account_type='master' and wpea.pea_account_id=${accountId}") {
            bingCredentials << [Username:it.masterUsername.toString(), DevToken: it.devToken.toString()]
        }
        bingCredentials[0]
    }

    def getBingMasterCredentials(env environment) {
        def bingCredentials = []
        def accessSql
        switch(environment) {
            case env.Analytics : accessSql = getAnalyticSql(); break
            case env.QaUSNX1 : accessSql = getQaNX1Sql(); break;
            default: throw new Exception("not supported currently")
        }
        accessSql.eachRow ("select pea_username, pea_account_id, pea_account_type, pea_key1 from WebPublisherExternalAccount where WebPublisher_idWebPublisher_FK in (17) and pea_account_type in ('master')") {
            bingCredentials << [Username:it.pea_username.toString(), DevToken: it.pea_key1.toString()]
        }
        bingCredentials[0]
    }

    def static main(args) {
        def helper = RLInternalAccessHelper.instance
        println "Testing access helper:"
        println helper.getBingMasterCredentials(RLInternalAccessHelper.env.Analytics)

        println helper.getBingMasterCredentialsForCustomerAccount(RLInternalAccessHelper.env.Analytics, 165221)

        println helper.getGoogleAccessToken("google_B1_QA@reachlocal.biz")
        println helper.getBingAccessToken("API_reachlocal")

//        println helper.getQaNX1Sql('CAN').firstRow("select * from WebPublisher")
    }
}