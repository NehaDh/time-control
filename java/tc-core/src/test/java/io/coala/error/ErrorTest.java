package io.coala.error;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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
		Contextualized.Publisher.asObservable()
				.filter(new Func1<Contextualized, Boolean>()
				{
					@Override
					public Boolean call(final Contextualized t)
					{
						LOG.trace("Filtering managed exception with context: "
								+ t.getContext());
						final Object v = t.getContext().get(k1);
						return v != null && v.equals(v1);
					}
				}).subscribe(new Observer<Contextualized>()
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
					public void onNext(final Contextualized t)
					{
						LOG.trace("Observed managed exception: " + t, t);
					}
				});

		// should be logged
		final CheckedException e1 = ExceptionBuilder.checked("e1").with(k1, v1)
				.with("k2", "v2").build();

		// should be filtered out
		ExceptionBuilder.unchecked(e1, "u1: %s", "due to " + e1.getUuid())
				.with("k3", "v3").with("k4", "v4").build();

		// should be logged
		final UncheckedException u2 = ExceptionBuilder
				.unchecked(e1, "u2: %s", "due to " + e1.getUuid()).with(k1, v1)
				.with("k4", "v4").build();

		// cause should precede (be smaller than) effect
		assertThat("UUID clock sequence", (Contextualized) u2,
				greaterThan((Contextualized) e1));
	}

}
