package dan200.computercraft.client.render;

import dan200.computercraft.client.gui.FixedWidthFontRenderer;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.core.terminal.TextBuffer;
import dan200.computercraft.shared.peripheral.monitor.TileMonitor;
import net.minecraft.client.Minecraft;
import org.lwjgl.BufferUtils;

import java.io.Closeable;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

public class TerminalRenderer implements Closeable
{
    private final Terminal m_terminal;
    private final int m_vertexBuffer;
    private double m_xScale, m_yScale;
    private boolean m_closed = false;
    private int m_vertexCount = 0;

    public TerminalRenderer( Terminal terminal, double xScale, double yScale )
    {
        m_terminal = terminal;
        m_vertexBuffer = glGenBuffers();

        refreshTerminalBuffer( xScale, yScale );
    }

    private void uploadTerminalBuffer( FloatBuffer buffer )
    {
        glBindBuffer( GL_ARRAY_BUFFER, m_vertexBuffer );
        glBufferData( GL_ARRAY_BUFFER, buffer, GL_DYNAMIC_DRAW );
    }

    private static float[] getRowColour( Terminal terminal, TextBuffer hexString, int x )
    {
        int colour = "0123456789abcdef".indexOf( hexString.charAt( x ) );
        double[] rgb = terminal.getPalette().getColour( 15 - colour );
        float[] frgb = new float[ rgb.length ];

        for( int i = 0; i < rgb.length; ++i )
        {
            frgb[i] = (float) rgb[i];
        }

        return frgb;
    }

    private void addRectangle( FloatBuffer buffer, float x, float y, float z, float w, float h, float[] colour, float ua, float va, float ub, float vb )
    {
        final float xa = x;
        final float ya = y;
        final float xb = xa + w;
        final float yb = ya + h;

        buffer.put( xa ).put( ya ).put( z ).put( ua ).put( va ).put( colour ); // Top left
        buffer.put( xa ).put( yb ).put( z ).put( ua ).put( vb ).put( colour ); // Bottom left
        buffer.put( xb ).put( ya ).put( z ).put( ub ).put( va ).put( colour ); // Top right
        buffer.put( xb ).put( ya ).put( z ).put( ub ).put( va ).put( colour ); // Top right
        buffer.put( xa ).put( yb ).put( z ).put( ua ).put( vb ).put( colour ); // Bottom left
        buffer.put( xb ).put( yb ).put( z ).put( ub ).put( vb ).put( colour ); // Bottom right

        m_vertexCount += 6;
    }

    private static int getCharU( char c )
    {
        return 1 + (int)c % 16 * (FixedWidthFontRenderer.FONT_WIDTH + 2);
    }

    private static int getCharV( char c )
    {
        return 1 + (int)c / 16 * (FixedWidthFontRenderer.FONT_HEIGHT + 2);
    }

    private void addCharacter( FloatBuffer buffer, float x, float y, float z, float w, float h, float[] colour, char c )
    {
        if( Character.isSpaceChar( c ) )
        {
            return;
        }

        final int ua = getCharU( c );
        final int va = getCharV( c );
        final int ub = ua + FixedWidthFontRenderer.FONT_WIDTH;
        final int vb = va + FixedWidthFontRenderer.FONT_HEIGHT;

        addRectangle( buffer, x, y, z, w, h, colour, ua / 256.0f, va / 256.0f, ub / 256.0f, vb / 256.0f );
    }

    private static final int VERTEX_SIZE = 8 * 4; // x, y, z, u, v, r, g, b
    private static final int VERTICES_PER_PIXEL = 6;

    private FloatBuffer buildTerminalBuffer()
    {
        m_vertexCount = 0;

        final int pixelCount = m_terminal.getWidth() * m_terminal.getHeight();
        FloatBuffer buffer = BufferUtils.createFloatBuffer( pixelCount * VERTICES_PER_PIXEL * VERTEX_SIZE );

        final float marginX = (float)(TileMonitor.RENDER_MARGIN / m_xScale);
        final float marginY = (float)(TileMonitor.RENDER_MARGIN / m_yScale);

        for( int y = 0; y < m_terminal.getHeight(); ++y )
        {
            TextBuffer rowBg = m_terminal.getBackgroundColourLine( y );
            TextBuffer rowFg = m_terminal.getTextColourLine( y );
            TextBuffer rowTxt = m_terminal.getLine( y );

            for( int x = 0; x < m_terminal.getWidth(); ++x )
            {
                float rx = x;
                float ry = y;
                float rw = 1.0f;
                float rh = 1.0f;

                if( x == 0 )
                {
                    rx -= marginX;
                    rw += marginX;
                }

                if( y == 0 )
                {
                    ry -= marginY;
                    rh += marginY;
                }

                if( x == m_terminal.getWidth() - 1 )
                {
                    rw += marginX;
                }

                if( y == m_terminal.getHeight() - 1 )
                {
                    rh += marginY;
                }

                float[] bgColour = getRowColour( m_terminal, rowBg, x );
                addRectangle( buffer, rx, ry, 0.0f, rw, rh, bgColour, 0.999f, 0.999f, 0.999f, 0.999f );

                float[] fgColour = getRowColour( m_terminal, rowFg, x );
                char c = rowTxt.charAt( x );
                addCharacter( buffer, x, y, 0.001f, 1.0f, 1.0f, fgColour, c);
            }
        }

        buffer.flip();
        return buffer;
    }

    public void refreshTerminalBuffer( double xScale, double yScale )
    {
        if( m_closed )
        {
            return;
        }

        m_xScale = xScale;
        m_yScale = yScale;
        uploadTerminalBuffer( buildTerminalBuffer() );
    }

    private void setupClientState()
    {
        glBindBuffer( GL_ARRAY_BUFFER, m_vertexBuffer );

        glEnableClientState( GL_VERTEX_ARRAY );
        glVertexPointer( 3, GL_FLOAT, VERTEX_SIZE, 0L );

        glEnableClientState( GL_TEXTURE_COORD_ARRAY );
        glTexCoordPointer( 2, GL_FLOAT, VERTEX_SIZE, 3 * 4L );

        glEnableClientState( GL_COLOR_ARRAY );
        glColorPointer( 3, GL_FLOAT, VERTEX_SIZE, (3 + 2) * 4L );
    }

    private void destroyClientState()
    {
        glDisableClientState( GL_COLOR_ARRAY );
        glDisableClientState( GL_TEXTURE_COORD_ARRAY );
        glDisableClientState( GL_VERTEX_ARRAY );

        glBindBuffer( GL_ARRAY_BUFFER, 0 );
    }

    public void renderTerminal()
    {
        if( m_closed )
        {
            return;
        }

        final Minecraft mc = Minecraft.getMinecraft();

        if( m_terminal.getChanged() )
        {
            refreshTerminalBuffer( m_xScale, m_yScale );
            m_terminal.clearChanged();
        }

        setupClientState();
        {
            mc.getTextureManager().bindTexture( FixedWidthFontRenderer.font );
            glDrawArrays( GL_TRIANGLES, 0, m_vertexCount );
        }
        destroyClientState();
    }

    @Override
    public void close()
    {
        glDeleteBuffers( m_vertexBuffer );
        m_closed = true;
    }
}