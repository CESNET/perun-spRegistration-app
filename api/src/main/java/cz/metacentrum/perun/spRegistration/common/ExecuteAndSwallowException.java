package cz.metacentrum.perun.spRegistration.common;

import org.slf4j.Logger;

@FunctionalInterface
public interface ExecuteAndSwallowException {

    default void execute() {
        execute(null);
    }

    default void execute(Logger log) {
        try {
            this.operation();
        } catch (Exception e) {
            if (log != null) {
                log.warn("Swallowed exception", e);
            }
        }
    }

    void operation() throws Exception;

}
