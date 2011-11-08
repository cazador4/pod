package ar.edu.itba.pod.ui;

import org.joda.time.DateTime;

public class ConsoleFeedbackCallback implements FeedbackCallback {
	
	@Override
	public void print(String format, Object... params) {
		String date = new DateTime().toString("HH:mm:ss");
		System.out.printf("%s --> %s\n", date, String.format(format, params));
	}
}
