package net.dhleong.corgen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tag any plugins that should be available to select 
 *  via command line with this annotation. This
 *  is so we can hide base implementations, etc.
 * 
 * @author dhleong
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Plugin {

}
