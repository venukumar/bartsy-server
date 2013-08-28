package bartsy

import com.notnoop.apns.ApnsService
import com.notnoop.apns.APNS
import grails.converters.JSON

class ApplePNService {

	def sendPN(Map pnMessage,String token,String bad,String body) {
		println"IOS sendPN body:: "+body
		ApnsService service = APNS.newService()
				.withCert("/home/srikantht/Bartsy_logs/Certificates.p12", "123456")
			//	.withCert("/usr/local/Bartsy/Certificates.p12", "123456")
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
		println"IOS sendPN body END:: "+body

	}

	def sendPNOrderTimeout(Map pnMessage,String token,String bad,String body) {
		println"IOS sendPNOrderTimeout body:: "+body
		ApnsService service = APNS.newService()
				.withCert("/home/srikantht/Bartsy_logs/Certificates.p12", "123456")
			//	.withCert("/usr/local/Bartsy/Certificates.p12", "123456")
				.withSandboxDestination()
				.build();

		String payload = APNS.newPayload().alertBody(body)
				.badge(Integer.parseInt(bad))
				.customField("cancelledOrder", pnMessage.get("cancelledOrder"))
				.customField("messageType", pnMessage.get("messageType"))
				.sound("default")
				.build();
		service.push(token, payload);
		println"IOS sendPNOrderTimeout body END:: "+body
	}

	def sendPNHeartBeat(Map pnMessage,String token,String bad,String body) {
		println"IOS sendPNHeartBeat body:: "+body
		ApnsService service = APNS.newService()
				.withCert("/home/srikantht/Bartsy_logs/Certificates.p12", "123456")
					//.withCert("/usr/local/Bartsy/Certificates.p12", "123456")
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
		println"IOS sendPNHeartBeat body:: END "+body
	}

	def sendPNUserTimeout(Map pnMessage,String token,String bad,String body) {
		println"IOS sendPNUserTimeout body:: "+body
		ApnsService service = APNS.newService()
				.withCert("/home/srikantht/Bartsy_logs/Certificates.p12", "123456")
					//.withCert("/usr/local/Bartsy/Certificates.p12", "123456")
				.withSandboxDestination()
				.build();

		String payload = APNS.newPayload()//.alertBody(body)
				.badge(Integer.parseInt(bad))
				.customField("bartsyId", pnMessage.get("bartsyId"))
				.customField("messageType", pnMessage.get("messageType"))
				//.sound("default")
				.build();
		service.push(token, payload);
		println"IOS sendPNUserTimeout body:: END "+body
	}

	def sendPNDrinkOffered(Map pnMessage,String token,String bad,String body) {
		println"IOS sendPNDrinkOffered body:: "+body
		ApnsService service = APNS.newService()
				.withCert("/home/srikantht/Bartsy_logs/Certificates.p12", "123456")
				//	.withCert("/usr/local/Bartsy/Certificates.p12", "123456")
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
		println"IOS sendPNDrinkOffered body:: END "+body	}
	def sendPNMessage(Map pnMessage,String token,String bad,String body) {
		println"IOS sendPNMessage body:: "+body
		ApnsService service = APNS.newService()
				.withCert("/home/srikantht/Bartsy_logs/Certificates.p12", "123456")
				//		.withCert("/usr/local/Bartsy/Certificates.p12", "123456")
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
		println"IOS sendPNMessage body::END "+body
	}
}
