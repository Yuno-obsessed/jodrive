package sanity.nil.meta.exceptions;

import sanity.nil.meta.consts.Quota;

public class InsufficientQuotaException extends RuntimeException {

    public InsufficientQuotaException(Quota quota, String limit) {
        super(String.format("Exceeded quota %s with limit %s", quota, limit));
    }

}
