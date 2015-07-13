package io.coala.json;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import io.coala.json.Wrapper.Polymorphic;
import io.coala.util.JsonUtil;
import io.coala.util.LogUtil;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * {@link JsonTest} tests the {@link DynaBean} used by {@link Wrapper}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">rick</a>
 */
public class JsonTest
{

	/** */
	private static final Logger LOG = LogUtil.getLogger(JsonTest.class);

	/**
	 * {@link MyWrapper} decorates any {@link Number}
	 * 
	 * @date $Date$
	 * @version $Id$
	 * @author <a href="mailto:rick@almende.org">rick</a>
	 */
	@Polymorphic(stringType = MyImaginaryNumber.class, objectType = MyImaginaryNumber.class)
	public static class MyWrapper extends Wrapper.Simple<Number>
	{
		public static MyWrapper valueOf(final Number value)
		{
			final MyWrapper result = new MyWrapper();
			result.setValue(value);
			return result;
		}
	}

	/**
	 * {@link MyImaginaryNumber} decorates imaginary values
	 * 
	 * @date $Date$
	 * @version $Id$
	 * @author <a href="mailto:rick@almende.org">rick</a>
	 */
	public static class MyImaginaryNumber extends Number
	{
		/** */
		private static final long serialVersionUID = 1L;

		/** */
		private static final Pattern parts = Pattern
				.compile("(\\d+[.,]?\\d*)(\\+(\\d+[.,]?\\d*)){1}i");

		private static final String REAL_PART = "realPart",
				IMAGINARY_PART = "imaginaryPart";

		/** */
		@JsonProperty(REAL_PART)
		public BigDecimal realPart = null;

		/** */
		@JsonProperty(IMAGINARY_PART)
		public BigDecimal imaginaryPart = null;

		/**
		 * {@link MyImaginaryNumber} zero-arg bean constructor, for JSON-object
		 * deserialization
		 */
		public MyImaginaryNumber()
		{
			// empty
		}

		/**
		 * {@link MyImaginaryNumber} constructor, for {@link String}
		 * deserialization
		 * 
		 * @param value the {@link String} representation
		 */
		public MyImaginaryNumber(final String value)
		{
			final Matcher matcher = parts.matcher(value);
			if (!matcher.find())
				throw new IllegalArgumentException(value
						+ ", expected: <v1>+<v2>i");

			this.realPart = new BigDecimal(matcher.group(1));
			this.imaginaryPart = new BigDecimal(matcher.group(3));
		}

		@Override
		public String toString()
		{
			return String.format("%s+%si", this.realPart, this.imaginaryPart);
		}

		@Override
		public boolean equals(final Object o)
		{
			return o != null
					&& o instanceof MyImaginaryNumber
					&& this.realPart.equals(((MyImaginaryNumber) o).realPart)
					&& this.imaginaryPart
							.equals(((MyImaginaryNumber) o).imaginaryPart);
		}

		@Override
		public int intValue()
		{
			return this.realPart.intValue();
		}

		@Override
		public long longValue()
		{
			return this.realPart.longValue();
		}

		@Override
		public float floatValue()
		{
			return this.realPart.floatValue();
		}

		@Override
		public double doubleValue()
		{
			return this.realPart.doubleValue();
		}
	}

	@Test
	public void testWrapper()
	{
		final Number v0 = 3;
		final String s0 = v0.toString();

		LOG.trace("Testing wrapped de/serialization of " + v0 + " <--> " + s0);

		assertThat("unwrap-serializer",
				JsonUtil.stringify(MyWrapper.valueOf(v0)), is(s0));

		assertThat("wrap-deserializer", v0,
				is(JsonUtil.valueOf(s0, MyWrapper.class).getValue()));

		final String s1 = "2.1+1.2i";

		LOG.trace("Testing wrapped polymorphic de/serialization of: " + s1);

		// deserialize wrapped value subtype from string
		final MyWrapper n1 = JsonUtil
				.valueOf("\"" + s1 + "\"", MyWrapper.class);
		assertThat("polymorphic-object", n1.getValue(),
				instanceOf(MyImaginaryNumber.class));
		assertThat("polymorphic-string", n1.toString(), equalTo(s1));

		// deserialize wrapped value subtype from object
		final MyWrapper n2 = JsonUtil
				.valueOf("{\"" + MyImaginaryNumber.REAL_PART + "\":2.1,\""
						+ MyImaginaryNumber.IMAGINARY_PART + "\":1.2}",
						MyWrapper.class);

		assertThat("polymorphic-object", n2.getValue(),
				instanceOf(MyImaginaryNumber.class));
		assertThat("polymorphic-object", n2.getValue(), equalTo(n1.getValue()));
	}
}
