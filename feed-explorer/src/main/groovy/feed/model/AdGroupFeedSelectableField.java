package feed.model;

import org.apache.commons.lang.Validate;

public enum AdGroupFeedSelectableField {
    // see https://developers.google.com/adwords/api/docs/reference/v201406/AdGroupFeedService.AdGroup Feed
    // for selectable fields
    FEED_ID("FeedId"),
    AD_GROUP_ID("AdGroupId"),
    MATCHING_FUNCTION("MatchingFunction"),
    PLACEHOLDER_TYPES("PlaceholderTypes"),
    STATUS("Status");

    private String value;

    AdGroupFeedSelectableField(String val) {
        value = val;
    }

    public String getValue() {
        return value;
    }

    public static AdGroupFeedSelectableField fromValue(String val) throws IllegalArgumentException,
                    NullPointerException {
        Validate.notNull(val, "Unable to create CampaignFeedSelectableField enum from Null value.");
        for (AdGroupFeedSelectableField cf : values()) {
            if (cf.getValue().equalsIgnoreCase(val)) {
                return cf;
            }
        }
        throw new IllegalArgumentException("No CampaignFeedSelectableField with the supplied attribute exists: " + val);
    }
}
