package bartsy

import com.notnoop.apns.ApnsService
import com.notnoop.apns.APNS
import grails.converters.JSON

class ApplePNService {

	def sendPN(Map pnMessage,String token,String bad,String body) {
		ApnsService service = APNS.newService()
				.withCert("/home/swethab/swetha/Bartsy/Certificates.p12", "123456")
				//.withCert("/usr/local/Bartsy/Certificates.p12", "123456")
				.withSandboxDestination()
				.build();

		String payload = APNS.newPayload().alertBody(body)
				.badge(Integer.parseInt(bad))
				.customField("orderStatus", pnMessage.get("orderStatus"))
				.customField("orderId", pnMessage.get("orderId"))
				.customField("messageType", pnMessage.get("messageType"))
				.sound("default")
				.build();
		service.push(token, payload);
	}
	
	def sendPNOrderTimeout(Map pnMessage,String token,String bad,String body) {
		ApnsService service = APNS.newService()
				.withCert("/home/swethab/swetha/Bartsy/Certificates.p12", "123456")
				//.withCert("/usr/local/Bartsy/Certificates.p12", "123456")
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
				.withCert("/home/swethab/swetha/Bartsy/Certificates.p12", "123456")
				//.withCert("/usr/local/Bartsy/Certificates.p12", "123456")
				.withSandboxDestination()
				.build();

		String payload = APNS.newPayload()//.alertBody(body)
				.badge(Integer.parseInt(bad))
				.customField("checkedInUsersList", pnMessage.get("checkedInUsersList"))
				.customField("ordersList", pnMessage.get("ordersList"))
				.customField("messageType", pnMessage.get("messageType"))
				//.sound("default")
				.build();
		service.push(token, payload);
	}	
	
	def sendPNUserTimeout(Map pnMessage,String token,String bad,String body) {
		ApnsService service = APNS.newService()
				.withCert("/home/swethab/swetha/Bartsy/Certificates.p12", "123456")
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
	}
}
