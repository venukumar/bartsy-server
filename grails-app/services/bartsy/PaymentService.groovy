package bartsy


import grails.converters.JSON
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64
import java.security.PrivateKey
import net.authorize.Merchant
import net.authorize.data.Order
import net.authorize.data.Customer
import net.authorize.data.ShippingAddress
import net.authorize.data.creditcard.CreditCard
import net.authorize.Environment
import net.authorize.aim.Result
import net.authorize.aim.Transaction
import net.authorize.TransactionType
import net.authorize.data.creditcard.CardType

class PaymentService {


	def authorizePayment(UserProfile userprofile,def price,def orderId){
		def response = [:]
		Merchant merchant = getMerchant()
		def venueList = CheckedInUsers.findByUserProfile(userprofile)
		def venueInfo = Venue.get(venueList.venue.id)
		// create credit card

		String credit = getDecryptCredit(userprofile.getCreditCardNumber())
		if(!credit.equals("1")){
			CreditCard creditCard = CreditCard.createCreditCard();
			creditCard.setCreditCardNumber(credit);
			creditCard.setExpirationMonth(userprofile.getExpMonth());
			creditCard.setExpirationYear(userprofile.getExpYear());

			// create transaction
			Transaction authCaptureTransaction = merchant.createAIMTransaction(
					TransactionType.AUTH_ONLY, new BigDecimal(price));
			Order order = new Order()
			order.setInvoiceNumber(orderId.toString())

			Customer customer = Customer.createCustomer()
			if(userprofile?.firstName){
				customer.setFirstName(userprofile.firstName)
				customer.setLastName(userprofile.lastName)
			}else
				customer.setFirstName(userprofile.nickName)
			customer.setEmail(userprofile.email)
			customer.setCustomerId(userprofile.bartsyId)

			ShippingAddress address = ShippingAddress.createShippingAddress()
			address.setFirstName(venueInfo.managerName)
			address.setCompany(venueInfo.venueName)
			address.setAddress(venueInfo.streetAddress)
			address.setCity(venueInfo.address)
			address.setState(venueInfo.region)
			address.setZipPostalCode(venueInfo.postalCode)

			authCaptureTransaction.setCreditCard(creditCard)
			authCaptureTransaction.setCustomer(customer)
			authCaptureTransaction.setShippingAddress(address)
			authCaptureTransaction.setOrder(order)
			Result<Transaction> result = (Result<Transaction>) merchant.postTransaction(authCaptureTransaction);
			if(result.isApproved()) {
				response.put("authApproved","true")
				response.put("authCode",result.getTarget().getAuthorizationCode())
				response.put("authTransactionNumber",result.getTarget().getTransactionId())
			}
			else{
				response.put("authApproved","false")
				response.put("authErrorMessage",result.getResponseText())
			}
		}else{

			response.put("authApproved","false")
			response.put("authErrorMessage","Credit Card processor error")
		}

		println response
		return response
	}

