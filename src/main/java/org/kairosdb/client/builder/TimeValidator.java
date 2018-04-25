/*
 * Copyright 2013 Proofpoint Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.kairosdb.client.builder;

import static com.google.common.base.Preconditions.checkState;

/**
 * Validates start and end times.
 */
public class TimeValidator
{
	private static final String START_TIME_EARLIER = "Start time must be earlier than or equal to the ending time\" (note the subtle difference";

	private TimeValidator()
	{
	}

	public static void validateEndTimeLaterThanStartTime(long startTime, long endTime)
	{
		checkState(endTime >= startTime, START_TIME_EARLIER);
	}

	public static void validateEndTimeLaterThanStartTime(RelativeTime startTime, RelativeTime endTime)
	{
		long now = System.currentTimeMillis();
		checkState(startTime.getTimeRelativeTo(now) <= endTime.getTimeRelativeTo(now), START_TIME_EARLIER);
	}

	public static void validateEndTimeLaterThanStartTime(long startTime, RelativeTime endTime)
	{
		long now = System.currentTimeMillis();
		checkState(startTime <= endTime.getTimeRelativeTo(now), START_TIME_EARLIER);
	}

	public static void validateEndTimeLaterThanStartTime(RelativeTime startTime, long endTime)
	{
		long now = System.currentTimeMillis();
		checkState(startTime.getTimeRelativeTo(now) <= endTime, START_TIME_EARLIER);
	}
}