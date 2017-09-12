package dan200.computercraft.shared.network.messages;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

public class SetComputerLabel implements ServerMessage
{
    private String label;

    public SetComputerLabel( String label )
    {
        this.label = label;
    }

    public String getLabel()
    {
        return label;
    }

    @Override
    public void fromBytes( ByteBuf buf )
    {
        int length = buf.readInt();
        label = buf.readCharSequence( length, Charset.forName( "UTF-8" ) ).toString();
    }

    @Override
    public void toBytes( ByteBuf buf )
    {
        buf.writeInt( label.length() );
        buf.writeCharSequence( label, Charset.forName( "UTF-8" ) );
    }

    @Override
    public boolean isContainerNeeded()
    {
        return true;
    }
}
