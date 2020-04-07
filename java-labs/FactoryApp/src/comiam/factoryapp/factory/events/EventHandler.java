package comiam.factoryapp.factory.events;

@FunctionalInterface
public interface EventHandler
{
    void perform(Object event);
}
