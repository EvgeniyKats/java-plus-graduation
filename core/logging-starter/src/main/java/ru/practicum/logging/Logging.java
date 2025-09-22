package ru.practicum.logging;

import org.slf4j.event.Level;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Logging {
    @AliasFor("value")
    Level level() default Level.INFO;

    @AliasFor("level")
    Level value() default Level.INFO;
}
