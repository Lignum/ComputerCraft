package dan200.computercraft.shared.network.messages;

import dan200.computercraft.shared.util.ObjectEncoder;
import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

public class ComputerEvent implements ServerMessage
{
    private int targetID;
    private String name;
    private Object[] arguments;

    public ComputerEvent( int targetID, String name, Object... args )
    {
        this.targetID = targetID;
        this.name = name;
        this.arguments = args;
    }

    @Override
    public boolean isContainerNeeded()
    {
        return true;
    }

    @Override
    public int getTargetID()
    {
        return targetID;
    }

    public String getName()
    {
        return name;
    }

    public Object[] getArguments()
    {
        return arguments;
    }

    @Override
    public void fromBytes( ByteBuf buf )
    {
        targetID = buf.readInt();
        int nameLength = buf.readInt();
        name = buf.readCharSequence( nameLength, Charset.forName( "UTF-8" ) ).toString();
        arguments = ObjectEncoder.decodeObjects( buf );
    }

    @Override
    public void toBytes( ByteBuf buf )
    {
        buf.writeInt( targetID );
        buf.writeInt( name.length() );
        buf.writeCharSequence( name, Charset.forName( "UTF-8" ) );
        ObjectEncoder.encodeObjects( buf, arguments );
    }
}
