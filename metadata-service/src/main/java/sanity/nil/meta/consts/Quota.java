package sanity.nil.meta.consts;

public enum Quota {
    USER_STORAGE_USED((short) 1),
    USER_WORKSPACES((short) 2);

    private Short id;

    public Short id() {
        return id;
    }

    Quota(Short id) {
        this.id = id;
    }

    public Quota fromID(Number id) {
        return switch ((short) id) {
            case 1 -> USER_STORAGE_USED;
            case 2 -> USER_WORKSPACES;
            default -> throw new IllegalStateException("Unexpected value: " + (short) id);
        };
    }
}
