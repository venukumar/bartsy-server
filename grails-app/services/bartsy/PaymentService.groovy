package bartsy


import grails.converters.JSON
import net.authorize.Merchant
import net.authorize.data.creditcard.CreditCard
import net.authorize.Environment
import net.authorize.aim.Result
import net.authorize.aim.Transaction
import net.authorize.TransactionType
import net.authorize.data.creditcard.CardType

class PaymentService {


	def authorizePayment(UserProfile userprofile,def price){
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
		authCaptureTransaction.setCreditCard(creditCard);
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
	def capturePayment(def authCode,UserProfile userprofile,def price){
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
				TransactionType.CAPTURE_ONLY, new BigDecimal(price));
		authCaptureTransaction.setAuthorizationCode(authCode)
		authCaptureTransaction.setCreditCard(creditCard);
		Result<Transaction> result = (Result<Transaction>) merchant.postTransaction(authCaptureTransaction);
		if(result.isApproved()) {
			response.put("captureApproved","true")
			response.put("captureTransactionNumber",result.getTarget().getTransactionId())
		}
		else{
			response.put("captureApproved","false")
			response.put("captureErrorMessage",result.getResponseText())
		}
		println response
		return response
	}
	
	def makePayment(Orders order){
		def captureResponse = capturePayment(order.getAuthCode(),order.user,order.getTotalPrice())
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

