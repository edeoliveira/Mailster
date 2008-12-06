package org.mailster.pop3.commands;

/**
 * ---<br>
 * Mailster (C) 2007-2009 De Oliveira Edouard
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 675 Mass
 * Ave, Cambridge, MA 02139, USA.
 * <p>
 * See&nbsp; <a href="http://tedorg.free.fr/en/projects.php" target="_parent">Mailster
 * Web Site</a> <br>
 * ---
 * <p>
 * Pop3CommandState.java - Class that represents a pair of <code>Pop3Command</code> 
 * and state. This is used to maintain state information when dealing with a muli 
 * state command. 
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class Pop3CommandState
{
    /**
     * Initial state of a multi state command.
     */
    public final static int INITIAL_STATE = 0;
	
    /**
     * The command for which state is carried.
     */
    private Pop3Command command;
    
    /**
     * The next state.
     */
    private int nextState = INITIAL_STATE;

    public Pop3CommandState(Pop3Command command)
    {
        this.command = command;        
    }
    
    public Pop3CommandState(Pop3Command command, int nextState)
    {
        this.command = command;
        this.nextState = nextState;
    }

    public Pop3Command getCommand()
    {
        return command;
    }

    public int getNextState()
    {
        return nextState;
    }

    public void setNextState(int nextState) 
    {
	this.nextState = nextState;
    }
	
    public boolean isInitialState()
    {
	return getNextState() == INITIAL_STATE;
    }
}
