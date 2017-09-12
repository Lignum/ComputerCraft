package dan200.computercraft.shared.network.messages;

import dan200.computercraft.shared.common.ServerTerminal;
import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

public class ComputerState implements ClientMessage
{
    private int targetID;
    private String label;
    private boolean on;
    private boolean blinking;
    private int modemLightColour;
    private ServerTerminal terminal;

    public ComputerState( int targetID, String label, boolean on, boolean blinking, ServerTerminal terminal, int modemLightColour )
    {
        this.targetID = targetID;
        this.label = label;
        this.on = on;
        this.terminal = terminal;
        this.blinking = blinking;
        this.modemLightColour = modemLightColour;
    }

    public ComputerState( int targetID, String label, boolean on, boolean blinking, ServerTerminal terminal )
    {
        this( targetID, label, on, blinking, terminal, 0 );
    }

    public int getTargetID()
    {
        return targetID;
    }

    public String getLabel()
    {
        return label;
    }

    public boolean isOn()
    {
        return on;
    }

    public boolean isBlinking()
    {
        return blinking;
    }

    public int getModemLightColour()
    {
        return modemLightColour;
    }

    @Override
    public void fromBytes( ByteBuf buf )
    {
        targetID = buf.readInt();

        boolean hasLabel = buf.readBoolean();
        if( hasLabel )
        {
            int labelLength = buf.readInt();
            label = buf.readCharSequence( labelLength, Charset.forName( "UTF-8" ) ).toString();
        }
        on = buf.readBoolean();
        blinking = buf.readBoolean();
        modemLightColour = buf.readInt();
    }

    @Override
    public void toBytes( ByteBuf buf )
    {
        buf.writeInt( targetID );
        boolean hasLabel = label != null;
        buf.writeBoolean( hasLabel );
        if( hasLabel )
        {
            buf.writeInt( label.length() );
            buf.writeCharSequence( label, Charset.forName( "UTF-8" ));
        }
        buf.writeBoolean( on );
        buf.writeBoolean( blinking );
        buf.writeInt( modemLightColour );
    }
}
