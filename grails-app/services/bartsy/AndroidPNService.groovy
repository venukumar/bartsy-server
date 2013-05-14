package bartsy

import grails.converters.JSON
import com.google.android.gcm.server.Message
import com.google.android.gcm.server.Result
import com.google.android.gcm.server.Sender

class AndroidPNService {

	public void sendPN(Map pnMessage,String registrationId){
		try {
			log.info("Came into the android push")
			println "Came into the android push"
			println "message:"+pnMessage.toString()
			println "message as JSON: "+ (pnMessage as JSON).toString()
			//String apiKey = "AIzaSyD8HRgmmsbrAqBOFQGngf5MkHYG1qacj9Q" // development
			String apiKey = "AIzaSyCg5JuXmUMUrdUjNXZFNOncozO3vuUaeak" // production
			Sender sender = new Sender(apiKey)
			Message message
			message = new Message.Builder()
					//            .collapseKey("1")
					//            .timeToLive(3)
					//            .delayWhileIdle(true)

					.addData("badgeCount",String.valueOf(1))
					.addData("message",(pnMessage as JSON).toString())
					.build();
			Result result = sender.send(message,registrationId,1);
			log.info("Result---------->>>"+result.toString())
		} catch (Exception e) {
			log.info("Android PN Exception"+e.getMessage())
		}
	}

//	public void sendPlaceOrderPN(String orderStatus,String orderId,String itemName,String orderTime,String basePrice,String tipPercentage,String totalPrice,String registrationId,String messageType){
//		try {
//			log.info("Came into the android push")
//			//println "Came into the android push"
//			//String apiKey = "AIzaSyD8HRgmmsbrAqBOFQGngf5MkHYG1qacj9Q" // development
//			String apiKey = "AIzaSyDC5CeQRHl3mE7pE9BLemmn1oWhhFxwvVk" // production
//			Sender sender = new Sender(apiKey)
//			
//			Message message
//			message = new Message.Builder()
//					//            .collapseKey("1")
//					//            .timeToLive(3)
//					//            .delayWhileIdle(true)
//					.addData("badgeCount",String.valueOf(1))
//					.addData("message",json.toString())
//					.build();
//
//
//			System.out.println("message "+message);
//			Result result = sender.send(message,registrationId,1);
//			println("result.toString( "+result.toString());
//			log.info("Result---------->>>"+result.toString())
//		} catch (Exception e) {
//			log.info("Android PN Exception"+e.getMessage())
//		}
//	}
//
//	public void sendUserProfilePN(Map userProfileMap,String registrationId,String messageType){
//		try {
//			log.info("Came into the android push")
//			//println "Came into the android push"
//			//String apiKey = "AIzaSyD8HRgmmsbrAqBOFQGngf5MkHYG1qacj9Q" // development
//			String apiKey = "AIzaSyDC5CeQRHl3mE7pE9BLemmn1oWhhFxwvVk" // production
//			Sender sender = new Sender(apiKey)
//			Message message
//			message = new Message.Builder()
//					//            .collapseKey("1")
//					//            .timeToLive(3)
//					//            .delayWhileIdle(true)
//					.addData("bartsyId",userProfileMap.get(bartsyId))
//					.addData("gender",userProfileMap.get(gender))
//					.addData("name",userProfileMap.get(name))
//					.addData("badgeCount",String.valueOf(1))
//					.addData("messageType",messageType)
//					.build();
//			Result result = sender.send(message,registrationId,1);
//			log.info("Result---------->>>"+result.toString())
//		} catch (Exception e) {
//			log.info("Android PN Exception"+e.getMessage())
//		}
//	}
}
