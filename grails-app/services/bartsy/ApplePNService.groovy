package bartsy

import com.notnoop.apns.ApnsService
import com.notnoop.apns.APNS
import grails.converters.JSON

class ApplePNService {

    def sendPN(String orderStatus,String orderId,String token,String bad,String body,String messageType) {
		println "in Apple PN"
		ApnsService service = APNS.newService()
		//.withCert("/home/swethab/swetha/Bartsy/Certificates.p12", "123456")
		.withCert("/usr/local/Bartsy/Certificates.p12", "123456")
		.withSandboxDestination()
		.build();
		
		
		
		String payload = APNS.newPayload().alertBody(body)
				.badge(Integer.parseInt(bad))
				.customField("orderStatus", orderStatus)
				.customField("orderId", orderId)
				.customField("messageType", messageType)
				.sound("default").build();
		service.push(token, payload);
	}
}
