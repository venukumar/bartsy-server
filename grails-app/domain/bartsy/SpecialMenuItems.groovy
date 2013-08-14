package bartsy

import java.util.Date;

class SpecialMenuItems {

	static constraints = {
		menuItemId(nullable:true)
		name(nullable:true)
		category(nullable:true)
		alcohol(nullable:true)
		glass(nullable:true)
		ingredients(nullable:true)
		instructions(nullable:true)
		price(nullable:true)
		available(nullable:true)
		description(nullable:true)
		shopping(nullable:true)
		price(nullable:true)
	}
	static belongsTo =[venue:Venue,specialMenu:SpecialMenus]
	static mapping = {
		instructions type:'text'
		ingredients type:'text'
	}
	long menuItemId
	String name
	String category
	String alcohol
	String glass
	String ingredients
	String instructions
	float price
	String available
	Date dateCreated
	Date lastUpdated
	String description
	String shopping
}
