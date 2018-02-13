/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.client.render;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.client.gui.FixedWidthFontRenderer;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.core.terminal.TextBuffer;
import dan200.computercraft.shared.common.ClientTerminal;
import dan200.computercraft.shared.common.ITerminal;
import dan200.computercraft.shared.peripheral.monitor.TileMonitor;
import dan200.computercraft.shared.util.Colour;
import dan200.computercraft.shared.util.DirectionUtil;
import dan200.computercraft.shared.util.Palette;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

public class TileEntityMonitorRenderer extends TileEntitySpecialRenderer<TileMonitor>
{
    public TileEntityMonitorRenderer()
    {
    }

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
        else
        {
            origin.m_lastRenderFrame = renderFrame;
        }

        boolean redraw = origin.pollChanged();
        BlockPos monitorPos = monitor.getPos();
        BlockPos originPos = origin.getPos();
        posX += originPos.getX() - monitorPos.getX();
        posY += originPos.getY() - monitorPos.getY();
        posZ += originPos.getZ() - monitorPos.getZ();

        // Determine orientation
        EnumFacing dir = origin.getDirection();
        EnumFacing front = origin.getFront();
        float yaw = DirectionUtil.toYawAngle( dir );
        float pitch = DirectionUtil.toPitchAngle( front );

        ITerminal term = origin.getTerminal();
        if( term == null || !(term instanceof ClientTerminal) )
        {
            return;
        }

        ClientTerminal clientTerminal = (ClientTerminal)origin.getTerminal();
        Terminal terminal = clientTerminal.getTerminal();

        if( terminal == null )
        {
            return;
        }

        if( origin.m_renderer == null )
        {
            origin.m_renderer = new TerminalRenderer( terminal );
        }

        origin.m_renderer.renderTerminal( posX, posY, posZ, yaw, pitch );
    }
}
