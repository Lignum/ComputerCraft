/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.computer.core;

import dan200.computercraft.shared.common.ClientTerminal;
import dan200.computercraft.shared.network.PacketHandler;
import dan200.computercraft.shared.network.messages.ComputerEvent;
import dan200.computercraft.shared.network.messages.ComputerInteraction;
import dan200.computercraft.shared.network.messages.ComputerState;

public class ClientComputer extends ClientTerminal
    implements IComputer
{
    private final int m_instanceID;

    private int m_computerID;
    private String m_label;
    private boolean m_on;
    private boolean m_blinking;
    private boolean m_changed;
    private int m_modemLightColour;

    private boolean m_changedLastFrame;

    public ClientComputer( int instanceID )
    {
        super( false );
        m_instanceID = instanceID;

        m_computerID = -1;
        m_label = null;
        m_on = false;
        m_blinking = false;
        m_changed = true;
        m_changedLastFrame = false;
    }

    @Override
    public void update()
    {
        super.update();
        m_changedLastFrame = m_changed;
        m_changed = false;
    }

    protected int getModemLightColour()
    {
        return m_modemLightColour;
    }

    public void setModemLightColour( int modemLightColour )
    {
        m_modemLightColour = modemLightColour;
    }

    public boolean hasOutputChanged()
    {
        return m_changedLastFrame;
    }

    public void requestState()
    {
        interact( ComputerInteraction.ActionType.RequestComputerUpdate );
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
        return m_computerID;
    }

    @Override
    public String getLabel()
    {
        return m_label;
    }

    @Override
    public boolean isOn()
    {
        return m_on;
    }

    @Override
    public boolean isCursorDisplayed()
    {
        return m_on && m_blinking;
    }

    private void interact( ComputerInteraction.ActionType action )
    {
        PacketHandler.INSTANCE.sendToServer( new ComputerInteraction( m_computerID, action ) );
    }

    @Override
    public void turnOn()
    {
        interact( ComputerInteraction.ActionType.TurnOn );
    }

    @Override
    public void shutdown()
    {
        interact( ComputerInteraction.ActionType.Shutdown );
    }

    @Override
    public void reboot()
    {
        interact( ComputerInteraction.ActionType.Reboot );
    }

    @Override
    public void queueEvent( String event )
    {
        queueEvent( event, null );
    }

    @Override
    public void queueEvent( String event, Object[] arguments )
    {
        PacketHandler.INSTANCE.sendToServer( new ComputerEvent( m_computerID, event, arguments ) );
    }

    public void syncState( ComputerState state )
    {
        int oldID = m_computerID;
        String oldLabel = m_label;
        boolean oldOn = m_on;
        boolean oldBlinking = m_blinking;

        m_computerID = state.getTargetID();
        m_label = state.getLabel();
        m_on = state.isOn();
        m_blinking = state.isBlinking();

        if( m_computerID != oldID || m_on != oldOn || m_blinking != oldBlinking || !m_label.equals( oldLabel ) )
        {
            m_changed = true;
        }
    }
}
