package dan200.computercraft.shared.network.messages;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ComputerEvent implements ServerMessage
{
    private String name;
    private final List<String> arguments = new LinkedList<>();

    @Override
    public boolean isContainerNeeded()
    {
        return true;
    }

    public String getName()
    {
        return name;
    }

    public List<String> getArguments()
    {
        return Collections.unmodifiableList( arguments );
    }

    @Override
    public void fromBytes( ByteBuf buf )
    {
        int nameLength = buf.readInt();
        name = buf.readCharSequence( nameLength, Charset.forName( "UTF-8" ) ).toString();

        arguments.clear();

        int argCount = buf.readInt();

        for( int i = 0; i < argCount; ++i )
        {
            int argLength = buf.readInt();
            String arg = buf.readCharSequence( argLength, Charset.forName( "UTF-8" ) ).toString();
            arguments.add( arg );
        }
    }

    @Override
    public void toBytes( ByteBuf buf )
    {
        buf.writeInt( name.length() );
        buf.writeCharSequence( name, Charset.forName( "UTF-8" ) );

        buf.writeInt( arguments.size() );

        for( String arg : arguments )
        {
            buf.writeInt( arg.length() );
            buf.writeCharSequence( arg, Charset.forName( "UTF-8" ) );
        }
    }
}
