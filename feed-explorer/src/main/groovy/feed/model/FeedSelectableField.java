package feed.model;

import org.apache.commons.lang.Validate;

public enum FeedSelectableField {
    ID("Id"),
    NAME("Name"),
    ATTRIBUTES("Attributes"),
    STATUS("FeedStatus"),
    ORIGIN("Origin");

    private String value;

    FeedSelectableField(String val) {
        value = val;
    }

    public String getValue() {
        return value;
    }

    public static FeedSelectableField fromValue(String val) throws IllegalArgumentException,
                    NullPointerException {
        Validate.notNull(val, "Unable to create FeedSelectableField enum from Null value.");
        return FeedSelectableField.valueOf(val.toUpperCase());
    }
}
