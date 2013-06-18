package org.mailster.util;

import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;

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
 * CustomPatternLayout.java - .
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision$, $Date$
 */
public class CustomPatternLayout extends PatternLayout{

	private static final String NEW_LINE_PLUS_BLANKS = "\n                    ";
	
	@Override
	public String format(LoggingEvent event) {
		String s = super.format(event);
		if (event.getRenderedMessage().indexOf('\n')>=0)
		{
			String rendered = event.getRenderedMessage();
			
			StringBuilder sb = new StringBuilder(s.substring(0, s.indexOf('\n')));
			sb.append(NEW_LINE_PLUS_BLANKS);
			rendered = rendered.substring(rendered.indexOf('\n')+1);
			int pos = 0;
			while ((pos = rendered.indexOf('\n')) > -1)
			{
				sb.append(rendered, 0, pos).append(NEW_LINE_PLUS_BLANKS);
				rendered = rendered.substring(pos+1);
			}
			sb.append(rendered).append("\n");
			return sb.toString();
		}
		else
			return s;
	}
}