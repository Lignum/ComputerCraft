package dan200.computercraft.client.render;

import dan200.computercraft.client.gui.FixedWidthFontRenderer;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.core.terminal.TextBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.BufferUtils;

import java.io.Closeable;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

public class TerminalRenderer implements Closeable
{
    private final Terminal m_terminal;
    private final int m_vertexBuffer;
    private boolean m_closed = false;

    public TerminalRenderer( Terminal terminal )
    {
        m_terminal = terminal;
        m_vertexBuffer = glGenBuffers();

        refreshTerminalBuffer();
    }

    private void uploadTerminalBuffer(FloatBuffer buffer)
    {
        glBindBuffer( GL_ARRAY_BUFFER, m_vertexBuffer );
        glBufferData( GL_ARRAY_BUFFER, buffer, GL_DYNAMIC_DRAW );
    }

    private static float[] getRowColour( Terminal terminal, TextBuffer hexString, int x )
    {
        int colour = "0123456789abcdef".indexOf( hexString.charAt( x ) );
        double[] rgb = terminal.getPalette().getColour( colour );
        float[] frgb = new float[ rgb.length ];

        for( int i = 0; i < rgb.length; ++i )
        {
            frgb[i] = (float) rgb[i];
        }

        return frgb;
    }

    private static final int VERTEX_SIZE = 7 * 4; // x, y, u, v, r, g, b
    private final static int VERTICES_PER_PIXEL = 6;

    private FloatBuffer buildTerminalBuffer()
    {
        final int pixelCount = m_terminal.getWidth() * m_terminal.getHeight();
        FloatBuffer buffer = BufferUtils.createFloatBuffer( pixelCount * VERTICES_PER_PIXEL * VERTEX_SIZE );

        for ( int y = 0; y < m_terminal.getHeight(); ++y )
        {
            TextBuffer rowBg = m_terminal.getBackgroundColourLine( y );
            TextBuffer rowFg = m_terminal.getTextColourLine( y );
            TextBuffer rowTxt = m_terminal.getLine( y );

            for ( int x = 0; x < m_terminal.getWidth(); ++x )
            {
                float[] bgColour = getRowColour( m_terminal, rowBg, x );
                buffer.put( (float)x ).put( (float)y ).put( 0.0f ).put( 0.0f ).put( bgColour ); // Top left
                buffer.put( (float)x ).put( (float)y + 1.0f ).put( 0.0f ).put( 0.0f ).put( bgColour ); // Bottom left
                buffer.put( (float)x + 1.0f ).put( (float)y ).put( 0.0f ).put( 0.0f ).put( bgColour ); // Top right
                buffer.put( (float)x + 1.0f ).put( (float)y ).put( 0.0f ).put( 0.0f ).put( bgColour ); // Top right
                buffer.put( (float)x ).put( (float)y + 1.0f ).put( 0.0f ).put( 0.0f ).put( bgColour ); // Bottom left
                buffer.put( (float)x + 1.0f ).put( (float)y + 1.0f ).put( 0.0f ).put( 0.0f ).put( bgColour ); // Bottom right
            }
        }

        buffer.flip();
        return buffer;
    }

    public void refreshTerminalBuffer()
    {
        if( m_closed )
        {
            return;
        }

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
            refreshTerminalBuffer();
            m_terminal.clearChanged();
        }

        setupClientState();
        {
            mc.getTextureManager().bindTexture( FixedWidthFontRenderer.background );
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
