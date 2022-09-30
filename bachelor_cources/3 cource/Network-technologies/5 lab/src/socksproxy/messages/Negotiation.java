package socksproxy.messages;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class Negotiation extends ToolsMessage
{
    public byte[] getResponse()
    {
        return response;
    }

    private byte[] response;

    Negotiation(ByteBuffer negotiationRequest)
    {
        super(new byte[negotiationRequest.limit()]);
        negotiationRequest.get(data);
        if ((data[0]) != X)
            throw new IllegalArgumentException();
    }

    public boolean hasSuccess()
    {
        return response[0] == X && response[1] == SUCCESS;
    }

    public void response(HashMap<String, String> map)
    {
        byte[] login = new byte[data[1]];

        response = new byte[2];
        response[0] = X;
        response[1] = SUCCESS;

        if (data[1] >= 0) System.arraycopy(data, 2, login, 0, data[1]);
        String loginString = new String(login);

        int plen = data[data[1] + 2];
        int pstart = data[1] + 2;

        byte[] password = new byte[plen];
        for (int i = 0; i < plen; ++i)
            password[i] = data[pstart + 1 + i];

        String passwordString = new String(password);

        if (!map.containsKey(loginString))
            response[1] = DENIED;

        if (response[1] != DENIED && !map.get(loginString).equals(passwordString))
            response[1] = DENIED;
    }
}
