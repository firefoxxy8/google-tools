package feed.model;

import org.apache.commons.lang.Validate;

public enum CustomerFeedSelectableField {
    // see https://developers.google.com/adwords/api/docs/reference/v201406/CustomerFeedService.CustomerFeed
    // for selectable fields
    FEED_ID("FeedId"),
    MATCHING_FUNCTION("MatchingFunction"),
    PLACEHOLDER_TYPES("PlaceholderTypes"),
    STATUS("Status");

    private String value;

    CustomerFeedSelectableField(String val) {
        value = val;
    }

    public String getValue() {
        return value;
    }

    public static CustomerFeedSelectableField fromValue(String val) throws IllegalArgumentException,
                    NullPointerException {
        Validate.notNull(val, "Unable to create CustomerFeedSelectableField enum from Null value.");
        for (CustomerFeedSelectableField cf : values()) {
            if (cf.getValue().equalsIgnoreCase(val)) {
                return cf;
            }
        }
        throw new IllegalArgumentException("No CustomerFeedSelectableField with the supplied attribute exists: " + val);
    }
}
