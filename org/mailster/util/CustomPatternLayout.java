package org.mailster.util;

import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;

public class CustomPatternLayout extends PatternLayout{

	@Override
	public String format(LoggingEvent event) {
		String s = super.format(event);
		if (event.getRenderedMessage().indexOf('\n')>=0)
		{
			String rendered = event.getRenderedMessage();
			StringBuilder sb = new StringBuilder("\n");
			for (int i=0;i<21;i++)
				sb.append(' ');
			String newLineWithBlanks = sb.toString();
			
			sb = new StringBuilder(s.substring(0, s.indexOf('\n')));
			sb.append(newLineWithBlanks);
			rendered = rendered.substring(rendered.indexOf('\n')+1);
			int pos = 0;
			while ((pos = rendered.indexOf('\n')) > -1)
			{
				sb.append(rendered, 0, pos).append(newLineWithBlanks);
				rendered = rendered.substring(pos+1);
			}
			sb.append(rendered).append("\n");
			return sb.toString();
		}
		else
			return s;
	}

}