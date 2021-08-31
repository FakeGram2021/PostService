package tim6.postservice.domain.exceptions;

public class EntityAlreadyExistsException extends RuntimeException {

    private static final long serialVersionUID = -465376256667420533L;

    public EntityAlreadyExistsException() {
    }

    public EntityAlreadyExistsException(String message) {
        super(message);
    }
}
