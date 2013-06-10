package bartsy

class Orders {

    static constraints = {
		authTransactionId(nullable:true)
		payTransactionId(nullable:true)
    }
    
    static belongsTo = [user:UserProfile,venue:Venue]
	
    Date dateCreated
    String orderStatus 
    String basePrice
	String totalPrice
	String tipPercentage
	String itemName
	String itemId
	long orderId
	String description
	long authTransactionId
	long payTransactionId
	Date lastUpdated
	long recieverBartsyId
}
