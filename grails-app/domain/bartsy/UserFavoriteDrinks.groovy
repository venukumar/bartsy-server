package bartsy

class UserFavoriteDrinks {

	static constraints = {
		itemsList()
		itemName(nullable:true)
		description(nullable:true)
		categorys(nullable:true)
		title(nullable:true)
		basePrice(nullable:true)
		quantity(nullable:true)
		type(nullable:true)
		name(nullable:true)
		order_price(nullable:true)
		selectedItems(nullable:true)
		specialInstructions(nullable:true)
		optionDescription(nullable:true)
		options(nullable:true)
		option_groups(nullable:true)
	}


	static mapping = {
		itemsList type: 'text'
		option_groups type: 'text'
	}

	static belongsTo = [user:UserProfile,venue:Venue]

	String title
	String itemName
	String itemsList
	String name
	String description
	String categorys
	String basePrice
	String quantity
	Date dateCreated
	String type
	String order_price
	String selectedItems
	String specialInstructions
	String optionDescription
	String options
	String option_groups
}
