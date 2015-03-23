package feed.model;

import org.apache.commons.lang.Validate;

public enum FeedItemSelectableField {
    FEED_ID("FeedId"),
    FEED_ITEM_ID("FeedItemId"),
    STATUS("Status"),
    START_TIME("StartTime"),
    END_TIME("EndTime"),
    ATTRIBUTE_VALUES("AttributeValues"),
    VALIDATION_DETAILS("ValidationDetails"),
    DEVICE_PREFERENCE("DevicePreference"),
    SCHEDULING("Scheduling");

    private String value;

    FeedItemSelectableField(String val) {
        value = val;
    }

    public String getValue() {
        return value;
    }

    public static FeedItemSelectableField fromValue(String val) throws IllegalArgumentException,
                    NullPointerException {
        Validate.notNull(val, "Unable to create FeedItemSelectableField enum from Null value.");
        for (FeedItemSelectableField cf : values()) {
            if (cf.getValue().equalsIgnoreCase(val)) {
                return cf;
            }
        }
        throw new IllegalArgumentException("No FeedItemSelectableField with the supplied attribute exists: " + val);
    }
}
