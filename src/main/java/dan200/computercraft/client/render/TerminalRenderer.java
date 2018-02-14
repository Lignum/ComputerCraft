package dan200.computercraft.client.render;

import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.core.terminal.TextBuffer;
import dan200.computercraft.shared.util.ArrayUtil;
import dan200.computercraft.shared.util.ColourUtils;
import dan200.computercraft.shared.util.Palette;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;

import java.io.Closeable;
import java.nio.FloatBuffer;
import java.util.Arrays;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

public class TerminalRenderer implements Closeable
{
    public static final int FONT_HEIGHT = 9;
    public static final int FONT_WIDTH = 6;

    public static final ResourceLocation font = new ResourceLocation( "computercraft", "textures/gui/term_font.png" );

    private int m_vertexBuffer = -1;
    private boolean m_closed = false;
    private int m_vertexCount = 0;

    private void uploadTerminalBuffer( FloatBuffer buffer )
    {
        if( m_vertexBuffer < 0 )
        {
            m_vertexBuffer = glGenBuffers();
        }

        glBindBuffer( GL_ARRAY_BUFFER, m_vertexBuffer );
        glBufferData( GL_ARRAY_BUFFER, buffer, GL_DYNAMIC_DRAW );
    }

    private static float[] getColourFromHexChar( Palette palette, char c, boolean greyscale )
    {
        int colour = "0123456789abcdef".indexOf( c );
        double[] rgb = palette.getColour( 15 - colour );

        if( greyscale )
        {
            ColourUtils.convertToMonochrome( rgb );
        }

        return ArrayUtil.doubleToFloatArray( rgb );
    }

    private static float[] getRowColour( Palette palette, TextBuffer hexString, int x, boolean greyscale )
    {
        return getColourFromHexChar( palette, hexString.charAt( x ), greyscale );
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
        return 1 + (int)c % 16 * ( FONT_WIDTH + 2);
    }

    private static int getCharV( char c )
    {
        return 1 + (int)c / 16 * ( FONT_HEIGHT + 2);
    }

    private void addCharacter( FloatBuffer buffer, float x, float y, float z, float w, float h, float[] colour, char c )
    {
        if( Character.isSpaceChar( c ) )
        {
            return;
        }

        final int ua = getCharU( c );
        final int va = getCharV( c );
        final int ub = ua + FONT_WIDTH;
        final int vb = va + FONT_HEIGHT;

        addRectangle( buffer, x, y, z, w, h, colour, ua / 256.0f, va / 256.0f, ub / 256.0f, vb / 256.0f );
    }

    private static final int VERTEX_SIZE = 8 * 4; // x, y, z, u, v, r, g, b
    private static final int VERTICES_PER_PIXEL = 6;

    private FloatBuffer buildTerminalBuffer( Terminal terminal, float marginX, float marginY, boolean showCursor, boolean greyscale )
    {
        m_vertexCount = 0;

        final Palette palette = terminal.getPalette();

        final int pixelCount = terminal.getWidth() * terminal.getHeight();
        FloatBuffer buffer = BufferUtils.createFloatBuffer( pixelCount * VERTICES_PER_PIXEL * VERTEX_SIZE );

        for( int y = 0; y < terminal.getHeight(); ++y )
        {
            TextBuffer rowBg = terminal.getBackgroundColourLine( y );
            TextBuffer rowFg = terminal.getTextColourLine( y );
            TextBuffer rowTxt = terminal.getLine( y );

            for( int x = 0; x < terminal.getWidth(); ++x )
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

                if( x == terminal.getWidth() - 1 )
                {
                    rw += marginX;
                }

                if( y == terminal.getHeight() - 1 )
                {
                    rh += marginY;
                }

                float[] bgColour = getRowColour( palette, rowBg, x, greyscale );
                addRectangle( buffer, rx, ry, 0.0f, rw, rh, bgColour, 0.999f, 0.999f, 0.999f, 0.999f );

                float[] fgColour = getRowColour( palette, rowFg, x, greyscale );
                char c = rowTxt.charAt( x );
                addCharacter( buffer, x, y, 0.001f, 1.0f, 1.0f, fgColour, c);
            }
        }

        if( showCursor )
        {
            final int tx = terminal.getCursorX();
            final int ty = terminal.getCursorY();
            double[] rgb = palette.getColour( 15 - terminal.getTextColour() );

            if( greyscale )
            {
                ColourUtils.convertToMonochrome( rgb );
            }

            float[] cursorColour = ArrayUtil.doubleToFloatArray( rgb );
            addCharacter( buffer, tx, ty, 0.001f, 1.0f, 1.0f, cursorColour, '_' );
        }

        buffer.flip();
        return buffer;
    }

    public void refreshTerminalBuffer( Terminal terminal, float marginX, float marginY, boolean showCursor, boolean greyscale )
    {
        if( m_closed )
        {
            return;
        }

        uploadTerminalBuffer( buildTerminalBuffer( terminal, marginX, marginY, showCursor, greyscale ) );
    }

    private void setupClientState()
    {
        glBindBuffer( GL_ARRAY_BUFFER, m_vertexBuffer );

        GlStateManager.glEnableClientState( GL_VERTEX_ARRAY );
        glVertexPointer( 3, GL_FLOAT, VERTEX_SIZE, 0L );

        GlStateManager.glEnableClientState( GL_TEXTURE_COORD_ARRAY );
        glTexCoordPointer( 2, GL_FLOAT, VERTEX_SIZE, 3 * 4L );

        GlStateManager.glEnableClientState( GL_COLOR_ARRAY );
        glColorPointer( 3, GL_FLOAT, VERTEX_SIZE, (3 + 2) * 4L );
    }

    private void destroyClientState()
    {
        GlStateManager.glDisableClientState( GL_COLOR_ARRAY );
        GlStateManager.glDisableClientState( GL_TEXTURE_COORD_ARRAY );
        GlStateManager.glDisableClientState( GL_VERTEX_ARRAY );

        glBindBuffer( GL_ARRAY_BUFFER, 0 );
    }

    public void renderTerminal( Terminal terminal, float marginX, float marginY, boolean showCursor, boolean greyscale )
    {
        if( m_closed )
        {
            return;
        }

        final Minecraft mc = Minecraft.getMinecraft();

        if( m_vertexBuffer < 0 )
        {
            refreshTerminalBuffer( terminal, marginX, marginY, showCursor, greyscale );
        }

        setupClientState();
        {
            mc.getTextureManager().bindTexture( font );
            GlStateManager.glDrawArrays( GL_TRIANGLES, 0, m_vertexCount );
        }
        destroyClientState();
    }

    @Override
    public void close()
    {
        glDeleteBuffers( m_vertexBuffer );
        m_vertexBuffer = -1;
        m_closed = true;
    }
}