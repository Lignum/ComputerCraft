package dan200.computercraft.shared.network.messages;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * A message intended for the server.
 */
public interface ServerMessage extends IMessage
{
    boolean isContainerNeeded();
}
