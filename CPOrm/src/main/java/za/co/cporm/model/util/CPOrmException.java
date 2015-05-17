package za.co.cporm.model.util;

/**
 * Created by hennie.brink on 2015-05-17.
 */
public class CPOrmException extends RuntimeException {

    public CPOrmException() {

    }

    public CPOrmException(String detailMessage) {

        super(detailMessage);
    }

    public CPOrmException(String detailMessage, Throwable throwable) {

        super(detailMessage, throwable);
    }

    public CPOrmException(Throwable throwable) {

        super(throwable);
    }
}
