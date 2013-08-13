package bartsy

import java.util.Date;

class OrderItems {

	static constraints = {
		//itemId(nullable:true)
		title(nullable:true)
		itemName(nullable:true)
		name(nullable:true)
		description(nullable:true)
		categorys(nullable:true)
		basePrice(nullable:true)
		quantity(nullable:true)
		type(nullable:true)
		order_price(nullable:true)
		selectedItems(nullable:true)
		itemList(nullable:true)
		specialInstructions(nullable:true)
	}

	static belongsTo = [order:Orders]
	static mapping = { itemList type: 'text' }
	//String itemId
	String title
	String itemName
	String name
	String description
	String categorys
	String basePrice
	String quantity
	Date dateCreated
	String type
	String order_price
	String selectedItems
	String itemList
	String specialInstructions
}
