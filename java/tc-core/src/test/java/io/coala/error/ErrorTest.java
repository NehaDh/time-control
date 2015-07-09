package io.coala.error;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import rx.Observer;
import rx.functions.Func1;

/**
 * {@link ErrorTest} tests {@link ExceptionBuilder}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">rick</a>
 */
public class ErrorTest
{

	/** */
	private static final Logger LOG = LogManager.getLogger(ErrorTest.class);

	@Test
	public void testChecked1()
	{
		final String k1 = "k1", v1 = "v1";
		// subscribe to all exceptions being generated
		ExceptionBuilder.getObservable()
				.filter(new Func1<ManageableException, Boolean>()
				{
					@Override
					public Boolean call(final ManageableException t)
					{
						LOG.trace("Filtering managed exception with context: "
								+ t.getContext());
						final Object v = t.getContext().get(k1);
						return v != null && v.equals(v1);
					}
				}).subscribe(new Observer<ManageableException>()
				{
					@Override
					public void onCompleted()
					{
						LOG.trace("Completed observing managed exceptions");
					}

					@Override
					public void onError(final Throwable e)
					{
						LOG.warn("Problem observing managed exceptions", e);
					}

					@Override
					public void onNext(final ManageableException t)
					{
						LOG.trace("Observed managed exception with context: "
								+ t.getContext(), t);
					}
				});

		// should be logged
		final CheckedException e1 = ExceptionBuilder.checked("e1").with(k1, v1)
				.with("k2", "v2").build();

		// should be filtered out
		ExceptionBuilder.unchecked(e1, "u1: %s", "due to " + e1.getUuid())
				.with("k3", "v3").with("k4", "v4").build();

		// should be logged
		ExceptionBuilder.unchecked(e1, "u2: %s", "due to " + e1.getUuid())
				.with(k1, v1).with("k4", "v4").build();
	}

}
