package bartsy

import com.notnoop.apns.ApnsService
import com.notnoop.apns.APNS
import grails.converters.JSON

class ApplePNService {

	def sendPN(Map pnMessage,String token,String bad,String body) {
		ApnsService service = APNS.newService()
//				.withCert("/home/swethab/swetha/Bartsy/Certificates.p12", "123456")
				.withCert("/usr/local/Bartsy/Certificates.p12", "123456")
				.withSandboxDestination()
				.build();

		String payload = APNS.newPayload().alertBody(body)
				.badge(Integer.parseInt(bad))
				.customField("orderStatus", pnMessage.get("orderStatus"))
				.customField("orderId", pnMessage.get("orderId"))
				.customField("messageType", pnMessage.get("messageType"))
				.customField("orderCount", pnMessage.get("orderCount"))
				.customField("updateTime", pnMessage.get("updateTime"))
				.sound("default")
				.build();
		service.push(token, payload);
	}
	
	def sendPNOrderTimeout(Map pnMessage,String token,String bad,String body) {
		ApnsService service = APNS.newService()
//				.withCert("/home/swethab/swetha/Bartsy/Certificates.p12", "123456")
				.withCert("/usr/local/Bartsy/Certificates.p12", "123456")
				.withSandboxDestination()
				.build();

		String payload = APNS.newPayload().alertBody(body)
				.badge(Integer.parseInt(bad))
				.customField("cancelledOrder", pnMessage.get("cancelledOrder"))
				.customField("messageType", pnMessage.get("messageType"))
				.sound("default")
				.build();
		service.push(token, payload);
	}
	
	def sendPNHeartBeat(Map pnMessage,String token,String bad,String body) {
		ApnsService service = APNS.newService()
//				.withCert("/home/swethab/swetha/Bartsy/Certificates.p12", "123456")
				.withCert("/usr/local/Bartsy/Certificates.p12", "123456")
				.withSandboxDestination()
				.build();

		String payload = APNS.newPayload()//.alertBody(body)
				.badge(Integer.parseInt(bad))
				.customField("bartsyId",pnMessage.get("bartsyId"))
				.customField("venueId",pnMessage.get("venueId"))
				.customField("venueName",pnMessage.get("venueName"))
				.customField("messageType",pnMessage.get("messageType"))
				.customField("userCount",pnMessage.get("userCount"))
				.customField("openOrders",pnMessage.get("openOrders"))
				.customField("orderCount",pnMessage.get("orderCount"))
				//.sound("default")
				.build();
		service.push(token, payload);
	}	
	
	def sendPNUserTimeout(Map pnMessage,String token,String bad,String body) {
		ApnsService service = APNS.newService()
//				.withCert("/home/swethab/swetha/Bartsy/Certificates.p12", "123456")
				.withCert("/usr/local/Bartsy/Certificates.p12", "123456")
				.withSandboxDestination()
				.build();

		String payload = APNS.newPayload()//.alertBody(body)
				.badge(Integer.parseInt(bad))
				.customField("bartsyId", pnMessage.get("bartsyId"))
				.customField("messageType", pnMessage.get("messageType"))
				//.sound("default")
				.build();
		service.push(token, payload);
	}
	
	def sendPNDrinkOffered(Map pnMessage,String token,String bad,String body) {		
		println "came in drinkoffered push"
		ApnsService service = APNS.newService()
//				.withCert("/home/swethab/swetha/Bartsy/Certificates.p12", "123456")
				.withCert("/usr/local/Bartsy/Certificates.p12", "123456")
				.withSandboxDestination()
				.build();

		String payload = APNS.newPayload().alertBody(body)
				.badge(Integer.parseInt(bad))
				.customField("bartsyId", pnMessage.get("bartsyId"))
				.customField("messageType", pnMessage.get("messageType"))
				.customField("senderBartsyId", pnMessage.get("senderBartsyId"))
				.customField("orderStatus", pnMessage.get("orderStatus"))
				.customField("orderId", pnMessage.get("orderId"))
				.customField("itemName", pnMessage.get("itemName"))
				.customField("orderTime", pnMessage.get("orderTime"))
				.customField("basePrice", pnMessage.get("basePrice"))
				.customField("totalPrice", pnMessage.get("totalPrice"))
				.customField("description", pnMessage.get("description"))
				.customField("updateTime", pnMessage.get("updateTime"))
				.customField("orderTimeout", pnMessage.get("orderTimeout"))
				.customField("specialInstructions", pnMessage.get("specialInstructions"))
				.sound("default")
				.build();
		service.push(token, payload);
		println payload
	}
	
	def sendPNMessage(Map pnMessage,String token,String bad,String body) {
		ApnsService service = APNS.newService()
//				.withCert("/home/swethab/swetha/Bartsy/Certificates.p12", "123456")
				.withCert("/usr/local/Bartsy/Certificates.p12", "123456")
				.withSandboxDestination()
				.build();

		String payload = APNS.newPayload().alertBody(body)
				.badge(Integer.parseInt(bad))
				.customField("senderId", pnMessage.get("senderId"))
				.customField("messageType", pnMessage.get("messageType"))
				.customField("message", pnMessage.get("message"))
				.customField("receiverId", pnMessage.get("receiverId"))
				.sound("default")
				.build();
		service.push(token, payload);
		println payload
	}
	
	}
