package dan200.computercraft.shared.network.messages;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class RequestTileEntityUpdate implements IMessage
{
    private int x, y, z;

    public RequestTileEntityUpdate( int x, int y, int z )
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void fromBytes( ByteBuf buf )
    {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
    }

    @Override
    public void toBytes( ByteBuf buf )
    {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public int getZ()
    {
        return z;
    }
}
