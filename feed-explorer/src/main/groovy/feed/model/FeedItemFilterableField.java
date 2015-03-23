package feed.model;

import org.apache.commons.lang.Validate;

public enum FeedItemFilterableField {
    //fields that can be used in predicates (as where clause in sql)
    FEED_ID("FeedId"),
    FEED_ITEM_ID("FeedItemId"),
    STATUS("Status"),
    DEVICE_PREFERENCE("DevicePreference");

    private String value;

    FeedItemFilterableField(String val) {
        value = val;
    }

    public String getValue() {
        return value;
    }

    public static FeedItemFilterableField fromValue(String val) throws IllegalArgumentException,
                    NullPointerException {
        Validate.notNull(val, "Unable to create FeedItemFilterableField enum from Null value.");
        for (FeedItemFilterableField cf : values()) {
            if (cf.getValue().equalsIgnoreCase(val)) {
                return cf;
            }
        }
        throw new IllegalArgumentException("No FeedItemFilterableField with the supplied attribute exists: " + val);
    }
}
