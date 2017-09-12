/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.shared.common;

import dan200.computercraft.core.terminal.Terminal;
import io.netty.buffer.ByteBuf;

public class ServerTerminal implements ITerminal
{
    private final boolean m_colour;
    private Terminal m_terminal;
    private boolean m_terminalChanged;
    private boolean m_terminalChangedLastFrame;

    public ServerTerminal( boolean colour )
    {
        m_colour = colour;
        m_terminal = null;
        m_terminalChanged = false;
        m_terminalChangedLastFrame = false;
    }

    public ServerTerminal( boolean colour, int terminalWidth, int terminalHeight )
    {
        m_colour = colour;
        m_terminal = new Terminal( terminalWidth, terminalHeight );
        m_terminalChanged = false;
        m_terminalChangedLastFrame = false;
    }

    public void resize( int width, int height )
    {
        if( m_terminal == null )
        {
            m_terminal = new Terminal( width, height );
            m_terminalChanged = true;
        }
        else
        {
            m_terminal.resize( width, height );
        }
    }

    public void delete()
    {
        if( m_terminal != null )
        {
            m_terminal = null;
            m_terminalChanged = true;
        }
    }

    public void update()
    {
        m_terminalChangedLastFrame = m_terminalChanged || (m_terminal != null && m_terminal.getChanged());
        if( m_terminal != null )
        {
            m_terminal.clearChanged();
        }
        m_terminalChanged = false;
    }

    public boolean hasTerminalChanged()
    {
        return m_terminalChangedLastFrame;
    }

    // ITerminal implementation

    @Override
    public Terminal getTerminal()
    {
        return m_terminal;
    }

    @Override
    public boolean isColour()
    {
        return m_colour;
    }

    // Networking stuff

    public void toBytes( ByteBuf buf )
    {
        buf.writeBoolean( m_colour );

        boolean terminal = m_terminal != null;
        buf.writeBoolean( terminal );

        if( terminal )
        {
            m_terminal.toBytes( buf );
        }
    }
}
