package bartsy

import com.vinomis.authnet.AuthorizeNet
import grails.converters.JSON

class PaymentService {

	def authorizePayment() {
		def s = new AuthorizeNet()
		s.authorizeOnly {
			amount '1.00'
			ccNumber '370000000000002'
			cvv '122'
			ccExpDate '012014'
			email 'bhatnagar.swetha@gmail.com'
			invoiceId '123'
		}
		def anr = s.submit()
		return anr
	}

	def capturePayment(){
		def s = new AuthorizeNet()
		s.capturePriorAuthorization {
			amount '1.00'  // Only needed if this amount is less than the original authorization. It cannot be more.
			txid '2194065140'  //TODO: Replace this txId
		}
		def anr = s.submit()
		println anr
	}
	
}
