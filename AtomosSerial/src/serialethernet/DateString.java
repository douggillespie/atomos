package serialethernet;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateString {

	public static synchronized String getTimeString(boolean andDate) {  
		SimpleDateFormat sdfDate;
		if (andDate) {
			sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
		}
		else {
			sdfDate = new SimpleDateFormat("HH:mm:ss");//dd/MM/yyyy
		}
		Date now = new Date();
		String strDate = sdfDate.format(now);
		return strDate;
	}
}
