package bartsy

import com.google.android.gcm.server.Message
import com.google.android.gcm.server.Result
import com.google.android.gcm.server.Sender

class AndroidPNService {

    public void sendPN(String orderStatus,String orderId,String registrationId,String messageType){
        try {
            log.info("Came into the android push")
			println "Came into the android push"
            //String apiKey = "AIzaSyD8HRgmmsbrAqBOFQGngf5MkHYG1qacj9Q" // development
            String apiKey = "AIzaSyBXDQMxqmGHJ7fOQeysFDFmrZRpuI1vp40" // production
            Sender sender = new Sender(apiKey)
            Message message
            message = new Message.Builder()
//            .collapseKey("1")
//            .timeToLive(3)
//            .delayWhileIdle(true)
            .addData("orderStatus",orderStatus)
            .addData("orderId",orderId)
			.addData("messageType",messageType)
            .addData("badgeCount",String.valueOf(1))
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
			String apiKey = "AIzaSyBXDQMxqmGHJ7fOQeysFDFmrZRpuI1vp40" // production
			Sender sender = new Sender(apiKey)
			Message message
			message = new Message.Builder()
//            .collapseKey("1")
//            .timeToLive(3)
//            .delayWhileIdle(true)
			.addData("orderStatus",orderStatus)
			.addData("orderId",orderId)
			.addData("itemName",itemName)
			.addData("orderTime",orderTime)
			.addData("basePrice",basePrice)
			.addData("tipPercentage",tipPercentage)
			.addData("totalPrice",totalPrice)
			.addData("badgeCount",String.valueOf(1))
			.addData("messageType",messageType)
			.build();
			Result result = sender.send(message,registrationId,1);
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
			String apiKey = "AIzaSyBXDQMxqmGHJ7fOQeysFDFmrZRpuI1vp40" // production
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
