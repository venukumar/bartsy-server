package bartsy

import org.codehaus.groovy.grails.web.json.JSONObject;

import com.google.android.gcm.server.Message
import com.google.android.gcm.server.Result
import com.google.android.gcm.server.Sender

class AndroidPNService {

	public void sendPN(String orderStatus,String orderId,String registrationId,String messageType){
		try {
			log.info("Came into the android push")
			println "Came into the android push"
			//String apiKey = "AIzaSyD8HRgmmsbrAqBOFQGngf5MkHYG1qacj9Q" // development
			String apiKey = "AIzaSyDC5CeQRHl3mE7pE9BLemmn1oWhhFxwvVk" // production
			Sender sender = new Sender(apiKey)
			JSONObject json = new JSONObject()

			json.put("orderStatus",orderStatus)
			json.put("orderId",orderId)
			json.put("messageType",messageType)
			Message message
			message = new Message.Builder()
					//            .collapseKey("1")
					//            .timeToLive(3)
					//            .delayWhileIdle(true)

					.addData("badgeCount",String.valueOf(1))
					.addData("message",json.toString())
					.build();
			Result result = sender.send(message,registrationId,1);
			log.info("Result---------->>>"+result.toString())
		} catch (Exception e) {
			log.info("Android PN Exception"+e.getMessage())
		}
	}

	public void sendPlaceOrderPN(String orderStatus,String orderId,String itemName,String orderTime,String basePrice,String tipPercentage,String totalPrice,String registrationId,String messageType){
		try {
			log.info("Came into the android push")
			//println "Came into the android push"
			//String apiKey = "AIzaSyD8HRgmmsbrAqBOFQGngf5MkHYG1qacj9Q" // development
			String apiKey = "AIzaSyDC5CeQRHl3mE7pE9BLemmn1oWhhFxwvVk" // production
			Sender sender = new Sender(apiKey)
			Map<String, String> json = new HashMap<String, String>();
			json.put("orderStatus",orderStatus)
			json.put("orderId",orderId)
			json.put("itemName",itemName)
			json.put("orderTime",orderTime)
			json.put("basePrice",basePrice)
			json.put("tipPercentage",tipPercentage)
			json.put("totalPrice",totalPrice)

			json.put("messageType",messageType)
			Message message
			message = new Message.Builder()
					//            .collapseKey("1")
					//            .timeToLive(3)
					//            .delayWhileIdle(true)
					.addData("badgeCount",String.valueOf(1))
					.addData("message",json.toString())
					.build();


			System.out.println("message "+message);
			Result result = sender.send(message,registrationId,1);
			println("result.toString( "+result.toString());
			log.info("Result---------->>>"+result.toString())
		} catch (Exception e) {
			log.info("Android PN Exception"+e.getMessage())
		}
	}

	public void sendUserProfilePN(Map userProfileMap,String registrationId,String messageType){
		try {
			log.info("Came into the android push")
			//println "Came into the android push"
			//String apiKey = "AIzaSyD8HRgmmsbrAqBOFQGngf5MkHYG1qacj9Q" // development
			String apiKey = "AIzaSyDC5CeQRHl3mE7pE9BLemmn1oWhhFxwvVk" // production
			Sender sender = new Sender(apiKey)
			Message message
			message = new Message.Builder()
					//            .collapseKey("1")
					//            .timeToLive(3)
					//            .delayWhileIdle(true)
					.addData("bartsyId",userProfileMap.get(bartsyId))
					.addData("gender",userProfileMap.get(gender))
					.addData("name",userProfileMap.get(name))
					.addData("badgeCount",String.valueOf(1))
					.addData("messageType",messageType)
					.build();
			Result result = sender.send(message,registrationId,1);
			log.info("Result---------->>>"+result.toString())
		} catch (Exception e) {
			log.info("Android PN Exception"+e.getMessage())
		}
	}
}
