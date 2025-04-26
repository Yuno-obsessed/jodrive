package sanity.nil.security;

import lombok.Getter;

@Getter
public enum Role {
    USER("user"),
    GUEST("guest");

    private String name;

    Role(String name) {
        this.name = name;
    }

    public static Role fromName(String name) {
        return switch (name) {
            case "user" -> Role.USER;
            case "guest" -> Role.GUEST;
            default -> null;
        };
    }
}
