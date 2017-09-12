package dan200.computercraft.shared.network.messages;

import io.netty.buffer.ByteBuf;
public class ComputerDeleted implements ClientMessage
{
    private int targetID;

    public ComputerDeleted( int targetID )
    {
        this.targetID = targetID;
    }

    @Override
    public int getTargetID()
    {
        return targetID;
    }

    @Override
    public void fromBytes( ByteBuf buf )
    {
        targetID = buf.readInt();
    }

    @Override
    public void toBytes( ByteBuf buf )
    {
        buf.writeInt( targetID );
    }
}
