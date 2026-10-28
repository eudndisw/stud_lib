package com.library.studentlibrary.exception;

/** Порушення бізнес-правил (ліміт видач, немає примірників, дубль email тощо). */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
