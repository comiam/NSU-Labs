package socksproxy;

import socksproxy.auth.AuthMode;
import socksproxy.auth.Users;
import socksproxy.proxy.Proxy;

public class Main
{
    public static void main(String[] args) throws Throwable
    {
        var serverThread = new Thread(new Proxy(1080, new Users().getUserMap(), AuthMode.NO_AUTH), "proxyThread");
        serverThread.start();
    }
}
