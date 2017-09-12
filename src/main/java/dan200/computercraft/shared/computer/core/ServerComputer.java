/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.computer.core;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.filesystem.IMount;
import dan200.computercraft.api.filesystem.IWritableMount;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.core.apis.IAPIEnvironment;
import dan200.computercraft.core.apis.ILuaAPI;
import dan200.computercraft.core.computer.Computer;
import dan200.computercraft.core.computer.IComputerEnvironment;
import dan200.computercraft.shared.common.ServerTerminal;
import dan200.computercraft.shared.network.PacketHandler;
import dan200.computercraft.shared.network.messages.ComputerDeleted;
import dan200.computercraft.shared.network.messages.ComputerInteraction;
import dan200.computercraft.shared.network.messages.ComputerState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.io.InputStream;

public class ServerComputer extends ServerTerminal
    implements IComputer, IComputerEnvironment
{
    private final int m_instanceID;

    private World m_world;
    private BlockPos m_position;

    private final Computer m_computer;
    private boolean m_changed;

    private boolean m_changedLastFrame;
    private int m_ticksSincePing;

    private int m_modemLightColour;

    public ServerComputer( World world, int computerID, String label, int instanceID, ComputerFamily family, int terminalWidth, int terminalHeight )
    {
        super( family != ComputerFamily.Normal, terminalWidth, terminalHeight );
        m_instanceID = instanceID;

        m_world = world;
        m_position = null;

        m_computer = new Computer( this, getTerminal(), computerID );
        m_computer.setLabel( label );
        m_changed = false;

        m_changedLastFrame = false;
        m_ticksSincePing = 0;
    }

    public World getWorld()
    {
        return m_world;
    }

    public void setWorld( World world )
    {
        m_world = world;
    }

    public BlockPos getPosition()
    {
        return m_position;
    }

    public void setPosition( BlockPos pos )
    {
        m_position = new BlockPos( pos );
    }

    public IAPIEnvironment getAPIEnvironment()
    {
        return m_computer.getAPIEnvironment();
    }

    @Override
    public void update()
    {
        super.update();
        m_computer.advance( 0.05 );

        m_changedLastFrame = m_changed || m_computer.pollChanged();
        m_computer.clearChanged();
        m_changed = false;

        m_ticksSincePing++;
    }

    public void keepAlive()
    {
        m_ticksSincePing = 0;
    }

    public boolean hasTimedOut()
    {
        return m_ticksSincePing > 100;
    }

    public boolean hasOutputChanged()
    {
        return m_changedLastFrame;
    }

    public void unload()
    {
        m_computer.unload();
    }

    public void broadcastState()
    {
        PacketHandler.INSTANCE.sendToAll( createState() );
    }

    public void sendState( EntityPlayerMP player )
    {
        PacketHandler.INSTANCE.sendTo( createState(), player );
    }

    public void broadcastDelete()
    {
        PacketHandler.INSTANCE.sendToAll( new ComputerDeleted( m_instanceID ) );
    }

    protected int getModemLightColour()
    {
        return m_modemLightColour;
    }

    protected void setModemLightColour( int modemLightColour )
    {
        m_modemLightColour = modemLightColour;
    }

    public IWritableMount getRootMount()
    {
        return m_computer.getRootMount();
    }

    public int assignID()
    {
        return m_computer.assignID();
    }

    public void setID( int id )
    {
        m_computer.setID( id );
    }

    // IComputer

    @Override
    public int getInstanceID()
    {
        return m_instanceID;
    }

    @Override
    public int getID()
    {
        return m_computer.getID();
    }

    @Override
    public String getLabel()
    {
        return m_computer.getLabel();
    }

    @Override
    public boolean isOn()
    {
        return m_computer.isOn();
    }

    @Override
    public boolean isCursorDisplayed()
    {
        return m_computer.isOn() && m_computer.isBlinking();
    }

    @Override
    public void turnOn()
    {
        m_computer.turnOn();
    }

    @Override
    public void shutdown()
    {
        m_computer.shutdown();
    }

    @Override
    public void reboot()
    {
        m_computer.reboot();
    }

    @Override
    public void queueEvent( String event )
    {
        queueEvent( event, null );
    }

    @Override
    public void queueEvent( String event, Object[] arguments )
    {
        m_computer.queueEvent( event, arguments );
    }

    public int getRedstoneOutput( int side )
    {
        return m_computer.getRedstoneOutput( side );
    }

    public void setRedstoneInput( int side, int level )
    {
        m_computer.setRedstoneInput( side, level );
    }

    public int getBundledRedstoneOutput( int side )
    {
        return m_computer.getBundledRedstoneOutput( side );
    }

    public void setBundledRedstoneInput( int side, int combination )
    {
        m_computer.setBundledRedstoneInput( side, combination );
    }

    public void addAPI( ILuaAPI api )
    {
        m_computer.addAPI( api );
    }

    public void setPeripheral( int side, IPeripheral peripheral )
    {
        m_computer.setPeripheral( side, peripheral );
    }

    public IPeripheral getPeripheral( int side )
    {
        return m_computer.getPeripheral( side );
    }

    public void setLabel( String label )
    {
        m_computer.setLabel( label );
    }

    // IComputerEnvironment implementation

    @Override
    public double getTimeOfDay()
    {
        return (double)((m_world.getWorldTime() + 6000) % 24000) / 1000.0;
    }

    @Override
    public int getDay()
    {
        return (int)((m_world.getWorldTime() + 6000) / 24000) + 1;
    }

    @Override
    public IWritableMount createSaveDirMount( String subPath, long capacity )
    {
        return ComputerCraftAPI.createSaveDirMount( m_world, subPath, capacity );
    }

    @Override
    public IMount createResourceMount( String domain, String subPath )
    {
        return ComputerCraftAPI.createResourceMount( ComputerCraft.class, domain, subPath );
    }

    @Override
    public InputStream createResourceFile( String domain, String subPath )
    {
        return ComputerCraft.getResourceFile( ComputerCraft.class, domain, subPath );
    }

    @Override
    public long getComputerSpaceLimit()
    {
        return ComputerCraft.computerSpaceLimit;
    }

    @Override
    public String getHostString()
    {
        return "ComputerCraft ${version} (Minecraft " + Loader.MC_VERSION + ")";
    }

    @Override
    public int assignNewID()
    {
        return ComputerCraft.createUniqueNumberedSaveDir( m_world, "computer" );
    }

    public void setChanged()
    {
        m_changed = true;
    }

    // Networking stuff

    public ComputerState createState()
    {
        return new ComputerState(
            m_computer.getID(),
            m_computer.getLabel(),
            m_computer.isOn(),
            m_computer.isBlinking(),
            this
        );
    }

    public void handleComputerInteraction( ComputerInteraction ci, EntityPlayerMP sender )
    {
        switch(ci.getAction())
        {
            case TurnOn:
            {
                // A player has turned the computer on
                turnOn();
                break;
            }
            case Reboot:
            {
                // A player has held down ctrl+r
                reboot();
                break;
            }
            case Shutdown:
            {
                // A player has held down ctrl+s
                shutdown();
                break;
            }
            case RequestComputerUpdate:
            {
                sendState( sender );
                break;
            }
        }
    }
}
