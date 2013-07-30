package bartsy

class UserFavoriteDrinks {

	static constraints = {
		itemsList()
		specialInstructions(nullable:true)
		description(nullable:true)
		categorys(nullable:true)
	}
	
	
	static mapping = {
		itemsList type: 'text'
		}
	
	static belongsTo = [user:UserProfile,venue:Venue]

	String itemsList
	String specialInstructions
	String description
	String categorys
	Date dateCreated
}
