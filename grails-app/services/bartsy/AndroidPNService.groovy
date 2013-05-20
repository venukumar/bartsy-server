package bartsy

import grails.converters.JSON
import com.google.android.gcm.server.Message
import com.google.android.gcm.server.Result
import com.google.android.gcm.server.Sender

class AndroidPNService {

	public void sendPN(Map pnMessage,String registrationId){
		try {
			log.info("Came into the android push")
			//String apiKey = "AIzaSyD8HRgmmsbrAqBOFQGngf5MkHYG1qacj9Q" // development
			String apiKey = "AIzaSyCg5JuXmUMUrdUjNXZFNOncozO3vuUaeak" // production
			Sender sender = new Sender(apiKey)
			Message message
			message = new Message.Builder()
					.addData("badgeCount",String.valueOf(1))
					.addData("message",(pnMessage as JSON).toString())
					.build();
			Result result = sender.send(message,registrationId,1);
			log.info("Result---------->>>"+result.toString())
		} catch (Exception e) {
			log.info("Android PN Exception"+e.getMessage())
		}
	}
}