	def authorizePaymentInSaveUserProfile(UserProfile userprofile,def price,def orderId){
		def response = [:]
		Merchant merchant = getMerchant()
		// create credit card
		String credit = getDecryptCredit(userprofile.getCreditCardNumber())
		if(!credit.equals("1")){
			CreditCard creditCard = CreditCard.createCreditCard();
			creditCard.setCreditCardNumber(credit);
			creditCard.setExpirationMonth(userprofile.getExpMonth());
			creditCard.setExpirationYear(userprofile.getExpYear());
			// create transaction
			Transaction authCaptureTransaction = merchant.createAIMTransaction(
					TransactionType.AUTH_ONLY, new BigDecimal(price));
			Order order = new Order()
			order.setInvoiceNumber(orderId.toString())
			authCaptureTransaction.setCreditCard(creditCard);
			authCaptureTransaction.setOrder(order)
			Result<Transaction> result = (Result<Transaction>) merchant.postTransaction(authCaptureTransaction);
			if(result.isApproved()) {
				response.put("authApproved","true")
				response.put("authCode",result.getTarget().getAuthorizationCode())
				response.put("authTransactionNumber",result.getTarget().getTransactionId())
			}
			else{
				response.put("authApproved","false")
				response.put("authErrorMessage",result.getResponseText())
			}
		}else{

			response.put("authApproved","false")
			response.put("authErrorMessage","Credit Card processor error")
		}
		println response
		return response
	}
	def capturePayment(def authCode,UserProfile userprofile,def price,def orderId){
		def response = [:]
		Merchant merchant = getMerchant()
		def venueList = CheckedInUsers.findByUserProfile(userprofile)
		def venueInfo = Venue.get(venueList.venue.id)
		String credit = getDecryptCredit(userprofile.getCreditCardNumber())
		if(!credit.equals("1")){
			// create credit card
			CreditCard creditCard = CreditCard.createCreditCard();
			creditCard.setCreditCardNumber(credit);
			creditCard.setExpirationMonth(userprofile.getExpMonth());
			creditCard.setExpirationYear(userprofile.getExpYear());
			// create transaction
			Transaction authCaptureTransaction = merchant.createAIMTransaction(
					TransactionType.CAPTURE_ONLY, new BigDecimal(price));
			Order order = new Order()
			order.setInvoiceNumber(orderId.toString())

			Customer customer = Customer.createCustomer()
			if(userprofile.firstName){
				customer.setFirstName(userprofile.firstName)
				customer.setLastName(userprofile.lastName)
			}else
				customer.setFirstName(userprofile.nickName)
			customer.setEmail(userprofile.email)
			customer.setCustomerId(userprofile.bartsyId)

			ShippingAddress address = ShippingAddress.createShippingAddress()
			address.setFirstName(venueInfo.managerName)
			address.setCompany(venueInfo.venueName)
			address.setAddress(venueInfo.streetAddress)
			address.setCity(venueInfo.address)
			address.setState(venueInfo.region)
			address.setZipPostalCode(venueInfo.postalCode)

			authCaptureTransaction.setAuthorizationCode(authCode)
			authCaptureTransaction.setCreditCard(creditCard);
			authCaptureTransaction.setCustomer(customer)
			authCaptureTransaction.setShippingAddress(address)
			authCaptureTransaction.setOrder(order)
			Result<Transaction> result = (Result<Transaction>) merchant.postTransaction(authCaptureTransaction);
			if(result.isApproved()) {
				response.put("captureApproved","true")
				response.put("authCode",result.getTarget().getAuthorizationCode())
				response.put("captureTransactionNumber",result.getTarget().getTransactionId())
			}
			else{
				response.put("captureApproved","false")
				response.put("captureErrorMessage",result.getResponseText())
			}
		}else{

			response.put("authApproved","false")
			response.put("authErrorMessage","Credit Card processor error")
		}
		println response
		return response
	}

	def makePayment(order){
		def captureResponse = capturePayment(order.getAuthCode(),order.user,order.getTotalPrice(),order.getOrderId())
		if(captureResponse.get("captureApproved").toBoolean()){
			order.setCaptureApproved("true")
			order.setCaptureTransactionNumber(captureResponse.get("captureTransactionNumber"))
		}
		else{
			order.setOrderStatus("7")
			order.setCaptureApproved("false")
			order.setCaptureErrorMessage(captureResponse.get("captureErrorMessage"))
		}
		return order
	}

	def getDecryptCredit(String encCard){
		try{
			def baseFolder = org.codehaus.groovy.grails.web.context.ServletContextHolder.getServletContext().getRealPath("/")
			PrivateKey bartsyPrivateKey=AsymmetricCipherTest.getPemPrivateKey(baseFolder+"images/bartsy_privateKey.pem","RSA")
			Base64 b64 = new Base64();
			//def hexMesg=b64.encode(hexOficeAES.getBytes());
			byte[] bb=b64.decode(encCard)
			log.info("decode with base 64")
			byte[] bDecryptedKey = AsymmetricCipherTest.decrypt(bb,bartsyPrivateKey)
			log.info("decrypt with Bartsy private key")
			String decCredit = new String(bDecryptedKey, "UTF8")
			decCredit = decCredit.trim();
			return decCredit
		}catch(Exception e){
			return "1"
		}
	}
	def getMerchant(){
		def paymentMode = BartsyConfiguration.findByConfigName("payment").value
		def authId = BartsyConfiguration.findByConfigName("authId").value
		def authPwd = BartsyConfiguration.findByConfigName("authPassword").value
		def Merchant merchant
		
		merchant = Merchant.createMerchant(paymentMode,
			authId, authPwd);
		
		/*if(paymentMode.equalsIgnoreCase("SandBox")){
			merchant = Merchant.createMerchant(Environment.SANDBOX,
					authId, authPwd);
		}
		else{
			merchant = Merchant.createMerchant(Environment.PRODUCTION,
					authId, authPwd);
		}*/
		return merchant
	}

}

