/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.network;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.shared.network.messages.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler
{
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(ComputerCraft.MOD_ID );

    static
    {
        int id = 0;
        INSTANCE.registerMessage( new ClientPacketHandler(), ComputerState.class, id++, Side.CLIENT );
        INSTANCE.registerMessage( new ClientPacketHandler(), ComputerDeleted.class, id++, Side.CLIENT );

        INSTANCE.registerMessage( new ServerPacketHandler(), ComputerEvent.class, id++, Side.SERVER );
        INSTANCE.registerMessage( new ServerPacketHandler(), SetComputerLabel.class, id++, Side.SERVER );
        INSTANCE.registerMessage( new ServerPacketHandler(), ComputerInteraction.class, id++, Side.SERVER );
    }
}
