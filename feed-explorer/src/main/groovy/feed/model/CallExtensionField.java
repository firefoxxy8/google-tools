package feed.model;

public enum CallExtensionField {
    PHONE_NUMBER(1, "Phone Number"),
    COUNTRY_CODE(2, "Country Code"),
    TRACKED(3, "Call Tracking"),
    ONLY(4, "Display Phone Number only");

    private int value;
    private String attributeName;

    CallExtensionField(int val, String attribute) {
        value = val;
        attributeName = attribute;
    }

    public int getValue() {
        return value;
    }

    public String getAttribute() {
        return attributeName;
    }

    public static CallExtensionField fromValue(int val) throws IllegalArgumentException {
        for (CallExtensionField cf : values()) {
            if (cf.getValue() == val) {
                return cf;
            }
        }
        throw new IllegalArgumentException("No CallExtensionField with the supplied value exists: " + val);
    }

    public static CallExtensionField fromAttribute(String attribute) throws IllegalArgumentException {
        for (CallExtensionField cf : values()) {
            if (cf.getAttribute().equalsIgnoreCase(attribute)) {
                return cf;
            }
        }
        throw new IllegalArgumentException("No CallExtensionField with the supplied attribute exists: " + attribute);
    }
}
