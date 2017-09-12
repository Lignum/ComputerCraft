/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.core.terminal;
import dan200.computercraft.shared.util.Palette;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.nio.charset.Charset;

public class Terminal implements IMessage
{    
    private static final String base16 = "0123456789abcdef";

    private int m_cursorX;
    private int m_cursorY;
    private boolean m_cursorBlink;
    private int m_cursorColour;
    private int m_cursorBackgroundColour;

    private int m_width;
    private int m_height;

    private TextBuffer m_text[];
    private TextBuffer m_textColour[];
    private TextBuffer m_backgroundColour[];

    private final Palette m_palette;

    private boolean m_changed;

    public Terminal( int width, int height )
    {
        m_width = width;
        m_height = height;
        
        m_cursorColour = 0;
        m_cursorBackgroundColour = 15;

        m_text = new TextBuffer[ m_height ];
        m_textColour = new TextBuffer[ m_height ];
        m_backgroundColour = new TextBuffer[ m_height ];
        for( int i=0; i<m_height; ++i )
        {
            m_text[i] = new TextBuffer( ' ', m_width );
            m_textColour[i] = new TextBuffer( base16.charAt( m_cursorColour ), m_width );
            m_backgroundColour[i] = new TextBuffer( base16.charAt( m_cursorBackgroundColour ), m_width );
        }
                
        m_cursorX = 0;
        m_cursorY = 0;
        m_cursorBlink = false;
        
        m_changed = false;

        m_palette = new Palette();
    }

    public void reset()
    {
        m_cursorColour = 0;
        m_cursorBackgroundColour = 15;
        m_cursorX = 0;
        m_cursorY = 0;
        m_cursorBlink = false;
        clear();
        m_changed = true;
        m_palette.resetColours();
    }
    
    public int getWidth() {
        return m_width;
    }
    
    public int getHeight() {
        return m_height;
    }
    
    public void resize( int width, int height )
    {
        if( width == m_width && height == m_height ) 
        {
            return;
        }
        
        int oldHeight = m_height;
        int oldWidth = m_width;
        TextBuffer[] oldText = m_text;
        TextBuffer[] oldTextColour = m_textColour;
        TextBuffer[] oldBackgroundColour = m_backgroundColour;

        m_width = width;
        m_height = height;

        m_text = new TextBuffer[ m_height ];
        m_textColour = new TextBuffer[ m_height ];
        m_backgroundColour = new TextBuffer[ m_height ];
        for( int i=0; i<m_height; ++i )
        {
            if( i >= oldHeight )
            {
                m_text[ i ] = new TextBuffer( ' ', m_width );
                m_textColour[ i ] = new TextBuffer( base16.charAt( m_cursorColour ), m_width );
                m_backgroundColour[ i ] = new TextBuffer( base16.charAt( m_cursorBackgroundColour ), m_width );
            }
            else if( m_width == oldWidth )
            {
                m_text[ i ] = oldText[ i ];
                m_textColour[ i ] = oldTextColour[ i ];
                m_backgroundColour[ i ] = oldBackgroundColour[ i ];
            }
            else
            {
                m_text[ i ] = new TextBuffer( ' ', m_width );
                m_textColour[ i ] = new TextBuffer( base16.charAt( m_cursorColour ), m_width );
                m_backgroundColour[ i ] = new TextBuffer( base16.charAt( m_cursorBackgroundColour ), m_width );
                m_text[ i ].write( oldText[ i ] );
                m_textColour[ i ].write( oldTextColour[ i ] );
                m_backgroundColour[ i ].write( oldBackgroundColour[ i ] );
            }
        }
        m_changed = true;
    }
    
    public void setCursorPos( int x, int y )
    {
        if( m_cursorX != x || m_cursorY != y )
        {
            m_cursorX = x;
            m_cursorY = y;
            m_changed = true;
        }
    }
    
    public void setCursorBlink( boolean blink )
    {
        if( m_cursorBlink != blink )
        {
            m_cursorBlink = blink;
            m_changed = true;
        }
    }
    
    public void setTextColour( int colour )
    {
        if( m_cursorColour != colour )
        {
            m_cursorColour = colour;
            m_changed = true;
        }
    }
    
    public void setBackgroundColour( int colour )
    {
        if( m_cursorBackgroundColour != colour )
        {
            m_cursorBackgroundColour = colour;
            m_changed = true;
        }
    }
    
    public int getCursorX()
    {
        return m_cursorX;
    }
    
    public int getCursorY()
    {
        return m_cursorY;
    }
    
    public boolean getCursorBlink()
    {
        return m_cursorBlink;
    }
    
    public int getTextColour()
    {
        return m_cursorColour;
    }
    
    public int getBackgroundColour()
    {
        return m_cursorBackgroundColour;
    }

    public Palette getPalette()
    {
        return m_palette;
    }

    public void blit( String text, String textColour, String backgroundColour )
    {
        int x = m_cursorX;
        int y = m_cursorY;
        if( y >= 0 && y < m_height )
        {
            m_text[ y ].write( text, x );
            m_textColour[ y ].write( textColour, x );
            m_backgroundColour[ y ].write( backgroundColour, x );
            m_changed = true;
        }
    }

