package dan200.computercraft.shared.network.messages;

import io.netty.buffer.ByteBuf;

import java.util.Arrays;
import java.util.Optional;

public class ComputerInteraction implements ServerMessage
{
    public enum ActionType
    {
        TurnOn(0, true),
        Reboot(1, true),
        Shutdown(2, true),
        RequestComputerUpdate(3, false);

        private final byte id;
        private final boolean containerNeeded;

        ActionType( int id, boolean containerNeeded )
        {
            this.id = (byte) id;
            this.containerNeeded = containerNeeded;
        }

        public byte getID()
        {
            return id;
        }

        public static Optional<ActionType> fromID( int id )
        {
            return Arrays.stream( ActionType.values() )
                .filter( a -> a.id == (byte)id )
                .findFirst();
        }

        public boolean isContainerNeeded()
        {
            return containerNeeded;
        }
    }

    private int targetID;
    private ActionType action;

    public ComputerInteraction( int targetID, ActionType action )
    {
        this.targetID = targetID;
        this.action = action;
    }

    @Override
    public int getTargetID()
    {
        return targetID;
    }

    public ActionType getAction()
    {
        return action;
    }

    @Override
    public boolean isContainerNeeded()
    {
        return action.isContainerNeeded();
    }

    @Override
    public void fromBytes( ByteBuf buf )
    {
        buf.writeInt( targetID );
        buf.writeByte( action.getID() );
    }

    @Override
    public void toBytes( ByteBuf buf )
    {
        targetID = buf.readInt();

        Optional<ActionType> action = ActionType.fromID( buf.readByte() );
        if(!action.isPresent())
        {
            // This is not a valid action, ignore any further input.
            return;
        }

        this.action = action.get();
    }
}
