package cloud.xcan.angus.core.event;

import cloud.xcan.angus.spec.EventObject;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.WorkHandler;

/**
 * Interface to be implemented by application event listeners.
 */
public interface EventListener<E> extends EventHandler<EventObject<E>>,
    WorkHandler<EventObject<E>> {


}
