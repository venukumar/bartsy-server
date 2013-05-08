package bartsy

class Orders {

    static constraints = {
    }
    
    static belongsTo = [user:UserProfile,venue:Venue]
    Date orderTime
    String orderStatus 
    String basePrice
	String totalPrice
	String tipPercentage
	String itemName
	String itemId
	long orderId
}
