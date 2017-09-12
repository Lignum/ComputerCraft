package dan200.computercraft.shared.network.messages;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * A message intended for the client.
 */
public interface ClientMessage extends IMessage
{
    int getTargetID();
}
