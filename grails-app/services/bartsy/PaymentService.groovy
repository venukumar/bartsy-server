package bartsy


import grails.converters.JSON
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
		Merchant merchant = Merchant.createMerchant(Environment.SANDBOX,
				"75x2yLLj", "5Lq4dG24m63qncQ4");
		def venueList = CheckedInUsers.findByUserProfile(userprofile)
		def venueInfo = Venue.get(venueList.venue.id)
		// create credit card
		CreditCard creditCard = CreditCard.createCreditCard();
		creditCard.setCreditCardNumber(userprofile.getCreditCardNumber());
		creditCard.setExpirationMonth(userprofile.getExpMonth());
		creditCard.setExpirationYear(userprofile.getExpYear());
		// create transaction
		Transaction authCaptureTransaction = merchant.createAIMTransaction(
				TransactionType.AUTH_ONLY, new BigDecimal(price));
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
		println response
		return response
	}

	def authorizePaymentInSaveUserProfile(UserProfile userprofile,def price,def orderId){
		def response = [:]
		Merchant merchant = Merchant.createMerchant(Environment.SANDBOX,
				"75x2yLLj", "5Lq4dG24m63qncQ4");
		// create credit card
		CreditCard creditCard = CreditCard.createCreditCard();
		creditCard.setCreditCardNumber(userprofile.getCreditCardNumber());
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
		println response
		return response
	}
	def capturePayment(def authCode,UserProfile userprofile,def price,def orderId){
		def response = [:]
		Merchant merchant = Merchant.createMerchant(Environment.SANDBOX,
				"75x2yLLj", "5Lq4dG24m63qncQ4");

		def venueList = CheckedInUsers.findByUserProfile(userprofile)
		def venueInfo = Venue.get(venueList.venue.id)
		// create credit card
		CreditCard creditCard = CreditCard.createCreditCard();
		creditCard.setCreditCardNumber(userprofile.getCreditCardNumber());
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

}

