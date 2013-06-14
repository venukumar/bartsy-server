package bartsy

class Orders {

    static constraints = {
		captureApproved(nullable:true)
		captureTransactionNumber(nullable:true)
		authErrorMessage(nullable:true)
		captureErrorMessage(nullable:true)
		authCode(nullable:true)
		authTransactionNumber(nullable:true)
		lastState(nullable:true)
		errorReason(nullable:true)
		drinkOffered(nullable:true)
		}
	
	static mapping = {
		drinkOffered defaultValue: 'false'
		}
    
    static belongsTo = [user:UserProfile,venue:Venue]
	
    Date dateCreated
    String orderStatus 
    String basePrice
	String totalPrice
	String tipPercentage
	String itemName
	String itemId
	String specialInstructions
	long orderId
	String description
	Date lastUpdated
	String recieverBartsyId
	String authCode
	String authApproved
	String authTransactionNumber
	String captureApproved
	String captureTransactionNumber
	String authErrorMessage
	String captureErrorMessage
	String lastState
	String errorReason
	boolean drinkOffered
}
