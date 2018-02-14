/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.client.render;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.shared.common.ClientTerminal;
import dan200.computercraft.shared.common.ITerminal;
import dan200.computercraft.shared.peripheral.monitor.TileMonitor;
import dan200.computercraft.shared.util.Colour;
import dan200.computercraft.shared.util.DirectionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import static org.lwjgl.opengl.GL11.*;

public class TileEntityMonitorRenderer extends TileEntitySpecialRenderer<TileMonitor>
{
    @Override
    public void render( TileMonitor tileEntity, double posX, double posY, double posZ, float f, int i, float f2 )
    {
        if( tileEntity != null )
        {
            renderMonitorAt( tileEntity, posX, posY, posZ, f, i );
        }
    }

    private void renderMonitorAt( TileMonitor monitor, double posX, double posY, double posZ, float f, int i )
    {
        // Render from the origin monitor
        TileMonitor origin = monitor.getOrigin();
        if( origin == null )
        {
            return;
        }

        // Ensure each monitor is rendered only once
        long renderFrame = ComputerCraft.getRenderFrame();
        if( origin.m_lastRenderFrame == renderFrame )
        {
            return;
        }
        origin.m_lastRenderFrame = renderFrame;

        final Minecraft mc = Minecraft.getMinecraft();

        BlockPos monitorPos = monitor.getPos();
        BlockPos originPos = origin.getPos();
        posX += originPos.getX() - monitorPos.getX();
        posY += originPos.getY() - monitorPos.getY();
        posZ += originPos.getZ() - monitorPos.getZ();

        // Determine orientation
        EnumFacing dir = origin.getDirection();
        EnumFacing front = origin.getFront();
        final float yaw = DirectionUtil.toYawAngle( dir );
        final float pitch = DirectionUtil.toPitchAngle( front );

        final double xSize = (double)origin.getWidth() - 2.0 * ( TileMonitor.RENDER_MARGIN + TileMonitor.RENDER_BORDER );
        final double ySize = (double)origin.getHeight() - 2.0 * ( TileMonitor.RENDER_MARGIN + TileMonitor.RENDER_BORDER );

        ITerminal term = origin.getTerminal();
        if( term == null || !(term instanceof ClientTerminal) )
        {
            return;
        }

        ClientTerminal clientTerminal = (ClientTerminal)origin.getTerminal();
        Terminal terminal = clientTerminal.getTerminal();

        GlStateManager.pushMatrix();
        GlStateManager.translate( posX + 0.5, posY + 0.5, posZ + 0.5 );
        GlStateManager.rotate( -yaw, 0.0f, 1.0f, 0.0f );
        GlStateManager.rotate( pitch, 1.0f, 0.0f, 0.0f );
        GlStateManager.translate(
                -0.5 + TileMonitor.RENDER_BORDER + TileMonitor.RENDER_MARGIN,
                ((double)origin.getHeight() - 0.5) - (TileMonitor.RENDER_BORDER + TileMonitor.RENDER_MARGIN),
                0.5
        );

        if( terminal != null )
        {
            final int width = terminal.getWidth();
            final int height = terminal.getHeight();

            final double xScale = xSize / (double)width;
            final double yScale = ySize / (double)height;

            final float marginX = (float)(TileMonitor.RENDER_MARGIN / xScale);
            final float marginY = (float)(TileMonitor.RENDER_MARGIN / yScale);
            GlStateManager.scale( xScale, -yScale, 1.0f );

            if( origin.m_renderer == null )
            {
                origin.m_renderer = new TerminalRenderer();
            }

            if( origin.pollChanged() )
            {
                origin.m_renderer.refreshTerminalBuffer( terminal, marginX, marginY, false );
            }

            GlStateManager.disableLighting();
            mc.entityRenderer.disableLightmap();

            origin.m_renderer.renderTerminal( terminal, marginX, marginY, false );

            mc.entityRenderer.enableLightmap();
            GlStateManager.enableLighting();
        }
        else
        {
            mc.getTextureManager().bindTexture( TerminalRenderer.font );
            final Colour colour = Colour.Black;

            final float r = colour.getR();
            final float g = colour.getG();
            final float b = colour.getB();

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin( GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_COLOR );
            buffer.pos( -TileMonitor.RENDER_MARGIN, TileMonitor.RENDER_MARGIN, 0.0D ).tex( 0.999, 0.999 ).color( r, g, b, 1.0f ).endVertex();
            buffer.pos( -TileMonitor.RENDER_MARGIN, -ySize - TileMonitor.RENDER_MARGIN, 0.0 ).tex( 0.999, 0.999 ).color( r, g, b, 1.0f ).endVertex();
            buffer.pos( xSize + TileMonitor.RENDER_MARGIN, TileMonitor.RENDER_MARGIN, 0.0D ).tex( 0.999, 0.999 ).color( r, g, b, 1.0f ).endVertex();
            buffer.pos( xSize + TileMonitor.RENDER_MARGIN, -ySize - TileMonitor.RENDER_MARGIN, 0.0 ).tex( 0.999, 0.999 ).color( r, g, b, 1.0f ).endVertex();
            tessellator.draw();
        }

        GlStateManager.popMatrix();
    }
}
