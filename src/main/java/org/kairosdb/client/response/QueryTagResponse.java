package org.kairosdb.client.response;

import com.google.gson.JsonSyntaxException;
import org.kairosdb.client.util.JsonUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Response returned by KairosDB.
 */
public class QueryTagResponse extends Response
{
	private final int responseCode;

	private List<TagQuery> results;
	private String body;

	@SuppressWarnings("ConstantConditions")
	public QueryTagResponse( int responseCode, InputStream stream) throws IOException
	{
		super(responseCode);
		this.responseCode = responseCode;
		this.body = getBody(stream);
		this.results = getQueries();
	}

	/**
	 * Returns a list of query results returned by KairosDB. If status code is not
	 * successful, call getErrors to get errors returned.
	 *
	 * @return list of query results or empty list of no data or if an error is returned.
	 * @throws IOException         if could not map response to Queries object
	 * @throws JsonSyntaxException if the response is not JSON or is invalid JSON
	 */
	public List<TagQuery> getQueries() throws IOException
	{
		if (results != null)
			return results;

		if (getBody() != null)
		{
			// We only get JSON if the response is a 200, 400 or 500 error
			if (responseCode == 400 || responseCode == 500)
			{
				ErrorResponse errorResponse = JsonUtils.fromJson(body, ErrorResponse.class);
				addErrors(errorResponse.getErrors());
				return Collections.emptyList();
			}
			else if (responseCode == 200)
			{
				KairosTagsResponse response = JsonUtils.fromJson(body, KairosTagsResponse.class);
				return response.getQueries();
			}
		}

		return Collections.emptyList();
	}

	/**
	 * Returns the body response as a string.
	 *
	 * @return body as a string or empty string.
	 */
	public String getBody()
	{
		return body;
	}

	public String getBody(InputStream stream) throws IOException
	{
		if (stream == null)
			return "";

		StringBuilder builder = new StringBuilder();
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new InputStreamReader(stream));
			String line;
			while ((line = reader.readLine()) != null)
			{
				builder.append(line);
			}
		}
		finally
		{
			if (reader != null)
				reader.close();
		}

		body = builder.toString();
		return body;
	}

	private class KairosTagsResponse
	{
		private List<TagQuery> queries = new ArrayList<TagQuery>();

		public List<TagQuery> getQueries()
		{
			return queries;
		}
	}
}