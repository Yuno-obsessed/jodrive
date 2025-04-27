package sanity.nil.exceptions;

public class WorkspaceIdentityException extends SecurityException {

    public WorkspaceIdentityException(String wsID) {
        super("Workspace identity is not in workspace " + wsID);
    }
}
