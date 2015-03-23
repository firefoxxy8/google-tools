package feed.model;

import org.apache.commons.lang.Validate;

public enum CampaignFeedSelectableField {
    FEED_ID("FeedId"),
    CAMPAIGN_ID("CampaignId"),
    MATCHING_FUNCTION("MatchingFunction"),
    PLACEHOLDER_TYPES("PlaceholderTypes"),
    STATUS("Status");

    private String value;

    CampaignFeedSelectableField(String val) {
        value = val;
    }

    public String getValue() {
        return value;
    }

    public static CampaignFeedSelectableField fromValue(String val) throws IllegalArgumentException,
                    NullPointerException {
        Validate.notNull(val, "Unable to create CampaignFeedSelectableField enum from Null value.");
        for (CampaignFeedSelectableField cf : values()) {
            if (cf.getValue().equalsIgnoreCase(val)) {
                return cf;
            }
        }
        throw new IllegalArgumentException("No CampaignFeedSelectableField with the supplied attribute exists: " + val);
    }
}
