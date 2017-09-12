package dan200.computercraft.shared.util;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ObjectEncoder
{
    private enum ObjectType
    {
        BOOLEAN(0),
        NUMBER(1),
        STRING(2),
        MAP(3);

        private final int id;

        ObjectType( int id )
        {
            this.id = id;
        }

        public int getID()
        {
            return id;
        }

        public static Optional<ObjectType> fromID( int id )
        {
            return Arrays.stream(ObjectType.values())
                .filter(o -> o.id == id)
                .findFirst();
        }
    }

    public static void writeObject( ByteBuf buf, Object obj )
    {
        if( obj instanceof Boolean )
        {
            buf.writeInt( ObjectType.BOOLEAN.getID() );
            buf.writeBoolean( (Boolean) obj );
        }
        else if( obj instanceof Number )
        {
            buf.writeInt( ObjectType.NUMBER.getID() );
            buf.writeDouble( (Double) obj );
        }
        else if( obj instanceof String )
        {
            String s = (String) obj;
            buf.writeInt( ObjectType.STRING.getID() );
            buf.writeInt( s.length() );
            buf.writeCharSequence( s, Charset.forName( "UTF-8" ) );
        }
        else if( obj instanceof Map )
        {
            buf.writeInt( ObjectType.MAP.getID() );
            Map<?, ?> m = (Map<?, ?>) obj;

            buf.writeInt( m.size() );

            for( Map.Entry<?, ?> entry : m.entrySet() )
            {
                writeObject( buf, entry.getKey() );
                writeObject( buf, entry.getValue() );
            }
        }
    }

    public static Object readObject( ByteBuf buf )
    {
        Optional<ObjectType> objectTypeOpt = ObjectType.fromID( buf.readInt() );
        if(objectTypeOpt.isPresent())
        {
            ObjectType objectType = objectTypeOpt.get();

            switch( objectType )
            {
                case BOOLEAN:
                    return buf.readBoolean();
                case NUMBER:
                    return buf.readDouble();
                case STRING:
                {
                    int strLength = buf.readInt();
                    return buf.readCharSequence( strLength, Charset.forName( "UTF-8" ) );
                }
                case MAP:
                {
                    Map<Object, Object> m = new HashMap<>();
                    int size = buf.readInt();

                    for( int i=0; i<size; ++i )
                    {
                        Object k = readObject( buf );
                        Object v = readObject( buf );
                        m.put( k, v );
                    }

                    return m;
                }
                default:
                    return null;
            }
        }
        else
        {
            return null;
        }
    }

    public static void encodeObjects( ByteBuf buf, Object... objs )
    {
        buf.writeInt( objs.length );

        for( Object obj : objs )
        {
            writeObject( buf, obj );
        }
    }

    public static Object[] decodeObjects( ByteBuf buf )
    {
        int objCount = buf.readInt();
        Object[] objs = new Object[objCount];

        for ( int i=0; i<objCount; ++i )
        {
            objs[i] = readObject( buf );
        }

        return objs;
    }
}
