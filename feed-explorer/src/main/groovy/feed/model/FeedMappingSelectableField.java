package feed.model;

import org.apache.commons.lang.Validate;

public enum FeedMappingSelectableField {
    FEED_MAPPING_ID("FeedMappingId"),
    FEED_ID("FeedId"),
    PLACEHOLDER_TYPE("PlaceholderType"),
    STATUS("Status"),
    ATTRIBUTE_FIELD_MAPPINGS("AttributeFieldMappings");

    private String value;

    FeedMappingSelectableField(String val) {
        value = val;
    }

    public String getValue() {
        return value;
    }

    public static FeedMappingSelectableField fromValue(String val) throws IllegalArgumentException,
                    NullPointerException {
        Validate.notNull(val, "Unable to create FeedMappingSelectableField enum from Null value.");
        for (FeedMappingSelectableField cf : values()) {
            if (cf.getValue().equalsIgnoreCase(val)) {
                return cf;
            }
        }
        throw new IllegalArgumentException("No FeedMappingSelectableField with the supplied attribute exists: " + val);
    }
}
