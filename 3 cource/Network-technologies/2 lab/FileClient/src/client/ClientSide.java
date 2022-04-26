package client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

class ClientSide
{
    static void sendFile(String address, int port, String path) throws Throwable
    {
        Socket sock = new Socket(InetAddress.getByName(address), port);
        DataInputStream dis = new DataInputStream(sock.getInputStream());
        DataOutputStream dos = new DataOutputStream(sock.getOutputStream());

        File file = new File(path);
        FileInputStream fis = new FileInputStream(file);

        dos.writeUTF(file.getName());
        dos.writeLong(file.length());

        byte[] buf = new byte[1024 * 4];
        int i;
        while ((i = fis.read(buf)) != -1)
        {
            dos.write(buf, 0, i);
            dos.flush();
        }

        String answer = dis.readUTF();
        System.out.println(answer);

        dis.close();
        dos.close();
        fis.close();
        sock.close();
    }
}
