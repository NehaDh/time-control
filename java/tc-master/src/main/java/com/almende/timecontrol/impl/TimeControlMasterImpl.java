package com.almende.timecontrol.impl;

import java.util.Map;

import com.almende.timecontrol.api.TimeControlMasterAPI;
import com.almende.timecontrol.entity.Clock;
import com.almende.timecontrol.entity.Scenario;
import com.almende.timecontrol.entity.Scenario.ID;
import com.almende.timecontrol.entity.Scenario.ScenarioInfo;
import com.almende.timecontrol.entity.Slave;
import com.almende.timecontrol.entity.Trigger;

/**
 * {@link TimeControlMasterImpl}
 * 
 * @date $Date$
 * @version $Id$
 * @author <a href="mailto:rick@almende.org">rick</a>
 */
public class TimeControlMasterImpl implements TimeControlMasterAPI
{

	/** @see com.almende.timecontrol.api.TimeControlMasterAPI#getStatus() */
	@Override
	public Map<ID, ScenarioInfo> getStatus()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/** @see com.almende.timecontrol.api.TimeControlMasterAPI#put(com.almende.timecontrol.entity.Scenario) */
	@Override
	public void put(Scenario replication)
	{
		// TODO Auto-generated method stub
		
	}

	/** @see com.almende.timecontrol.api.TimeControlMasterAPI#remove(com.almende.timecontrol.entity.Scenario.ID) */
	@Override
	public void remove(ID name)
	{
		// TODO Auto-generated method stub
		
	}

	/** @see com.almende.timecontrol.api.TimeControlMasterAPI#remove(com.almende.timecontrol.entity.Slave.ID) */
	@Override
	public void remove(com.almende.timecontrol.entity.Slave.ID name)
	{
		// TODO Auto-generated method stub
		
	}

	/** @see com.almende.timecontrol.api.TimeControlMasterAPI#remove(com.almende.timecontrol.entity.Clock.ID) */
	@Override
	public void remove(com.almende.timecontrol.entity.Clock.ID name)
	{
		// TODO Auto-generated method stub
		
	}

	/** @see com.almende.timecontrol.api.TimeControlMasterAPI#remove(com.almende.timecontrol.entity.Trigger.ID) */
	@Override
	public void remove(com.almende.timecontrol.entity.Trigger.ID name)
	{
		// TODO Auto-generated method stub
		
	}

	/** @see com.almende.timecontrol.api.TimeControlMasterAPI#put(com.almende.timecontrol.entity.Scenario.ID, com.almende.timecontrol.entity.Slave) */
	@Override
	public void put(ID scenarioID, Slave federate)
	{
		// TODO Auto-generated method stub
		
	}

	/** @see com.almende.timecontrol.api.TimeControlMasterAPI#put(com.almende.timecontrol.entity.Clock) */
	@Override
	public void put(Clock clock)
	{
		// TODO Auto-generated method stub
		
	}

	/** @see com.almende.timecontrol.api.TimeControlMasterAPI#put(com.almende.timecontrol.entity.Trigger) */
	@Override
	public void put(Trigger trigger)
	{
		// TODO Auto-generated method stub
		
	}



}
