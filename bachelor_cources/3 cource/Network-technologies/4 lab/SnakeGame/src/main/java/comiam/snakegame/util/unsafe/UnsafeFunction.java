package comiam.snakegame.util.unsafe;

@FunctionalInterface
public interface UnsafeFunction<ArgumentT, ResultT>
{

    ResultT apply(final ArgumentT argument) throws Exception;
}
