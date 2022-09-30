package comiam.snakegame.gamelogic.event;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.logging.*;

public class EventQueueProcessor implements Runnable, EventChannel, EventProcessor
{

    private static final Logger logger = Logger.getLogger(EventQueueProcessor.class.getSimpleName());

    private static final int CAPACITY = 64;

    private final BlockingQueue<Event> queue = new ArrayBlockingQueue<>(CAPACITY);
    private final List<CheckedHandler> handlers = new ArrayList<>();

    public void submit(final Event event) throws InterruptedException
    {
        logger.fine(
                Thread.currentThread().getName() + " Queuing event, queue size is " + this.queue.size());
        this.queue.put(event);
    }

    @Override
    public HandlerDescriptor addHandler(
            final Predicate<Event> shouldHandle,
            final EventHandler handler)
    {
        synchronized (this.handlers)
        {
            var checkedHandler = new CheckedHandler(shouldHandle, handler, false);
            this.handlers.add(checkedHandler);
            logger.info("Added handler");
            return this.createRemover(checkedHandler);
        }
    }

    @Override
    public HandlerDescriptor addOneOffHandler(
            final Predicate<Event> shouldHandle,
            final EventHandler handler)
    {
        synchronized (this.handlers)
        {
            var checkedHandler = new CheckedHandler(shouldHandle, handler, true);
            this.handlers.add(checkedHandler);
            logger.info("Added one-off handler");
            return this.createRemover(checkedHandler);
        }
    }

    private HandlerDescriptor createRemover(final CheckedHandler checkedHandler)
    {
        return () -> {
            synchronized (this)
            {
                var removed = this.handlers.remove(checkedHandler);
                if (removed)
                {
                    logger.info("Removed handler");
                } else
                {
                    logger.warning("Handler was not removed");
                }
            }
        };
    }

    @Override
    public void run()
    {
        while (!Thread.currentThread().isInterrupted())
        {
            try
            {
                var message = this.queue.take();
                logger.finest(
                        Thread.currentThread().getName() + " Retrieved message, queue capacity is "
                                + this.queue.size());

                var anyCalled = new boolean[]{false};

                synchronized (this.handlers)
                {
                    for (int index = 0; index < this.handlers.size(); index += 1) {
                        var handler = this.handlers.get(index);
                        if (handler.shouldHandle.test(message)) {
                            try {
                                handler.handler.handle(message);
                                handler.used = true;
                                anyCalled[0] = true;
                                if (handler.isOneOff) {
                                    logger.info("One-off handler executed");
                                }
                            } catch (final Exception e) {
                                logger.log(Level.WARNING, "Exception thrown by message handler {0}", handler.handler);
                                logger.warning(e.toString());
                                return;
                            }
                        }
                    }
                    this.handlers.removeIf(it -> it.isOneOff && it.used);
                }

                if (!anyCalled[0])
                {
                    logger.fine(Thread.currentThread().getName() + " Not a single handler accepted message");
                }
            } catch (final InterruptedException e)
            {
                break;
            }
        }
        logger.log(Level.INFO, "{0} Interrupted", Thread.currentThread().getName());
    }

    private static final class CheckedHandler
    {

        private final Predicate<Event> shouldHandle;
        private final EventHandler handler;
        private final boolean isOneOff;
        private boolean used = false;

        private CheckedHandler(
                final Predicate<Event> shouldHandle,
                final EventHandler handler,
                final boolean isOneOff)
        {
            this.shouldHandle = shouldHandle;
            this.handler = handler;
            this.isOneOff = isOneOff;
        }
    }
}
