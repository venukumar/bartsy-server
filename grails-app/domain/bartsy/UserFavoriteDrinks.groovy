package bartsy

class UserFavoriteDrinks {

	static constraints = {
		itemsList()
		specialInstructions(nullable:true)
		description(nullable:true)
		categorys(nullable:true)
		title(nullable:true)
		basePrice(nullable:true)
		quantity(nullable:true)
	}


	static mapping = { itemsList type: 'text' }

	static belongsTo = [user:UserProfile,venue:Venue]
	String title
	String itemsList
	String specialInstructions
	String description
	String categorys
	String basePrice
	String quantity
	Date dateCreated
}
