package socksproxy.auth;

import javax.crypto.*;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Users
{
    public HashMap<String, String> getUserMap()
    {
        return userMap;
    }

    HashMap<String, String> userMap;

    public HashMap<String, String> getRootMap()
    {
        return rootMap;
    }

    public HashMap<String, String> rootMap;

    public HashMap<String, String> getAdminMap()
    {
        return adminMap;
    }

    HashMap<String, String> adminMap;

    public File getUserFile()
    {
        return userFile;
    }

    File userFile;

    public File getAdminFile()
    {
        return adminFile;
    }

    public File getRootFile()
    {
        return rootFile;
    }

    File rootFile;

    File adminFile;
    PasswordCipher cipher;

    public Users() throws Throwable
    {
        userMap = new HashMap<>();
        adminMap = new HashMap<>();
        rootMap = new HashMap<>();
        userFile = new File("src/users/users");
        adminFile = new File("src/users/admins.txt");
        rootFile = new File("src/users/root.txt");
        cipher = new PasswordCipher("Wearemenmanlymen");

        this.getDataFromFile(getUserMap(), getUserFile().getCanonicalPath());
        this.getDataFromFile(getAdminMap(), getAdminFile().getCanonicalPath());
        this.getDataFromFile(getRootMap(), getRootFile().getCanonicalPath());
    }

    public void getDataFromFile(HashMap<String, String> map, String pathname) throws FileNotFoundException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException
    {
        File file = new File(pathname);
        Scanner magicReader = new Scanner(file);
        String delimeter = "///";
        String[] userdata;
        while (magicReader.hasNext())
        {
            String aux = magicReader.next();
            userdata = aux.split(delimeter);
            userdata[1] = cipher.decrypt(userdata[1]);
            map.put(userdata[0], userdata[1]);
        }
        magicReader.close();
    }
}

