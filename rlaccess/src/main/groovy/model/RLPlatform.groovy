package model

/**
 * Copy Right 2014 of Reach Local Inc.,
 */
public enum RLPlatform {
    USA,
    CAN,
    AUS,
    EUR,
    GBR,
    JPN;

    static RLPlatform fromValue(String value) {
        for(RLPlatform platform: RLPlatform.values()) {
            if(value == platform.name()) {
                return platform
            }
        }
        throw new RuntimeException("No such platform " + value);
    }
}