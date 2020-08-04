package com.castlesiege.data.guilds;

public enum GuildTheme
{
	DEFAULT(0, new double[]
	{ .5, 0, .5 }, new double[]
	{ 0, 0, 8 }, new double[]
	{ -2, 3, -9 });

	private int id;
	private double[] spawn, map, board;

	GuildTheme(int id, double[] spawn, double[] map, double[] board)
	{
		this.id = id;
		this.spawn = spawn;
		this.map = map;
		this.board = board;
	}

	public int getId()
	{
		return id;
	}

	public double[] getSpawn()
	{
		return spawn;
	}

	public double[] getMap()
	{
		return map;
	}

	public double[] getBoardLocs()
	{
		return new double[]
		{ board[0], board[1], board[2],
				board[0] + 1, board[1], board[2],
				board[0] + 2, board[1], board[2],
				board[0] + 3, board[1], board[2],
				board[0] + 4, board[1], board[2],
				board[0], board[1] - 1, board[2],
				board[0] + 1, board[1] - 1, board[2],
				board[0] + 2, board[1] - 1, board[2],
				board[0] + 3, board[1] - 1, board[2],
				board[0] + 4, board[1] - 1, board[2],
				board[0], board[1] - 2, board[2],
				board[0] + 1, board[1] - 2, board[2],
				board[0] + 2, board[1] - 2, board[2],
				board[0] + 3, board[1] - 2, board[2],
				board[0] + 4, board[1] - 2, board[2] };
	}

	public double[] getLeftPageLoc()
	{
		return new double[]
		{ board[0], board[1] - 3, board[2] };
	}

	public double[] getRightPageLoc()
	{
		return new double[]
		{ board[0] + 4, board[1] - 3, board[2] };
	}

	public double[] getTitleLoc()
	{
		return new double[]
		{ board[0] + 2, board[1] + 1, board[2] };
	}

	public GuildTheme getGuildTheme(int id)
	{
		for(GuildTheme theme : values())
		{
			if(theme.id == id)
			{
				return theme;
			}
		}
		return GuildTheme.DEFAULT;
	}

	public GuildTheme getGuildTheme(String name)
	{
		for(GuildTheme theme : values())
		{
			if(theme.toString().equalsIgnoreCase(name))
			{
				return theme;
			}
		}
		return GuildTheme.DEFAULT;
	}
}
