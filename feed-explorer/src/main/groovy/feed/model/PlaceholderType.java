package feed.model;

public enum PlaceholderType {
    SITELINKS(1, "Default SiteLinks Feed"),
    CALL(2, "Default CallExt Feed"),
    APP(3, "Default Application Feed"),
    LOCATION(7, "Location Feed");

    private int value;
    private String attributeName;

    PlaceholderType(int val, String attribute) {
        value = val;
        attributeName = attribute;
    }

    public int getValue() {
        return value;
    }

    public String getAttribute() {
        return attributeName;
    }

    public static PlaceholderType fromValue(int val) throws IllegalArgumentException {
        for (PlaceholderType ft : values()) {
            if (ft.getValue() == val) {
                return ft;
            }
        }
        throw new IllegalArgumentException("No FeedType with the supplied value exists: " + val);
    }

    public static PlaceholderType fromAttribute(String attribute) throws IllegalArgumentException {
        for (PlaceholderType ft : values()) {
            if (ft.getAttribute().equalsIgnoreCase(attribute)) {
                return ft;
            }
        }
        throw new IllegalArgumentException("No FeedType with the supplied attribute exists: " + attribute);
    }
}
