package dan200.computercraft.shared.network;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.common.TileGeneric;
import dan200.computercraft.shared.computer.core.ClientComputer;
import dan200.computercraft.shared.network.messages.ClientMessage;
import dan200.computercraft.shared.network.messages.ComputerDeleted;
import dan200.computercraft.shared.network.messages.ComputerState;
import dan200.computercraft.shared.network.messages.RequestTileEntityUpdate;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ClientPacketHandler implements IMessageHandler<ClientMessage, IMessage>
{
    @Override
    public IMessage onMessage( ClientMessage msg, MessageContext ctx )
    {
        Minecraft.getMinecraft().addScheduledTask(() ->
        {
            ClientComputer computer;

            if( !ComputerCraft.clientComputerRegistry.contains( msg.getTargetID() ) )
            {
                computer = new ClientComputer( msg.getTargetID() );
                ComputerCraft.clientComputerRegistry.add( msg.getTargetID(), computer );
            }
            else
            {
                if( msg instanceof ComputerDeleted )
                {
                    ComputerCraft.clientComputerRegistry.remove( msg.getTargetID() );
                    return;
                }
                computer = ComputerCraft.clientComputerRegistry.get( msg.getTargetID() );
            }

            if( msg instanceof ComputerState )
            {
                computer.syncState( (ComputerState) msg );
            }
        });

        return null;
    }
}
