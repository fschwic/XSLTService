package net.schwichtenberg.xsltservice;

import org.apache.commons.pool.KeyedPoolableObjectFactory;
import org.apache.commons.pool.impl.StackKeyedObjectPool;
import org.slf4j.LoggerFactory;

public class TransformerPool extends StackKeyedObjectPool {

	private static final org.slf4j.Logger LOGGER = LoggerFactory
			.getLogger(TransformerPool.class);

	public TransformerPool() {
		super();
		init();
	}

	public TransformerPool(int max, int init) {
		super(max, init);
		init();
	}

	public TransformerPool(int max) {
		super(max);
		init();
	}

	@Override
	public synchronized void setFactory(KeyedPoolableObjectFactory factory)
			throws IllegalStateException {
		throw new UnsupportedOperationException("Factory not configurable.");
	}

	private void init() {
		KeyedPoolableObjectFactory factory = new UrlPoolableTransformerFactory();
		super.setFactory(factory);
		if (LOGGER.isDebugEnabled()) {
			String message = "TransformerPool created. " + this.getNumActive()
					+ " instances active. " + this.getNumIdle()
					+ " instances idle.";
			LOGGER.debug(message);
		}
	}

	@Override
	public synchronized Object borrowObject(Object arg0) throws Exception {
		Object o = super.borrowObject(arg0);
		if (LOGGER.isInfoEnabled()) {
			String message = "Object borrowed. Key[" + arg0 + "] active[key["
					+ this.getNumActive(arg0) + "] all[" + this.getNumActive()
					+ "]] idle[key[" + this.getNumIdle(arg0) + "] all["
					+ this.getNumIdle() + "]]";
			LOGGER.info(message);
		}
		return o;
	}

	@Override
	public synchronized void returnObject(Object arg0, Object arg1)
			throws Exception {
		super.returnObject(arg0, arg1);
		if (LOGGER.isDebugEnabled()) {
			String message = "Object returned. Key[" + arg0 + "] active[key["
					+ this.getNumActive(arg0) + "] all[" + this.getNumActive()
					+ "] idle[key[" + this.getNumIdle(arg0) + "] all["
					+ this.getNumIdle() + "]";
			LOGGER.debug(message);
		}
	}

}
