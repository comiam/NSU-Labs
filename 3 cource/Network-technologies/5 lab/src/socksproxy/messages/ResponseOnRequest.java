package socksproxy.messages;

import java.util.Arrays;

public class ResponseOnRequest extends ToolsMessage
{
    private final Request request;

    public ResponseOnRequest(Request request)
    {
        super(Arrays.copyOf(request.getBytes(), request.getBytes().length));
        this.request = request;
    }

    public byte[] create(boolean isConnected)
    {
        data[0] = SOCKS_5;
        data[1] = SUCCEEDED;
        if (!request.isCommand(CONNECT_TCP))
            data[1] = COMMAND_NOT_SUPPORTED;

        if (!isConnected)
            data[1] = HOST_NOT_AVAILABLE;

        if (request.getAddressType() == IPv6)
            data[1] = ADDRESS_TYPE_NOT_SUPPORTED;

        return data;
    }
}
