package comiam.chat.server.messages.types;

public class Answer
{
    private final String data;
    private final boolean success;

    public Answer(String data, boolean success)
    {
        this.success = success;
        this.data = data;
    }

    public String getData()
    {
        return data;
    }

    public boolean isSuccess()
    {
        return success;
    }
}
