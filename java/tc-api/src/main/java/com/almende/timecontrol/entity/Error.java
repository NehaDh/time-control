/* $Id$
 * $URL$
 * 
 * Part of the EU project Inertia, see http://www.inertia-project.eu/
 * 
 * @license
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Copyright (c) 2014 Almende B.V. 
 */
package com.almende.timecontrol.entity;

import io.coala.json.JsonUtil;
import io.coala.json.dynabean.DynaBean;
import io.coala.json.dynabean.DynaBean.ComparableProperty;
import io.coala.refer.Identifier;

import java.util.Properties;

import org.aeonbits.owner.Config;

import com.almende.timecontrol.TimeControl;
import com.fasterxml.jackson.core.TreeNode;

/**
 * {@link Error} is an extension of the <a
 * href="http://www.jsonrpc.org/specification#error_object">JSON-RPC Error
 * Object</a>
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">Rick</a>
 */
@ComparableProperty(TimeControl.ID_KEY)
public interface Error extends Comparable<Error>, Config
{

	/** @return the {@link ID} of this {@link Error} */
	@Key(TimeControl.ID_KEY)
	ID id();

	/** source of the error, or {@code null} for unknown */
	SlaveConfig.ID source();

	/** JSON-RPC error code, see http://www.jsonrpc.org/specification */
	Integer code();

	/** (localized) error message */
	String message();

	/** error data, e.g. stack trace */
	String data();

	/**
	 * {@link ID}
	 * 
	 * @date $Date$
	 * @version $Id$
	 * @author <a href="mailto:rick@almende.org">Rick</a>
	 */
	class ID extends Identifier<String>
	{
		/** @see org.aeonbits.owner.Converters.CLASS_WITH_VALUE_OF_METHOD */
		public static ID valueOf(final String value)
		{
			return Identifier.valueOf(value, ID.class);
		}
	}

	/**
	 * {@link Builder}
	 * 
	 * @date $Date$
	 * @version $Id$
	 * @author <a href="mailto:rick@almende.org">Rick</a>
	 */
	class Builder extends DynaBean.Builder<Error, Builder>
	{

		/**
		 * {@link Builder} factory method
		 * 
		 * @param json the JSON-formatted {@link String}
		 * @param imports optional property defaults
		 * @return the new {@link Builder}
		 */
		public static Builder fromJSON(final String json,
				final Properties... imports)
		{
			return fromJSON(JsonUtil.valueOf(json));
		}

		/**
		 * {@link Builder} factory method
		 * 
		 * @param tree the partially parsed JSON object
		 * @param imports optional property defaults
		 * @return the new {@link Builder}
		 */
		public static Builder fromJSON(final TreeNode tree,
				final Properties... imports)
		{
			return new Builder(imports).withID(tree.get(TimeControl.ID_KEY));
		}

		/**
		 * @param id the JSON-formatted identifier value
		 * @param imports optional property defaults
		 * @return the new {@link Builder}
		 */
		public static Builder fromID(final String id,
				final Properties... imports)
		{
			return new Builder(imports).withID(ID.valueOf(id));
		}

		public Builder withID(final TreeNode id)
		{
			return withID(JsonUtil.valueOf(id, ID.class));
		}

		public Builder withID(final ID id)
		{
			with(TimeControl.ID_KEY, id);
			return this;
		}

		/**
		 * {@link Builder} constructor, to be extended by a public zero-arg
		 * constructor in concrete sub-types
		 */
		public Builder(final Properties... imports)
		{
			super(imports);
		}

	}

}