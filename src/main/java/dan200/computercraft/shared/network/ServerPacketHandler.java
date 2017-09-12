package dan200.computercraft.shared.network;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.common.TileGeneric;
import dan200.computercraft.shared.computer.core.IComputer;
import dan200.computercraft.shared.computer.core.IContainerComputer;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.network.messages.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ServerPacketHandler implements IMessageHandler<ServerMessage, IMessage>
{
    @Override
    public IMessage onMessage( ServerMessage msg, MessageContext ctx )
    {
        EntityPlayerMP sender = ctx.getServerHandler().player;
        sender.getServerWorld().addScheduledTask(() ->
        {
            // Allow Computer/Tile updates as they may happen at any time.
            if(msg.isContainerNeeded())
            {
                Container container = sender.openContainer;
                if( !(container instanceof IContainerComputer) )
                {
                    return;
                }

                IComputer computer = ((IContainerComputer) container).getComputer();
                if( computer != this )
                {
                    return;
                }
            }

            int computerID = msg.getTargetID();

            ServerComputer computer = ComputerCraft.serverComputerRegistry.get( computerID );
            if( computer == null )
            {
                return;
            }

            // Receive packets sent from the client to the server
            if( msg instanceof ComputerInteraction )
            {
                computer.handleComputerInteraction( (ComputerInteraction) msg, sender );
            }
            else if( msg instanceof SetComputerLabel )
            {
                computer.setLabel( ((SetComputerLabel) msg).getLabel() );
            }
            else if( msg instanceof ComputerEvent )
            {
                ComputerEvent event = (ComputerEvent) msg;
                computer.queueEvent( event.getName(), event.getArguments() );
            }
            else if( msg instanceof RequestTileEntityUpdate )
            {
                RequestTileEntityUpdate rteu = (RequestTileEntityUpdate) msg;
                BlockPos pos = new BlockPos( rteu.getX(), rteu.getY(), rteu.getZ() );
                World world = sender.getEntityWorld();
                TileEntity tileEntity = world.getTileEntity( pos );
                if( tileEntity != null && tileEntity instanceof TileGeneric )
                {
                    TileGeneric generic = (TileGeneric) tileEntity;
                    SPacketUpdateTileEntity description = generic.getUpdatePacket();
                    sender.connection.sendPacket( description );
                }
            }
        });

        return null;
    }
}
