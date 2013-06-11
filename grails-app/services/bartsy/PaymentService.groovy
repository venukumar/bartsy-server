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


	def authorizePayment(){
		def response = [:]
		Merchant merchant = Merchant.createMerchant(Environment.SANDBOX,
				"75x2yLLj", "5Lq4dG24m63qncQ4");
		// create credit card
		CreditCard creditCard = CreditCard.createCreditCard();
		creditCard.setCreditCardNumber("4007000000027");
		creditCard.setExpirationMonth("12");
		creditCard.setExpirationYear("2015");
		// create transaction
		Transaction authCaptureTransaction = merchant.createAIMTransaction(
				TransactionType.AUTH_ONLY, new BigDecimal(1.99));
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
	def capturePayment(def authCode){
		def response = [:]
		Merchant merchant = Merchant.createMerchant(Environment.SANDBOX,
				"75x2yLLj", "5Lq4dG24m63qncQ4");
		// create credit card
		CreditCard creditCard = CreditCard.createCreditCard();
		creditCard.setCreditCardNumber("4007000000027");
		creditCard.setExpirationMonth("12");
		creditCard.setExpirationYear("2015");
		// create transaction
		Transaction authCaptureTransaction = merchant.createAIMTransaction(
				TransactionType.CAPTURE_ONLY, new BigDecimal(1.99));
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
	
	def makePayment(order,status){
		def captureResponse = capturePayment(order.getAuthCode())
		if(captureResponse.get("captureApproved").toBoolean()){
			order.setCaptureApproved("true")
			order.setOrderStatus(status)
			order.setCaptureTransactionNumber(captureResponse.get("captureTransactionNumber"))
		}
		else{
			order.setOrderStatus("7")
			order.setCaptureApproved("false")
			order.setCaptureErrorMessage(captureResponse.get("captureErrorMessage"))
		}
		order.save()
		return response
	}

}

