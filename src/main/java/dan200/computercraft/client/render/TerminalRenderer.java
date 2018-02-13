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

    private void addRectangle( FloatBuffer buffer, float x, float y, float w, float h, float[] colour, float ua, float va, float ub, float vb )
    {
        final float xa = x;
        final float ya = y;
        final float xb = xa + w;
        final float yb = ya + h;

        buffer.put( xa ).put( ya ).put( ua ).put( va ).put( colour ); // Top left
        buffer.put( xa ).put( yb ).put( ua ).put( vb ).put( colour ); // Bottom left
        buffer.put( xb ).put( ya ).put( ub ).put( va ).put( colour ); // Top right
        buffer.put( xb ).put( ya ).put( ub ).put( va ).put( colour ); // Top right
        buffer.put( xa ).put( yb ).put( ua ).put( vb ).put( colour ); // Bottom left
        buffer.put( xb ).put( yb ).put( ub ).put( vb ).put( colour ); // Bottom right
    }

    private static final int VERTEX_SIZE = 7 * 4; // x, y, u, v, r, g, b
    private static final int VERTICES_PER_PIXEL = 6;

    private FloatBuffer buildTerminalBuffer()
    {
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
                addRectangle( buffer, rx, ry, rw, rh, bgColour, 0.999f, 0.999f, 0.999f, 0.999f );
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
        glVertexPointer( 2, GL_FLOAT, VERTEX_SIZE, 0L );

        glEnableClientState( GL_TEXTURE_COORD_ARRAY );
        glTexCoordPointer( 2, GL_FLOAT, VERTEX_SIZE, 2 * 4L );

        glEnableClientState( GL_COLOR_ARRAY );
        glColorPointer( 3, GL_FLOAT, VERTEX_SIZE, (2 + 2) * 4L );
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
            glDrawArrays( GL_TRIANGLES, 0, VERTICES_PER_PIXEL * m_terminal.getWidth() * m_terminal.getHeight() );
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