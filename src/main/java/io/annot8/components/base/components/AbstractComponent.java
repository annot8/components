package io.annot8.components.base.components;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;

import io.annot8.components.resources.Logging;
import io.annot8.components.resources.Metering;
import io.annot8.components.resources.metering.Metrics;
import io.annot8.components.resources.metering.NoOpMetrics;
import io.annot8.core.components.Annot8Component;
import io.annot8.core.context.Context;
import io.annot8.core.exceptions.BadConfigurationException;
import io.annot8.core.exceptions.MissingResourceException;

public abstract class AbstractComponent implements Annot8Component {

	private Logger logger;
	
	private Metrics metrics;

	@Override
	public void configure(Context context) throws BadConfigurationException, MissingResourceException {
		Annot8Component.super.configure(context);

		// Look if we have a logging resource and crate a logger from it is possible
		Optional<Logging> logging = context.getResource(Logging.class);
		if (logging.isPresent()) {
			logger = logging.get().getLogger(getClass());
		} else {
			createNopLogger();
		}
		
		// Get Metrics
		Optional<Metering> metering = context.getResource(Metering.class);
		if (metering.isPresent()) {
			metrics = metering.get().getMetrics(getClass());
		} else {
			createNopMetrics();
		}
	}

	protected Logger log() {
		// if configure has not been called we might not have a logger, so check and create is necessary
		if (logger == null) {
			createNopLogger();
		}

		return logger;
	}
	
	protected Metrics metrics() {
		// if configure has not been called we might not have a metrics, so check and create is necessary
		if (metrics == null) {
			createNopMetrics();
		}

		return metrics;
	}

	private void createNopLogger() {
		logger = NOPLogger.NOP_LOGGER;
	}
	
	private void createNopMetrics() {
		metrics = NoOpMetrics.instance();
		
	}
}
