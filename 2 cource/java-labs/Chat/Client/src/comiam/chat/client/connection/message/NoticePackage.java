package comiam.chat.server.messages.types;

import java.util.ArrayList;

public class NoticePackage<T>
{
    private final UpdateType type;
    private final ArrayList<T> packageUpd;

    public NoticePackage(UpdateType type, ArrayList<T> updates)
    {
        this.type = type;
        this.packageUpd = updates;
    }

    public ArrayList<T> getPackageUpd()
    {
        return packageUpd;
    }

    public UpdateType getType()
    {
        return type;
    }
}
