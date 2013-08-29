package bartsy

import java.util.Date;

class OrderItems {

	static constraints = {
		//itemId(nullable:true)
		title(nullable:true)
		itemName(nullable:true)
		name(nullable:true)
		description(nullable:true)
		optionDescription(nullable:true)
		categorys(nullable:true)
		basePrice(nullable:true)
		quantity(nullable:true)
		type(nullable:true)
		order_price(nullable:true)
		selectedItems(nullable:true)
		itemList(nullable:true)
		specialInstructions(nullable:true)
		options(nullable:true)
		option_groups(nullable:true)
	}

	static belongsTo = [order:Orders]
	static mapping = {
		itemList type: 'text'
		option_groups type: 'text'
	}
	//String itemId
	String title
	String itemName
	String name
	String description
	String optionDescription
	String categorys
	String basePrice
	String quantity
	Date dateCreated
	String type
	String order_price
	String selectedItems
	String itemList
	String specialInstructions
	String options
	String option_groups
}
