package net.sf.groovyMonkey.util;
import java.io.Serializable;

public interface Transformer < T >
extends Serializable
{
    public T transform( final Object input );
}
