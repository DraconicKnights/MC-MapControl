package com.draconincdomain.mapcontrol.Annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Commands {
    String name();
    String permission() default "";
    boolean requiresPlayer() default true;
    boolean hasCooldown() default false;
    int cooldownDuration() default 5;
}