    public void write( String text )
    {
        int x = m_cursorX;
        int y = m_cursorY;
        if( y >= 0 && y < m_height )
        {
            m_text[ y ].write( text, x );
            m_textColour[ y ].fill( base16.charAt( m_cursorColour ), x, x + text.length() );
            m_backgroundColour[ y ].fill( base16.charAt( m_cursorBackgroundColour ), x, x + text.length() );
            m_changed = true;
        }
    }
    
    public void scroll( int yDiff )
    {
        if( yDiff != 0 )
        {
            TextBuffer[] newText = new TextBuffer[ m_height ];
            TextBuffer[] newTextColour = new TextBuffer[ m_height ];
            TextBuffer[] newBackgroundColour = new TextBuffer[ m_height ];
            for( int y = 0; y < m_height; ++y )
            {
                int oldY = y + yDiff;
                if( oldY >= 0 && oldY < m_height )
                {
                    newText[ y ] = m_text[ oldY ];
                    newTextColour[ y ] = m_textColour[ oldY ];
                    newBackgroundColour[ y ] = m_backgroundColour[ oldY ];
                }
                else
                {
                    newText[ y ] = new TextBuffer( ' ', m_width );
                    newTextColour[ y ] = new TextBuffer( base16.charAt( m_cursorColour ), m_width );
                    newBackgroundColour[ y ] = new TextBuffer( base16.charAt( m_cursorBackgroundColour ), m_width );
                }
            }
            m_text = newText;
            m_textColour = newTextColour;
            m_backgroundColour = newBackgroundColour;
            m_changed = true;
        }
    }
    
    public void clear()
    {
        for( int y = 0; y < m_height; ++y )
        {
            m_text[ y ].fill( ' ' );
            m_textColour[ y ].fill( base16.charAt( m_cursorColour ) );
            m_backgroundColour[ y ].fill( base16.charAt( m_cursorBackgroundColour ) );
        }
        m_changed = true;
    }

    public void clearLine()
    {
        int y = m_cursorY;
        if( y >= 0 && y < m_height )
        {
            m_text[ y ].fill( ' ' );
            m_textColour[ y ].fill( base16.charAt( m_cursorColour ) );
            m_backgroundColour[ y ].fill( base16.charAt( m_cursorBackgroundColour ) );
            m_changed = true;
        }
    }
        
    public TextBuffer getLine( int y )
    {
        if( y >= 0 && y < m_height )
        {
            return m_text[ y ];
        }
        return null;
    }

    public void setLine( int y, String text, String textColour, String backgroundColour )
    {
        m_text[y].write( text );
        m_textColour[y].write( textColour );
        m_backgroundColour[y].write( backgroundColour );
        m_changed = true;
    }
    
    public TextBuffer getTextColourLine( int y )
    {
        if( y>=0 && y<m_height )
        {
            return m_textColour[ y ];
        }
        return null;
    }

    public TextBuffer getBackgroundColourLine( int y )
    {
        if( y>=0 && y<m_height )
        {
            return m_backgroundColour[ y ];
        }
        return null;
    }

    public boolean getChanged()
    {
        return m_changed;
    }

    public void setChanged()
    {
        m_changed = true;
    }

    public void clearChanged()
    {
        m_changed = false;
    }

    @Override
    public void toBytes( ByteBuf buf )
    {
        buf.writeInt( m_cursorX );
        buf.writeInt( m_cursorY );
        buf.writeBoolean( m_cursorBlink );
        buf.writeInt( m_cursorColour );
        buf.writeInt( m_cursorBackgroundColour );
        buf.writeInt( m_width );
        buf.writeInt( m_height );
        for( int n=0; n<m_height; ++n )
        {
            final Charset ascii = Charset.forName( "US-ASCII" );
            buf.writeCharSequence( m_text[n].toString(), ascii );
            buf.writeCharSequence( m_textColour[n].toString(), ascii );
            buf.writeCharSequence( m_backgroundColour[n].toString(), ascii );
        }
        boolean palette = m_palette != null;
        buf.writeBoolean( palette );
        if( palette )
        {
            m_palette.toBytes( buf );
        }
    }

    @Override
    public void fromBytes( ByteBuf buf )
    {
        m_cursorX = buf.readInt();
        m_cursorY = buf.readInt();
        m_cursorBlink = buf.readBoolean();
        m_cursorColour = buf.readInt();
        m_cursorBackgroundColour = buf.readInt();
        m_width = buf.readInt();
        m_height = buf.readInt();
        for( int n=0; n<m_height; ++n )
        {
            final Charset ascii = Charset.forName( "US-ASCII" );
            m_text[n].write( buf.readCharSequence( m_width, ascii ).toString() );
            m_textColour[n].write( buf.readCharSequence( m_width, ascii ).toString() );
            m_backgroundColour[n].write( buf.readCharSequence( m_width, ascii ).toString() );
        }
        boolean palette = buf.readBoolean();
        if( palette && m_palette != null )
        {
            m_palette.fromBytes( buf );
        }
        m_changed = true;
    }
}
