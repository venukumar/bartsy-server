package bartsy

class OrderItems {

    static constraints = {
		itemId(nullable:true)
		itemName(nullable:true)
		description(nullable:true)
		basePrice(nullable:true)
		
    }
	
	static belongsTo = [order:Orders]
	
	String itemId
	String itemName
	String description
	String basePrice
}
