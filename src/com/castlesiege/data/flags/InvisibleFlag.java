package com.castlesiege.data.flags;

import com.castlesiege.Main;
import com.castlesiege.data.ObjectiveData;
import com.castlesiege.data.TeamData;

public class InvisibleFlag extends Flag
{

	public InvisibleFlag(Main plugin, ObjectiveData objectiveData)
	{
		super(plugin, objectiveData);
	}

	public void doCapture(Main plugin, TeamData teamData, int playerCount, int bannermanCount)
	{
		if(teamData != null)
		{
			if(playerCount > 6)
			{
				playerCount = 6;
			}
			double cap = (((double) (playerCount + bannermanCount) * 0.2) + 0.8) * (200d / timer / 20d);
			if(controlling != null)
			{
				if(!controlling.equals(teamData))
				{
					if(capped > 0)
					{
						capped = capDouble(capped - cap);
						didMessage = false;
					}

					if(capped <= 0)
					{
						switchTeam(plugin, teamData);
					}
				}
				else
				{
					if(capped < 100)
					{
						capped = capDouble(capped + cap);
					}
				}
			}
			else
			{
				if(capped > 0)
				{
					capped = capDouble(capped - cap);
					didMessage = false;
				}

				if(capped <= 0)
				{
					switchTeam(plugin, teamData);
				}
			}
			doCapMessages(plugin, teamData);
		}
	}
}
