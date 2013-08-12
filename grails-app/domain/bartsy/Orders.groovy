package bartsy

class Orders {

    static constraints = {
		orderId(unique:true)
		captureApproved(nullable:true)
		captureTransactionNumber(nullable:true)
		authErrorMessage(nullable:true)
		captureErrorMessage(nullable:true)
		authCode(nullable:true)
		authTransactionNumber(nullable:true)
		lastState(nullable:true)
		errorReason(nullable:true)
		drinkOffered(nullable:true)
		specialInstructions(nullable:true)
		basePrice(nullable:true)
		itemId(nullable:true)
		itemName(nullable:true)
		description(nullable:true)
		itemsList(nullable:true)
		dateOrderStatus(nullable:true)
	}
	
	static mapping = {
		drinkOffered defaultValue: 'false'
		itemsList type: 'text'
	}
    
    static belongsTo = [user:UserProfile,venue:Venue,receiverProfile:UserProfile]
	
    Date dateCreated
	Date dateOffered = new Date()
    String orderStatus 
    String basePrice
	String totalPrice
	String tipPercentage
	String itemName
	String itemId
	String itemsList
	String specialInstructions
	long orderId
	String description
	Date lastUpdated
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
	String dateOrderStatus
}
